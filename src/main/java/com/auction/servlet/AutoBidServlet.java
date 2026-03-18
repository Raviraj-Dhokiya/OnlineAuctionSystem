package com.auction.servlet;

import com.auction.dao.AutoBidDAO;
import com.auction.dao.AuctionItemDAO;
import com.auction.model.AutoBid;
import com.auction.model.AuctionItem;
import com.auction.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ════════════════════════════════════════════════════════
 *  AutoBidServlet.java — Auto Bid Enable/Disable/Status
 * ════════════════════════════════════════════════════════
 *
 * ACTIONS:
 *   POST action=enable  → Auto bid set karo (itemId, maxTarget)
 *   POST action=cancel  → Auto bid band karo
 *   GET  action=status  → Kya user ka auto bid active hai? (JSON)
 *
 * URL: /AutoBidServlet
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/AutoBidServlet")
public class AutoBidServlet extends HttpServlet {

    private final AutoBidDAO    autoBidDAO = new AutoBidDAO();
    private final AuctionItemDAO itemDAO   = new AuctionItemDAO();
    private final ObjectMapper  mapper     = new ObjectMapper();

    // ── GET — Status check (AJAX se call hoga) ────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            res.getWriter().write("{\"error\":\"not_logged_in\"}");
            return;
        }

        User user = (User) session.getAttribute("loggedUser");
        String itemIdStr = req.getParameter("itemId");

        if (itemIdStr == null) {
            res.getWriter().write("{\"error\":\"missing_itemId\"}");
            return;
        }

        int itemId;
        try { itemId = Integer.parseInt(itemIdStr); }
        catch (NumberFormatException e) {
            res.getWriter().write("{\"error\":\"invalid_itemId\"}");
            return;
        }

        AutoBid ab = autoBidDAO.getActiveAutoBid(itemId, user.getUserId());

        Map<String, Object> result = new LinkedHashMap<>();
        if (ab != null) {
            result.put("active", true);
            result.put("maxTarget", ab.getMaxTarget());
        } else {
            result.put("active", false);
        }

        res.getWriter().write(mapper.writeValueAsString(result));
    }

    // ── POST — Enable ya Cancel ───────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            res.getWriter().write("{\"ok\":false,\"msg\":\"Please login first.\"}");
            return;
        }

        User user   = (User) session.getAttribute("loggedUser");
        String action   = req.getParameter("action");
        String itemIdStr = req.getParameter("itemId");

        if (itemIdStr == null) {
            res.getWriter().write("{\"ok\":false,\"msg\":\"itemId missing.\"}");
            return;
        }

        int itemId;
        try { itemId = Integer.parseInt(itemIdStr); }
        catch (NumberFormatException e) {
            res.getWriter().write("{\"ok\":false,\"msg\":\"Invalid itemId.\"}");
            return;
        }

        // ── ACTION: enable ─────────────────────────────────────────────────────
        if ("enable".equals(action)) {
            String maxTargetStr = req.getParameter("maxTarget");

            double maxTarget;
            try { maxTarget = Double.parseDouble(maxTargetStr); }
            catch (Exception e) {
                res.getWriter().write("{\"ok\":false,\"msg\":\"Please enter a valid target price.\"}");
                return;
            }

            // Validation: maxTarget > currentPrice hona chahiye
            AuctionItem item = itemDAO.findById(itemId);
            if (item == null || !"ACTIVE".equals(item.getStatus())) {
                res.getWriter().write("{\"ok\":false,\"msg\":\"Auction is not available.\"}");
                return;
            }

            if (maxTarget <= item.getCurrentPrice()) {
                res.getWriter().write("{\"ok\":false,\"msg\":\"Target price must be greater than current bid: &#8377;" +
                        String.format("%.0f", item.getCurrentPrice()) + "\"}");
                return;
            }

            // Seller cannot auto bid on their own item
            if (item.getSellerId() == user.getUserId()) {
                res.getWriter().write("{\"ok\":false,\"msg\":\"You cannot place an auto bid on your own item.\"}");
                return;
            }

            AutoBid autoBid = new AutoBid(itemId, user.getUserId(), maxTarget);
            boolean saved   = autoBidDAO.saveAutoBid(autoBid);

            if (saved) {
                res.getWriter().write("{\"ok\":true,\"msg\":\"Auto Bid activated! Target: &#8377;" +
                        String.format("%.0f", maxTarget) + "\"}");
            } else {
                res.getWriter().write("{\"ok\":false,\"msg\":\"Could not save Auto Bid. Please try again.\"}");
            }

        }
        // ── ACTION: cancel ─────────────────────────────────────────────────────
        else if ("cancel".equals(action)) {
            autoBidDAO.deactivateAutoBid(itemId, user.getUserId());
            res.getWriter().write("{\"ok\":true,\"msg\":\"Auto Bid has been disabled.\"}");

        } else {
            res.getWriter().write("{\"ok\":false,\"msg\":\"Invalid action.\"}");
        }
    }
}
