package com.auction.servlet;

import com.auction.dao.*;
import com.auction.model.*;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

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

    /**
     * GET Request — Admin panel ke saare actions yahan handle hote hain.
     * URL parameter "action" se pata chalta hai kya karna hai.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // URL se "action" parameter nikalo (jaise ?action=closeAuction)
        String action = req.getParameter("action");

        // ── Action: Auction manually close karo ──────────────────────────────
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

        // ── Koi action nahi → Admin Dashboard dikhao ─────────────────────────
        // Saara data DB se fetch karo aur admin.jsp ko forward karo
        req.setAttribute("allUsers",   userDAO.getAllUsers());     // Sab registered users
        req.setAttribute("allItems",   itemDAO.getAllItems());     // Sab auction items
        req.setAttribute("allWinners", winnerDAO.getAllWinners()); // Sab winners

        req.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(req, res);
    }
}
