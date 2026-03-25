package com.auction.servlet;

import com.auction.dao.*;
import com.auction.model.*;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

/**
 * ════════════════════════════════════════════════════════
 *  AdminServlet.java — Admin Panel Handler
 * ════════════════════════════════════════════════════════
 *
 * YEH SERVLET KYA KARTA HAI?
 *   Admin ke liye sabse powerful page. Admin yahan se:
 *   - Saare users dekh aur block/unblock kar sakta hai
 *   - Saari auctions dekh sakta hai
 *   - Manually auction close kar sakta hai (winner declare)
 *   - Payment status update kar sakta hai
 *
 * URL: /AdminServlet
 * ACCESS: Sirf ADMIN role wale (AuthFilter check karta hai)
 *
 * ACTION-BASED ROUTING:
 *   GET  (koi action nahi)    → Admin dashboard dikhao
 *   GET  ?action=exportBidsCsv → CSV file download (read-only, GET theek hai)
 *   POST ?action=closeAuction  → Auction manually band karo (state-change)
 *   POST ?action=toggleUser    → User ko block ya unblock karo (state-change)
 *   POST ?action=updatePayment → Payment status update karo (state-change)
 *
 * SECURITY FIX (BidServlet ki tarah):
 *   - State-changing actions (closeAuction, toggleUser, updatePayment) ab
 *     POST method se handle hote hain — GET nahi.
 *   - CSRF Token: doGet() mein token generate hota hai, doPost() mein verify.
 *   - Isse CSRF attacks se protection milti hai.
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/AdminServlet")
public class AdminServlet extends HttpServlet {

    private final UserDAO        userDAO   = new UserDAO();
    private final AuctionItemDAO itemDAO   = new AuctionItemDAO();
    private final WinnerDAO      winnerDAO = new WinnerDAO();
    private final BidDAO         bidDAO    = new BidDAO();

    /**
     * GET Request — Admin dashboard dikhao + read-only actions.
     * State-changing actions (closeAuction, toggleUser, updatePayment) yahan NAHI hote.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String action = req.getParameter("action");

        // ── Action: Bid History CSV Export (read-only, GET OK hai) ───────────
        if ("exportBidsCsv".equals(action)) {
            int itemId;
            try {
                itemId = Integer.parseInt(req.getParameter("itemId"));
            } catch (NumberFormatException e) {
                res.sendRedirect(req.getContextPath() + "/AdminServlet");
                return;
            }

            AuctionItem targetItem = itemDAO.findById(itemId);
            String itemTitle = (targetItem != null) ? targetItem.getTitle() : "item_" + itemId;
            List<Bid> bids = bidDAO.getBidsForItem(itemId);

            String safeTitle = itemTitle.replaceAll("[^a-zA-Z0-9_-]", "_");
            String dateStr   = new SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
            String fileName  = "bids_" + safeTitle + "_" + dateStr + ".csv";

            res.setContentType("text/csv;charset=UTF-8");
            res.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            PrintWriter pw = res.getWriter();
            pw.println("Bid ID,Bidder,Bid Amount (INR),Bid Time,Is Winning");

            SimpleDateFormat dateFmt = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
            for (Bid b : bids) {
                String bidTimeStr = (b.getBidTime() != null) ? dateFmt.format(b.getBidTime()) : "";
                pw.println(
                    b.getBidId() + "," +
                    csvVal(b.getBidderName()) + "," +
                    String.format("%.2f", b.getBidAmount()) + "," +
                    csvVal(bidTimeStr) + "," +
                    (b.isWinning() ? "YES" : "NO")
                );
            }
            pw.flush();

            com.auction.io.AuctionLogger.log(
                "ADMIN_CSV_EXPORT | Item #" + itemId + " | " + itemTitle +
                " | " + bids.size() + " bids"
            );
            return;
        }

        // ── Admin Dashboard dikhao ────────────────────────────────────────────
        // CSRF token generate karo — POST forms mein use hoga
        HttpSession session = req.getSession(true);
        String csrfToken = UUID.randomUUID().toString();
        session.setAttribute("adminCsrfToken", csrfToken);
        req.setAttribute("csrfToken", csrfToken);

        req.setAttribute("allUsers",   userDAO.getAllUsers());
        req.setAttribute("allItems",   itemDAO.getAllItems());
        req.setAttribute("allWinners", winnerDAO.getAllWinners());

        // Success/error message (redirect ke baad dikhao)
        String msg = req.getParameter("msg");
        if (msg != null) req.setAttribute("msg", msg);

        req.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(req, res);
    }

    /**
     * POST Request — State-changing admin actions yahan handle hote hain.
     * CSRF token verify hota hai pehle (BidServlet.doPost() ki tarah).
     *
     * POST ?action=closeAuction  → Auction close + winner declare
     * POST ?action=toggleUser    → User block/unblock
     * POST ?action=updatePayment → Payment status update
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // ── CSRF Verification (BidServlet jaisi — one-time token) ─────────────
        HttpSession session = req.getSession(false);
        if (session == null) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Session expired. Please log in again.");
            return;
        }

        String formToken = req.getParameter("csrfToken");
        String sessToken = (String) session.getAttribute("adminCsrfToken");

        if (formToken == null || !formToken.equals(sessToken)) {
            // Token mismatch → possible CSRF attack → 403 Forbidden
            res.sendError(HttpServletResponse.SC_FORBIDDEN,
                "Invalid CSRF token. Action rejected for security.");
            return;
        }
        // Token use ho gaya → invalidate karo (one-time use)
        session.removeAttribute("adminCsrfToken");

        String action = req.getParameter("action");

        // ── Action: Auction manually close karo ──────────────────────────────
        if ("closeAuction".equals(action)) {
            try {
                int itemId = Integer.parseInt(req.getParameter("itemId"));
                // Oracle Stored Procedure: { CALL determine_winner(itemId) }
                itemDAO.closeAuctionAndDetermineWinner(itemId);
                res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=auction_closed");
            } catch (NumberFormatException e) {
                res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=error_invalid_id");
            }
            return;
        }

        // ── Action: User ko block ya unblock karo ────────────────────────────
        if ("toggleUser".equals(action)) {
            try {
                int    userId = Integer.parseInt(req.getParameter("userId"));
                String active = req.getParameter("active"); // "true" ya "false"
                userDAO.setActiveStatus(userId, "true".equals(active));
                res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=user_updated");
            } catch (NumberFormatException e) {
                res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=error_invalid_id");
            }
            return;
        }

        // ── Action: Payment status update karo ───────────────────────────────
        if ("updatePayment".equals(action)) {
            try {
                int    winnerId = Integer.parseInt(req.getParameter("winnerId"));
                String status   = req.getParameter("status"); // "PAID", "PENDING", "FAILED"
                winnerDAO.updatePaymentStatus(winnerId, status);
                res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=payment_updated");
            } catch (NumberFormatException e) {
                res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=error_invalid_id");
            }
            return;
        }

        // Unknown action
        res.sendRedirect(req.getContextPath() + "/AdminServlet");
    }

    // ── CSV Helper: value mein comma/quotes ho toh wrap karo ─────────────────
    private String csvVal(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
