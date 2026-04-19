package com.auction.servlet;

import com.auction.dao.*;
import com.auction.model.*;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

/**
 * ════════════════════════════════════════════════════════
 *  AdminServlet.java — Admin Panel Handler (v2)
 * ════════════════════════════════════════════════════════
 *
 * SESSION CHECK: "adminLoggedIn" = true  (AdminLoginServlet set karta hai)
 *
 * GET  (no action)         → Admin dashboard dikhao
 * GET  ?action=exportBidsCsv → CSV download
 * POST ?action=closeAuction  → Auction manually close
 * POST ?action=toggleUser    → User block/unblock
 * POST ?action=updatePayment → Payment status update
 * POST ?action=deleteBid     → Single bid delete karo
 * POST ?action=deleteItem    → Auction item + sab bids delete karo
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/AdminServlet")
public class AdminServlet extends HttpServlet {

    private final UserDAO        userDAO   = new UserDAO();
    private final AuctionItemDAO itemDAO   = new AuctionItemDAO();
    private final WinnerDAO      winnerDAO = new WinnerDAO();
    private final BidDAO         bidDAO    = new BidDAO();

    // ── Session guard ─────────────────────────────────────────────────────────
    private boolean isAdminLoggedIn(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"));
    }

    /**
     * GET — dashboard + CSV export
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Admin session check
        if (!isAdminLoggedIn(req)) {
            res.sendRedirect(req.getContextPath() + "/AdminLoginServlet");
            return;
        }

        String action = req.getParameter("action");

        // ── CSV Export ───────────────────────────────────────────────────────
        if ("exportBidsCsv".equals(action)) {
            int itemId;
            try {
                itemId = Integer.parseInt(req.getParameter("itemId"));
            } catch (NumberFormatException e) {
                res.sendRedirect(req.getContextPath() + "/AdminServlet");
                return;
            }

            AuctionItem targetItem = itemDAO.findById(itemId);
            String itemTitle = (targetItem != null) ? targetItem.getTitle() : "item_" + itemId;
            List<Bid> bids = bidDAO.getBidsForItem(itemId);

            String safeTitle = itemTitle.replaceAll("[^a-zA-Z0-9_-]", "_");
            String dateStr   = new SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
            String fileName  = "bids_" + safeTitle + "_" + dateStr + ".csv";

            res.setContentType("text/csv;charset=UTF-8");
            res.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            PrintWriter pw = res.getWriter();
            pw.println("Bid ID,Bidder,Bid Amount (INR),Bid Time,Is Winning");
            SimpleDateFormat dateFmt = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
            for (Bid b : bids) {
                String bidTimeStr = (b.getBidTime() != null) ? dateFmt.format(b.getBidTime()) : "";
                pw.println(
                    b.getBidId() + "," +
                    csvVal(b.getBidderName()) + "," +
                    String.format("%.2f", b.getBidAmount()) + "," +
                    csvVal(bidTimeStr) + "," +
                    (b.isWinning() ? "YES" : "NO")
                );
            }
            pw.flush();
            return;
        }

        // ── Admin Dashboard ──────────────────────────────────────────────────
        // CSRF token generate karo
        HttpSession session = req.getSession(true);
        String csrfToken = UUID.randomUUID().toString();
        session.setAttribute("adminCsrfToken", csrfToken);
        req.setAttribute("csrfToken", csrfToken);

        req.setAttribute("allUsers",   userDAO.getAllUsers());
        req.setAttribute("allItems",   itemDAO.getAllItems());
        req.setAttribute("allWinners", winnerDAO.getAllWinners());
        req.setAttribute("allBids",    bidDAO.getAllBids());

        // Counts for quick summary
        long activeCount = itemDAO.getAllItems().stream()
            .filter(i -> "ACTIVE".equals(i.getStatus())).count();
        long closedCount = itemDAO.getAllItems().stream()
            .filter(i -> "CLOSED".equals(i.getStatus())).count();
        req.setAttribute("activeAuctionCount", activeCount);
        req.setAttribute("closedAuctionCount", closedCount);

        String msg = req.getParameter("msg");
        if (msg != null) req.setAttribute("msg", msg);

        req.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(req, res);
    }

    /**
     * POST — State-changing actions with CSRF protection
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Admin session check
        if (!isAdminLoggedIn(req)) {
            res.sendRedirect(req.getContextPath() + "/AdminLoginServlet");
            return;
        }

        // ── CSRF Verification ────────────────────────────────────────────────
        HttpSession session = req.getSession(false);
        if (session == null) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Session expired.");
            return;
        }

        String formToken = req.getParameter("csrfToken");
        String sessToken = (String) session.getAttribute("adminCsrfToken");

        if (formToken == null || !formToken.equals(sessToken)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token.");
            return;
        }
        session.removeAttribute("adminCsrfToken");

        String action = req.getParameter("action");

        // ── Close Auction ────────────────────────────────────────────────────
        if ("closeAuction".equals(action)) {
            try {
                int itemId = Integer.parseInt(req.getParameter("itemId"));
                itemDAO.closeAuctionAndDetermineWinner(itemId);
                res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=auction_closed");
            } catch (NumberFormatException e) {
                res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=error_invalid_id");
            }
            return;
        }

        // ── Toggle User block/unblock ─────────────────────────────────────────
        if ("toggleUser".equals(action)) {
            try {
                int    userId = Integer.parseInt(req.getParameter("userId"));
                String active = req.getParameter("active");
                userDAO.setActiveStatus(userId, "true".equals(active));
                res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=user_updated");
            } catch (NumberFormatException e) {
                res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=error_invalid_id");
            }
            return;
        }

        // ── Update Payment Status ────────────────────────────────────────────
        if ("updatePayment".equals(action)) {
            try {
                int    winnerId = Integer.parseInt(req.getParameter("winnerId"));
                String status   = req.getParameter("status");
                winnerDAO.updatePaymentStatus(winnerId, status);
                res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=payment_updated");
            } catch (NumberFormatException e) {
                res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=error_invalid_id");
            }
            return;
        }

        // ── Delete Bid ───────────────────────────────────────────────────────
        if ("deleteBid".equals(action)) {
            try {
                int bidId = Integer.parseInt(req.getParameter("bidId"));
                boolean deleted = bidDAO.deleteBid(bidId);
                res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=" +
                    (deleted ? "bid_deleted" : "error_bid_not_found"));
            } catch (NumberFormatException e) {
                res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=error_invalid_id");
            }
            return;
        }

        // ── Delete Auction Item (+ its bids + its winners) ───────────────────
        if ("deleteItem".equals(action)) {
            try {
                int itemId = Integer.parseInt(req.getParameter("itemId"));
                // Order matters: winners → bids → item  (foreign key constraints)
                winnerDAO.deleteWinnersForItem(itemId);
                bidDAO.deleteBidsForItem(itemId);
                boolean deleted = itemDAO.deleteItem(itemId);
                res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=" +
                    (deleted ? "item_deleted" : "error_item_not_found"));
            } catch (NumberFormatException e) {
                res.sendRedirect(req.getContextPath() + "/AdminServlet?msg=error_invalid_id");
            }
            return;
        }

        res.sendRedirect(req.getContextPath() + "/AdminServlet");
    }

    // ── CSV Helper ────────────────────────────────────────────────────────────
    private String csvVal(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
