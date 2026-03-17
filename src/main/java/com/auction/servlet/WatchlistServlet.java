package com.auction.servlet;

import com.auction.dao.WatchlistDAO;
import com.auction.model.User;
import com.auction.model.Watchlist;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 *  WatchlistServlet.java — Watchlist Handler
 * ════════════════════════════════════════════════════════
 *
 * YEH SERVLET KYA KARTA HAI?
 *   User ki watchlist manage karta hai:
 *   - GET (koi action nahi) → User ki watchlist dikhao (watchlist.jsp)
 *   - GET ?action=add&itemId=X    → Item watchlist mein add karo
 *   - GET ?action=remove&itemId=X → Item watchlist se hatao
 *
 * URL: /WatchlistServlet
 * ACCESS: Sirf logged-in users (AuthFilter check karta hai)
 *
 * FLOW:
 *   Item detail page pe "Add to Watchlist" button click karo
 *   → WatchlistServlet?action=add&itemId=5
 *   → WatchlistDAO.addToWatchlist() call hoga
 *   → Back redirect hoga item page par
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/WatchlistServlet")
public class WatchlistServlet extends HttpServlet {

    private final WatchlistDAO watchlistDAO = new WatchlistDAO();

    /**
     * GET Request — Watchlist actions aur view yahan handle hote hain.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Session se current user nikalo
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            res.sendRedirect(req.getContextPath() + "/LoginServlet");
            return;
        }

        User loggedUser = (User) session.getAttribute("loggedUser");
        int  userId     = loggedUser.getUserId();

        String action = req.getParameter("action");
        String itemIdStr = req.getParameter("itemId");

        // ── Action: Add to Watchlist ──────────────────────────────────────────
        if ("add".equals(action) && itemIdStr != null) {
            try {
                int itemId = Integer.parseInt(itemIdStr);
                watchlistDAO.addToWatchlist(userId, itemId);
                // Item detail page par wapas redirect karo (with success message)
                res.sendRedirect(req.getContextPath() +
                    "/BidServlet?itemId=" + itemId + "&watchAdded=1");
            } catch (NumberFormatException e) {
                res.sendRedirect(req.getContextPath() + "/DashboardServlet");
            }
            return;
        }

        // ── Action: Remove from Watchlist ─────────────────────────────────────
        if ("remove".equals(action) && itemIdStr != null) {
            try {
                int itemId = Integer.parseInt(itemIdStr);
                watchlistDAO.removeFromWatchlist(userId, itemId);

                // Agar referrer se aya hai (item page se), toh wapas item page bhejo
                // Otherwise watchlist page pe redirect karo
                String ref = req.getParameter("ref");
                if ("item".equals(ref)) {
                    res.sendRedirect(req.getContextPath() +
                        "/BidServlet?itemId=" + itemId + "&watchRemoved=1");
                } else {
                    res.sendRedirect(req.getContextPath() + "/WatchlistServlet");
                }
            } catch (NumberFormatException e) {
                res.sendRedirect(req.getContextPath() + "/WatchlistServlet");
            }
            return;
        }

        // ── Koi action nahi → User ki Watchlist dikhao ───────────────────────
        List<Watchlist> watchlist = watchlistDAO.getWatchlistByUser(userId);
        req.setAttribute("watchlist", watchlist);
        req.getRequestDispatcher("/WEB-INF/views/watchlist.jsp").forward(req, res);
    }
}
