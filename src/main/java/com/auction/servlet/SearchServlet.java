package com.auction.servlet;

import com.auction.dao.AuctionItemDAO;
import com.auction.security.SecurityUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

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
     * URL: /SearchServlet?q=searchKeyword
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // URL se search query nikalo (q = query)
        // sanitizeInput() → user agar HTML code likhe toh usse safe banao
        String query = SecurityUtil.sanitizeInput(req.getParameter("q"));

        // Khaali search → Dashboard par wapas bhejo
        if (query == null || query.trim().isEmpty()) {
            res.sendRedirect(req.getContextPath() + "/DashboardServlet");
            return;
        }

        // DB mein search karo — case-insensitive (UPPER() function use hota hai)
        req.setAttribute("searchResults", itemDAO.searchItems(query.trim()));
        req.setAttribute("searchQuery",   query); // JSP mein "Results for: guitar" dikhane ke liye

        // search-results.jsp forward karo
        req.getRequestDispatcher("/WEB-INF/views/search-results.jsp").forward(req, res);
    }
}
