package com.auction.servlet;

import com.auction.dao.AuctionItemDAO;
import com.auction.dao.BidDAO;
import com.auction.dao.WinnerDAO;
import com.auction.model.User;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 *  DashboardServlet.java — Main Home Page (Logged-in users ke liye)
 * ════════════════════════════════════════════════════════
 *
 * YEH SERVLET KYA KARTA HAI?
 *   Login ke baad user ko jo main page dikhta hai — woh yeh servlet provide karta hai.
 *   Teen sections ke liye DB se data laata hai:
 *   1. Active auctions → sab chal rahi auctions ki list
 *   2. Mere bids       → is user ne jo bids lagayi hain
 *   3. Mere wins       → is user ne jo auctions jeeti hain
 *
 * URL: /DashboardServlet
 *
 * FLOW:
 *   1. Session se logged user ka object nikalo
 *   2. Teen DAO calls se data lao
 *   3. Sab data request attributes mein rakho
 *   4. dashboard.jsp forward karo jo HTML view banata hai
 *
 * NB: Yeh sirf GET handle karta hai — dashboard par koi form submit nahi hota.
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/DashboardServlet")
public class DashboardServlet extends HttpServlet {

    // Teen DAOs create karo — har ek alag table se data laata hai
    private final AuctionItemDAO itemDAO   = new AuctionItemDAO(); // auction_items table
    private final BidDAO         bidDAO    = new BidDAO();         // bids table
    private final WinnerDAO      winnerDAO = new WinnerDAO();      // winners table

    /**
     * GET Request — Dashboard data laao aur view show karo.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Session se logged user nikalo
        // (AuthFilter ne pehle hi guarantee ki hai ki session exist karta hai)
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("loggedUser");

        // ── DB se Data Fetch karo ─────────────────────────────────────────────

        // 1. Saari ACTIVE auctions (status='ACTIVE' AND end_time > now)
        //    dashboard.jsp mein "Available Auctions" section mein dikhegi
        req.setAttribute("activeItems", itemDAO.getActiveItems());

        // 2. Is user ki apni sari bids (bidder_id = loggedUser.userId)
        //    dashboard.jsp mein "My Bids" section
        req.setAttribute("myBids", bidDAO.getBidsByUser(user.getUserId()));

        // 3. Is user ne jo auctions jeeti hain
        //    dashboard.jsp mein "My Wins" section
        req.setAttribute("myWins", winnerDAO.getWinsByUser(user.getUserId()));

        // ── View par forward karo (RequestDispatcher) ─────────────────────────
        // req mein set data JSP ke mein ${activeItems}, ${myBids} se access hoga
        req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, res);
    }
}
