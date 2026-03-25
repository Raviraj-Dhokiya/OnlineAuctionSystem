package com.auction.servlet;

import com.auction.dao.AuctionItemDAO;
import com.auction.dao.BidDAO;
import com.auction.dao.WatchlistDAO;
import com.auction.dao.WinnerDAO;
import com.auction.model.*;
import com.auction.security.SecurityUtil;
import com.auction.network.BidNotificationServer;
import com.auction.patterns.AuctionBidNotifier;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * ════════════════════════════════════════════════════════
 * BidServlet.java — Item Detail + Bid Place Karna
 * ════════════════════════════════════════════════════════
 *
 * FIXES APPLIED:
 * 1. CSRF Protection: doGet() mein token generate hota hai, doPost() mein
 * verify hota hai.
 * Attacker fake page se bid nahi lagwa sakta.
 * 2. NumberFormatException Fix: itemId parse try-catch mein hai —
 * URL mein "abc" dene par 500 nahi, redirect hoga Dashboard par.
 * 3. Real-time notification: BidNotificationServer.broadcastBidUpdate() se
 * // sab connected browsers ko instantly new bid ki info milti hai
 * 4. sanitizeInput() applied on user inputs before use.
 *
 * URL: /BidServlet?itemId=5
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/BidServlet")
public class BidServlet extends HttpServlet {

    private final AuctionItemDAO itemDAO = new AuctionItemDAO();
    private final BidDAO bidDAO = new BidDAO();
    private final WinnerDAO winnerDAO = new WinnerDAO();
    private final WatchlistDAO watchlistDAO = new WatchlistDAO();

    /**
     * GET Request — Auction item ki detail page dikhao.
     * Also generates a CSRF token and stores it in session.
     * URL: /BidServlet?itemId=5
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // FIX: NumberFormatException — try-catch se parse karo
        String itemIdStr = req.getParameter("itemId");
        if (itemIdStr == null) {
            res.sendRedirect(req.getContextPath() + "/DashboardServlet");
            return;
        }

        int itemId;
        try {
            itemId = Integer.parseInt(itemIdStr);
        } catch (NumberFormatException e) {
            // URL mein "abc" jaise galat value → graceful redirect
            res.sendRedirect(req.getContextPath() + "/DashboardServlet");
            return;
        }

        // CSRF FIX: Session mein ek naya random token generate karo
        // Bid form submit hone par yeh token verify hoga
        HttpSession session = req.getSession(true);
        String csrfToken = UUID.randomUUID().toString();
        session.setAttribute("csrfToken", csrfToken);
        req.setAttribute("csrfToken", csrfToken); // JSP mein use hoga

        // DB se item ki detail, sab bids aur winner (agar koi ho) lao
        AuctionItem item = itemDAO.findById(itemId);
        List<Bid> bids = bidDAO.getBidsForItem(itemId);
        Winner winner = winnerDAO.getWinnerByItem(itemId);

        // Fetch additional gallery images IDs from LOCAL FOLDER (not Oracle DB)
        List<Integer> extraImages = new java.util.ArrayList<>();
        String uploadPath = System.getProperty("user.home") + java.io.File.separator + "auction_uploads";
        for (int i = 1; i <= 4; i++) {
            java.io.File file = new java.io.File(uploadPath, "item_" + itemId + "_" + i + ".jpg");
            if (file.exists()) {
                extraImages.add(i);
            }
        }

        req.setAttribute("item", item);
        req.setAttribute("bids", bids);
        req.setAttribute("winner", winner);
        req.setAttribute("bidCount", bids.size());
        req.setAttribute("extraImages", extraImages);

        // ── Minimum Bid Suggestion ───────────────────────────────────────────
        // Real auction systems mein "minimum next bid" suggest karte hain.
        // Formula: currentPrice + 5% ya currentPrice + 1, jo bhi bada ho.
        if (item != null) {
            double currentPrice = item.getCurrentPrice();
            double fivePercent = currentPrice * 0.05;
            double increment = Math.max(fivePercent, 1.0); // minimum ₹1
            double minNextBid = Math.ceil(currentPrice + increment); // round up
            req.setAttribute("minNextBid", minNextBid);

            // ── Watchlist Status ─────────────────────────────────────────────
            // Check karo user ne yeh item watch kiya hua hai?
            User loggedUser = (User) session.getAttribute("loggedUser");
            if (loggedUser != null) {
                boolean watching = watchlistDAO.isWatching(loggedUser.getUserId(), itemId);
                req.setAttribute("isWatching", watching);
            }
        }

        req.getRequestDispatcher("/WEB-INF/views/item-detail.jsp").forward(req, res);
    }

    /**
     * POST Request — Bid form submit hone par (user "Place Bid" click kare).
     * CSRF token verify hota hai pehle.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Session se logged user nikalo
        HttpSession session = req.getSession(false);
        User loggedUser = (User) session.getAttribute("loggedUser");

        // CSRF FIX: Token verify karo
        String formToken = req.getParameter("csrfToken");
        String sessToken = (String) session.getAttribute("csrfToken");
        if (formToken == null || !formToken.equals(sessToken)) {
            // Token mismatch → possible CSRF attack → 403 Forbidden
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token.");
            return;
        }
        // Token use ho gaya → invalidate karo (one-time use)
        session.removeAttribute("csrfToken");

        // FIX: NumberFormatException on itemId — try-catch se parse karo
        String itemIdStr = req.getParameter("itemId");
        int itemId;
        try {
            itemId = Integer.parseInt(SecurityUtil.sanitizeInput(itemIdStr));
        } catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/DashboardServlet");
            return;
        }

        double bidAmount;
        try {
            bidAmount = Double.parseDouble(req.getParameter("bidAmount"));
        } catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/BidServlet?itemId=" + itemId + "&error=invalid_amount");
            return;
        }

        // Negative ya zero bid accept nahi
        if (bidAmount <= 0) {
            res.sendRedirect(req.getContextPath() + "/BidServlet?itemId=" + itemId + "&error=invalid_amount");
            return;
        }

        com.auction.model.AuctionItem auctionItem = itemDAO.findById(itemId);
        if (auctionItem != null && auctionItem.getSellerId() == loggedUser.getUserId()) {
            res.sendRedirect(req.getContextPath() + "/BidServlet?itemId=" + itemId + "&error=own_item");
            return;
        }

        // ── Bid Place karo (DB Transaction) ──────────────────────────────────
        // BidDAO.placeBid() internally:
        // 1. SELECT FOR UPDATE → row lock (race condition fix)
        // 2. Check: bidAmount > current_price aur end_time > NOW
        // 3. INSERT INTO bids + UPDATE auction_items (Transaction)
        Bid bid = new Bid(itemId, loggedUser.getUserId(), bidAmount);
        boolean success = bidDAO.placeBid(bid);
        if (success) {
            // Real-time notification — sab connected browsers ko batao (TCP Socket)
            AuctionItem item = itemDAO.findById(itemId);
            BidNotificationServer.getInstance().broadcastBidUpdate(
                    itemId,
                    loggedUser.getUsername(),
                    bidAmount,
                    item != null ? item.getTitle() : "");

            // BUG #5 FIX: Observer Pattern trigger karo — pehle yeh KABHI call nahi hota tha!
            // Email observer, Watchlist observer, aur Log observer sab ab trigger honge.
            if (item != null) {
                Bid placedBid = new Bid(itemId, loggedUser.getUserId(), bidAmount);
                AuctionBidNotifier.getInstance().publish(placedBid, item);
            }

            res.sendRedirect(req.getContextPath() + "/BidServlet?itemId=" + itemId + "&success=1");
        } else {
            res.sendRedirect(req.getContextPath() + "/BidServlet?itemId=" + itemId + "&error=bid_low");
        }
    }
}