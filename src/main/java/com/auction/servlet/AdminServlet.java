package com.auction.servlet;

import com.auction.dao.*;
import com.auction.model.*;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;

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
 *   ?action=closeAuction  → Auction manually band karo
 *   ?action=toggleUser    → User ko block ya unblock karo
 *   ?action=updatePayment → Payment status update karo
 *   (koi action nahi)     → Admin dashboard dikhao
 *
 * NOTE: GET method hi use hota hai yahan, POST nahi.
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/AdminServlet")
public class AdminServlet extends HttpServlet {

    private final UserDAO        userDAO   = new UserDAO();        // users table
    private final AuctionItemDAO itemDAO   = new AuctionItemDAO(); // auction_items table
    private final WinnerDAO      winnerDAO = new WinnerDAO();      // winners table
    private final BidDAO         bidDAO    = new BidDAO();         // bids table (CSV export)

    /**
     * GET Request — Admin panel ke saare actions yahan handle hote hain.
     * URL parameter "action" se pata chalta hai kya karna hai.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // URL se "action" parameter nikalo (jaise ?action=closeAuction)
        String action = req.getParameter("action");

        // ── Action: Auction manually close karo ────────────────────────────────────
        if ("closeAuction".equals(action)) {
            int itemId = Integer.parseInt(req.getParameter("itemId"));

            // Oracle Stored Procedure call (CallableStatement):
            // { CALL determine_winner(itemId) }
            // Procedure: sabse badi bid dhundh ke winners table mein insert karta hai
            itemDAO.closeAuctionAndDetermineWinner(itemId);

            // Admin page par wapas redirect karo (success message ke saath)
            res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=auction_closed");
            return;
        }

        // ── Action: User ko block ya unblock karo ────────────────────────────
        if ("toggleUser".equals(action)) {
            int    userId = Integer.parseInt(req.getParameter("userId"));
            String active = req.getParameter("active"); // "true" ya "false"

            // DB mein UPDATE users SET is_active=? WHERE user_id=?
            userDAO.setActiveStatus(userId, "true".equals(active));

            res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=user_updated");
            return;
        }

        // ── Action: Payment status update karo ───────────────────────────────
        if ("updatePayment".equals(action)) {
            int    winnerId = Integer.parseInt(req.getParameter("winnerId"));
            String status   = req.getParameter("status"); // "PAID", "PENDING", "FAILED"

            // DB mein UPDATE winners SET payment_status=? WHERE winner_id=?
            winnerDAO.updatePaymentStatus(winnerId, status);

            res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=payment_updated");
            return;
        }

        // ── Action: Bid History CSV Export ───────────────────────────────────
        // ?action=exportBidsCsv&itemId=5
        // FileWriter se CSV banao aur browser ko download dedo
        if ("exportBidsCsv".equals(action)) {
            int itemId;
            try {
                itemId = Integer.parseInt(req.getParameter("itemId"));
            } catch (NumberFormatException e) {
                res.sendRedirect(req.getContextPath() + "/AdminServlet");
                return;
            }

            // Auction item info (title ke liye)
            AuctionItem targetItem = itemDAO.findById(itemId);
            String itemTitle = (targetItem != null) ? targetItem.getTitle() : "item_" + itemId;

            // Sab bids lao (highest first)
            List<Bid> bids = bidDAO.getBidsForItem(itemId);

            // File naam banao: bids_ItemTitle_20250317.csv
            String safeTitle = itemTitle.replaceAll("[^a-zA-Z0-9_-]", "_");
            String dateStr   = new SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
            String fileName  = "bids_" + safeTitle + "_" + dateStr + ".csv";

            // Response headers: file download trigger karo
            res.setContentType("text/csv;charset=UTF-8");
            res.setHeader("Content-Disposition",
                          "attachment; filename=\"" + fileName + "\"");

            // PrintWriter se CSV likh do
            // (FileWriter jaisa hi — PrintWriter directly response mein likha raha hai)
            PrintWriter pw = res.getWriter();

            // CSV Header row
            pw.println("Bid ID,Bidder,Bid Amount (INR),Bid Time,Is Winning");

            // Data rows
            SimpleDateFormat dateFmt = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
            for (Bid b : bids) {
                String bidTimeStr = (b.getBidTime() != null)
                    ? dateFmt.format(b.getBidTime()) : "";
                // CSV values mein comma ya quote ho toh double-quote mein wrap karo
                pw.println(
                    b.getBidId() + "," +
                    csvVal(b.getBidderName()) + "," +
                    String.format("%.2f", b.getBidAmount()) + "," +
                    csvVal(bidTimeStr) + "," +
                    (b.isWinning() ? "YES" : "NO")
                );
            }
            pw.flush();

            // Log bhi karo (Java I/O)
            com.auction.io.AuctionLogger.log(
                "ADMIN_CSV_EXPORT | Item #" + itemId + " | " + itemTitle +
                " | " + bids.size() + " bids"
            );
            return;
        }

        // ── Koi action nahi → Admin Dashboard dikhao ─────────────────────────
        // Saara data DB se fetch karo aur admin.jsp ko forward karo
        req.setAttribute("allUsers",   userDAO.getAllUsers());     // Sab registered users
        req.setAttribute("allItems",   itemDAO.getAllItems());     // Sab auction items
        req.setAttribute("allWinners", winnerDAO.getAllWinners()); // Sab winners

        req.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(req, res);
    }
    // ── CSV Helper: value mein comma/quotes ho toh wrap karo ─────────────────
    private String csvVal(String val) {
        if (val == null) return "";
        // Agar value mein comma ya double-quote hai → double-quote mein wrap karo
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
