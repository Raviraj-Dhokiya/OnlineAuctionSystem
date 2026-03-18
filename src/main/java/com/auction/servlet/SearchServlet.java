package com.auction.servlet;

import com.auction.dao.AuctionItemDAO;
import com.auction.security.SecurityUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 *  SearchServlet.java — Auction Items Search
 * ════════════════════════════════════════════════════════
 *
 * YEH SERVLET KYA KARTA HAI?
 *   Dashboard ke search box mein jo keyword type karo,
 *   woh yahan process hota hai. DB mein title aur category
 *   mein search karke matching items laata hai.
 *
 * URL: /SearchServlet?q=guitar
 *
 * FLOW:
 *   1. URL se "q" parameter nikalo (search keyword)
 *   2. Khaala hai? → Dashboard bhejo
 *   3. sanitizeInput() → XSS attack prevent karo
 *   4. AuctionItemDAO.searchItems(keyword) → DB mein search
 *      SQL: WHERE UPPER(title) LIKE '%GUITAR%' OR UPPER(category) LIKE '%GUITAR%'
 *   5. Results set karo → search-results.jsp forward karo
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/SearchServlet")
public class SearchServlet extends HttpServlet {

    private final AuctionItemDAO itemDAO = new AuctionItemDAO();

    /**
     * GET Request — Search results dikhao.
     * URL: /SearchServlet?q=searchKeyword&category=Art&minPrice=100...
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // 1. Inputs read and sanitize
        String query    = SecurityUtil.sanitizeInput(req.getParameter("q"));
        String category = SecurityUtil.sanitizeInput(req.getParameter("category"));
        String minPStr  = SecurityUtil.sanitizeInput(req.getParameter("minPrice"));
        String maxPStr  = SecurityUtil.sanitizeInput(req.getParameter("maxPrice"));
        String sortBy   = SecurityUtil.sanitizeInput(req.getParameter("sortBy"));

        // If no query and no category filters exist, it's an empty search
        if ((query == null || query.trim().isEmpty()) && 
            (category == null || category.trim().isEmpty() || category.equals("ALL"))) {
            // Optional: You could redirect, but let's allow "show all" with filters
            query = ""; // Treat blank as 'match all' for keyword
        }

        // Parse prices
        double minPrice = 0;
        double maxPrice = 0;
        try { if (minPStr != null && !minPStr.isEmpty()) minPrice = Double.parseDouble(minPStr); } catch(Exception ignored){}
        try { if (maxPStr != null && !maxPStr.isEmpty()) maxPrice = Double.parseDouble(maxPStr); } catch(Exception ignored){}

        // 2. Fetch data from DB using new filtered method
        List<com.auction.model.AuctionItem> results = itemDAO.searchItemsFiltered(
            query, category, minPrice, maxPrice, sortBy
        );

        // Fetch bid count for each result card
        com.auction.dao.BidDAO bidDAO = new com.auction.dao.BidDAO();
        for (com.auction.model.AuctionItem item : results) {
            item.setBidCount(bidDAO.getBidCount(item.getItemId()));
        }

        // 3. Set attributes for the JSP
        req.setAttribute("searchResults", results);
        req.setAttribute("searchQuery",   query);
        req.setAttribute("searchCategory",category);
        req.setAttribute("searchMinPrice",minPStr);
        req.setAttribute("searchMaxPrice",maxPStr);
        req.setAttribute("searchSortBy",  sortBy);
        
        // Load distinct categories for the dropdown filter
        req.setAttribute("categories",  itemDAO.getActiveCategories());

        // 4. Forward to search-results.jsp
        req.getRequestDispatcher("/WEB-INF/views/search-results.jsp").forward(req, res);
    }
}
