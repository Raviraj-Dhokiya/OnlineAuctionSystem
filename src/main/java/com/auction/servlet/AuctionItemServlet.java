package com.auction.servlet;

import com.auction.dao.AuctionItemDAO;
import com.auction.model.AuctionItem;
import com.auction.model.User;
import com.auction.security.SecurityUtil;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 *  AuctionItemServlet.java — Nayi Auction Item Add Karna
 * ════════════════════════════════════════════════════════
 *
 * YEH SERVLET KYA KARTA HAI?
 *   - Nayi auction item list karne ka form dikhata hai (doGet)
 *   - Form submit hone par item DB mein save karta hai (doPost)
 *   - Item ki photo (image) bhi upload hoti hai aur DB mein BLOB format mein save hoti hai
 *   - Item ki photo ko browser mein serve bhi karta hai (?action=image)
 *
 * URL: /AuctionItemServlet
 *
 * MULTIPART FORM:
 *   Image upload ke liye normal form kaam nahi karta.
 *   enctype="multipart/form-data" use hota hai.
 *   Apache Commons FileUpload library isko parse karti hai.
 *
 * FLOW (doPost):
 *   1. Form multipart hai? → verify karo
 *   2. Har field parse karo (title, price, dates, image file)
 *   3. Image → bytes[] mein convert karo → BLOB ke roop mein save hoga
 *   4. AuctionItemDAO.addItem() → DB mein INSERT
 *   5. Success → naye item ki detail page par redirect
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/AuctionItemServlet")
public class AuctionItemServlet extends HttpServlet {

    private final AuctionItemDAO itemDAO = new AuctionItemDAO();

    /**
     * GET Request — "List New Item" form dikhao ya item image serve karo.
     *
     * ?action=image&itemId=5 → DB se image bytes nikalo aur browser ko bhejo
     * (koi action nahi)      → add-item.jsp form dikhao
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String action = req.getParameter("action");

        if ("image".equals(action)) {
            // ── Image serve karo (BLOB retrieval) ────────────────────────────
            // FIX: NumberFormatException — itemId parse try-catch mein
            String itemIdStr = req.getParameter("itemId");
            int itemId;
            try {
                itemId = Integer.parseInt(itemIdStr);
            } catch (NumberFormatException e) {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // DB se image ka binary data nikalo
            byte[] imageData = itemDAO.getItemImage(itemId);

            if (imageData != null) {
                res.setContentType("image/jpeg");
                res.setContentLength(imageData.length);
                res.getOutputStream().write(imageData);
            } else {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            return;
        }

        // Default: "Add New Item" form dikhao
        req.getRequestDispatcher("/WEB-INF/views/add-item.jsp").forward(req, res);
    }

    /**
     * POST Request — New auction item save karo (form submit hone par).
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Session se seller info nikalo (current logged-in user = seller)
        HttpSession session = req.getSession(false);
        User seller = (User) session.getAttribute("loggedUser");

        // ── Multipart form check ─────────────────────────────────────────────
        // Image upload ke liye form "multipart/form-data" hona chahiye
        if (!ServletFileUpload.isMultipartContent(req)) {
            req.setAttribute("error", "Invalid form submission.");
            req.getRequestDispatcher("/WEB-INF/views/add-item.jsp").forward(req, res);
            return;
        }

        // Commons FileUpload library se multipart form parse karo
        DiskFileItemFactory factory = new DiskFileItemFactory(); // temporary disk storage
        ServletFileUpload   upload  = new ServletFileUpload(factory);
        upload.setFileSizeMax(5 * 1024 * 1024L); // Max 5 MB image allow

        // Naya AuctionItem object banao (isme saara data fill hoga)
        AuctionItem item = new AuctionItem();
        item.setSellerId(seller.getUserId()); // seller = current logged-in user

        try {
            // Form ke sab fields parse karo (text + file dono)
            List<FileItem> fileItems = upload.parseRequest(req);

            for (FileItem fi : fileItems) {

                if (fi.isFormField()) {
                    // ── Normal text field ─────────────────────────────────────
                    switch (fi.getFieldName()) {
                        case "title":
                            item.setTitle(SecurityUtil.sanitizeInput(fi.getString()));
                            break;
                        case "description":
                            item.setDescription(SecurityUtil.sanitizeInput(fi.getString()));
                            break;
                        case "category":
                            item.setCategory(SecurityUtil.sanitizeInput(fi.getString()));
                            break;
                        case "startingPrice":
                            item.setStartingPrice(Double.parseDouble(fi.getString()));
                            break;
                        case "reservePrice":
                            String rp = fi.getString();
                            if (!rp.isEmpty()) item.setReservePrice(Double.parseDouble(rp));
                            break;
                        case "endTime":
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                            Date endDate = sdf.parse(fi.getString());
                            item.setEndTime(new Timestamp(endDate.getTime()));
                            break;
                    }
                } else {
                    // ── File field → Image Upload (BLOB) ─────────────────────
                    if (!fi.getName().isEmpty() && fi.getSize() > 0) {

                        // FIX: File size check — max 2 MB
                        if (fi.getSize() > 2L * 1024 * 1024) {
                            req.setAttribute("error", "Image too large. Maximum allowed size is 2 MB.");
                            req.getRequestDispatcher("/WEB-INF/views/add-item.jsp").forward(req, res);
                            return;
                        }

                        // FIX: File type check — only images allowed
                        String contentType = fi.getContentType();
                        if (contentType == null || !contentType.startsWith("image/")) {
                            req.setAttribute("error", "Invalid file type. Please upload an image file (jpg, png, gif, etc.).");
                            req.getRequestDispatcher("/WEB-INF/views/add-item.jsp").forward(req, res);
                            return;
                        }

                        InputStream is = fi.getInputStream();
                        byte[] imageBytes = is.readAllBytes();
                        item.setImageData(imageBytes);
                        item.setImageName(SecurityUtil.sanitizeInput(fi.getName()));
                    }
                }
            }

            // Start time = abhi (jis moment item list ki ja rahi hai)
            item.setStartTime(new Timestamp(System.currentTimeMillis()));

            // ── DB mein INSERT karo ───────────────────────────────────────────
            // Returns newly generated item_id (auto-increment)
            int newItemId = itemDAO.addItem(item);

            if (newItemId > 0) {
                // Success → naye item ki detail page par redirect karo
                res.sendRedirect(req.getContextPath() +
                    "/BidServlet?itemId=" + newItemId + "&success=listed");
            } else {
                req.setAttribute("error", "Failed to list item. Please try again.");
                req.getRequestDispatcher("/WEB-INF/views/add-item.jsp").forward(req, res);
            }

        } catch (Exception e) {
            System.err.println("[AuctionItemServlet] Error: " + e.getMessage());
            req.setAttribute("error", "Error processing request: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/add-item.jsp").forward(req, res);
        }
    }
}
