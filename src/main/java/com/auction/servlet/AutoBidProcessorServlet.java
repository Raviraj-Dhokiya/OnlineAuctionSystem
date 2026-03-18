package com.auction.servlet;

import com.auction.dao.AutoBidDAO;
import com.auction.dao.AuctionItemDAO;
import com.auction.dao.BidDAO;
import com.auction.model.AutoBid;
import com.auction.model.AuctionItem;
import com.auction.model.Bid;
import com.auction.model.User;
import com.auction.network.BidNotificationServer;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ════════════════════════════════════════════════════════
 *  AutoBidProcessorServlet.java — Auto Bid 30-Second Trigger
 * ════════════════════════════════════════════════════════
 *
 * YEH SERVLET KYA KARTA HAI?
 *   - JavaScript (item-detail.jsp) jab kisi aur ki bid detect kare,
 *     30 seconds baad yeh servlet ko call karta hai.
 *   - Yeh servlet check karta hai:
 *       1. Is item ke koi active auto bidders hain?
 *       2. Jo highest bidder hai, uske alawa kya auto bidders hain?
 *       3. Unka auto bid = currentPrice + 10%
 *       4. Agar amount > maxTarget → auto bid band karo
 *       5. Warna bid place karo
 *
 * URL: POST /AutoBidProcessorServlet
 * PARAMS: itemId, lastBidder (jo abhi bid laya — usko skip karo)
 *
 * RESPONSE JSON:
 *   { "triggered": true/false, "bidder": "username", "amount": 5500.0 }
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/AutoBidProcessorServlet")
public class AutoBidProcessorServlet extends HttpServlet {

    private final AutoBidDAO    autoBidDAO = new AutoBidDAO();
    private final AuctionItemDAO itemDAO   = new AuctionItemDAO();
    private final BidDAO        bidDAO     = new BidDAO();
    private final ObjectMapper  mapper     = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        // Session check — logged in hona chahiye
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            res.getWriter().write("{\"triggered\":false,\"msg\":\"Not logged in\"}");
            return;
        }

        String itemIdStr  = req.getParameter("itemId");
        String lastBidder = req.getParameter("lastBidder"); // jo abhi bid laya

        if (itemIdStr == null) {
            res.getWriter().write("{\"triggered\":false,\"msg\":\"itemId missing\"}");
            return;
        }

        int itemId;
        try { itemId = Integer.parseInt(itemIdStr); }
        catch (NumberFormatException e) {
            res.getWriter().write("{\"triggered\":false,\"msg\":\"invalid itemId\"}");
            return;
        }

        // Auction item fetch karo — latest price ke liye
        AuctionItem item = itemDAO.findById(itemId);
        if (item == null || !"ACTIVE".equals(item.getStatus()) || item.isEnded()) {
            res.getWriter().write("{\"triggered\":false,\"msg\":\"Auction active nahi hai\"}");
            return;
        }

        double currentPrice = item.getCurrentPrice();

        // Is item ke sab active auto bids lo
        List<AutoBid> autoBids = autoBidDAO.getAllActiveForItem(itemId);

        if (autoBids.isEmpty()) {
            res.getWriter().write("{\"triggered\":false,\"msg\":\"Koi auto bid nahi\"}");
            return;
        }

        boolean anyTriggered = false;
        String triggeredBidder = null;
        double triggeredAmount = 0;

        for (AutoBid ab : autoBids) {
            // Jo abhi sabse upar hai (current highest bidder), usko skip karo
            // (woh pehle se win kar raha hai, dobara bid kyun kare?)
            if (ab.getUsername() != null && ab.getUsername().equals(lastBidder)) {
                continue;
            }

            // Auto bid amount = current price + 10%
            double autoBidAmount = Math.ceil(currentPrice * 1.10);

            // Agar auto bid amount > maxTarget → auto bid band karo
            if (autoBidAmount > ab.getMaxTarget()) {
                System.out.println("[AutoBidProcessor] User " + ab.getUsername() +
                    " ka target ₹" + ab.getMaxTarget() + " reach ho gaya. Auto bid band.");
                autoBidDAO.deactivateAutoBid(itemId, ab.getUserId());
                continue;
            }

            // Bid place karo
            Bid bid = new Bid(itemId, ab.getUserId(), autoBidAmount);
            boolean success = bidDAO.placeBid(bid);

            if (success) {
                // Real-time notification
                BidNotificationServer.getInstance().broadcastBidUpdate(
                        itemId,
                        ab.getUsername(),
                        autoBidAmount,
                        item.getTitle());

                System.out.println("[AutoBidProcessor] Auto bid placed: " +
                    ab.getUsername() + " → ₹" + autoBidAmount +
                    " (item #" + itemId + ")");

                anyTriggered    = true;
                triggeredBidder = ab.getUsername();
                triggeredAmount = autoBidAmount;

                // Ek auto bid process karne ke baad baaki ke auto bids
                // dobara poll se trigger honge (chain bidding avoid)
                break;
            }
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("triggered", anyTriggered);
        if (anyTriggered) {
            resp.put("bidder", triggeredBidder);
            resp.put("amount", triggeredAmount);
        }

        res.getWriter().write(mapper.writeValueAsString(resp));
    }
}
