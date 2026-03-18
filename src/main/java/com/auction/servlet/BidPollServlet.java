package com.auction.servlet;

import com.auction.dao.BidDAO;
import com.auction.model.Bid;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 *  BidPollServlet.java — Real-time Bid Updates (AJAX Polling)
 * ════════════════════════════════════════════════════════
 *
 * YEH SERVLET KYA KARTA HAI?
 *   bid-live.js (JavaScript) har 3 seconds mein is servlet ko secretly
 *   call karta hai. Yeh servlet naye bids JSON format mein return karta hai.
 *   Isse page refresh kiye bina live price update hoti hai.
 *
 * URL: /BidPollServlet?itemId=5&since=1710000000000
 *
 * PARAMETERS:
 *   itemId → kis auction item ke bids chahiye
 *   since  → kaun se time ke baad ke bids chahiye (milliseconds)
 *            (purane bids dobara mat dikhao)
 *
 * RESPONSE FORMAT (JSON):
 *   [
 *     {"itemId": 5, "bidder": "Rahul", "amount": 5000.0, "item": "Item #5"},
 *     ...
 *   ]
 *
 * FLOW:
 *   1. itemId aur since parameter nikalo
 *   2. BidDAO se us item ke sab bids lao
 *   3. Sirf "since" ke baad ke naye bids filter karo
 *   4. JSON mein convert karo → response mein likhdo
 *
 * NOTE: Yeh HTML nahi, pure JSON return karta hai (application/json).
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/BidPollServlet")
public class BidPollServlet extends HttpServlet {

    private final BidDAO      bidDAO = new BidDAO();
    private final ObjectMapper mapper = new ObjectMapper(); // JSON converter (Jackson library)

    /**
     * GET Request — Naye bids JSON mein return karo.
     * JavaScript (bid-live.js) har 3 sec mein ise call karta hai.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Response type JSON set karo (HTML nahi)
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        // URL parameters nikalo
        String itemIdStr = req.getParameter("itemId");
        String lastBidIdStr = req.getParameter("lastBidId"); // Time sync bug fix: now using bidId instead of timestamp

        long lastBidId = 0;
        try { lastBidId = Long.parseLong(lastBidIdStr); } catch (Exception ignored) {}

        List<Object> result = new ArrayList<>();

        if (itemIdStr != null) {
            int    itemId = Integer.parseInt(itemIdStr);
            final long filterId = lastBidId;

            // DB se us item ke sab bids lao
            List<Bid> bids = bidDAO.getBidsForItem(itemId);

            // Sirf naye bids bhejo — jo "lastBidId" ke baad aaye hain
            bids.stream()
                .filter(b -> b.getBidId() > filterId)  // ID is reliable, time sync is not
                .forEach(b -> {
                    // Har bid ko Map (key-value) format mein convert karo
                    java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
                    map.put("bidId", b.getBidId()); // Send bidId to client
                    map.put("itemId", b.getItemId());
                    map.put("bidder", b.getBidderName()); // kisne lagai
                    map.put("amount", b.getBidAmount());  // kitna
                    map.put("item",   "Item #" + b.getItemId());
                    
                    // Format the date for the table
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM HH:mm:ss");
                    String formattedTime = sdf.format(b.getBidTime());
                    map.put("bidTimeStr", formattedTime);
                    
                    result.add(map);
                });
        }

        // List ko JSON string mein convert karo aur response mein likho
        // mapper.writeValueAsString() → [{"itemId":5,"bidder":"Rahul","amount":5000.0}]
        res.getWriter().write(mapper.writeValueAsString(result));
    }
}
