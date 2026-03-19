package com.auction.servlet;

import com.auction.dao.WinnerDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * ════════════════════════════════════════════════════════
 *  PaymentServlet.java — Mock Payment Process
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/PaymentServlet")
public class PaymentServlet extends HttpServlet {

    private WinnerDAO winnerDAO;

    @Override
    public void init() throws ServletException {
        // Initialize DAO
        winnerDAO = new WinnerDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // Get parameters
        String winnerIdStr = request.getParameter("winnerId");
        String amountStr = request.getParameter("amount");
        String title = request.getParameter("title");

        if (winnerIdStr == null || amountStr == null || title == null) {
            response.sendRedirect(request.getContextPath() + "/DashboardServlet");
            return;
        }

        request.setAttribute("winnerId", winnerIdStr);
        request.setAttribute("amount", amountStr);
        request.setAttribute("title", title);

        // Forward to mock payment page
        request.getRequestDispatcher("/WEB-INF/views/payment.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String winnerIdStr = request.getParameter("winnerId");
        if (winnerIdStr != null) {
            try {
                int winnerId = Integer.parseInt(winnerIdStr);
                // Update payment status to 'PAID'
                boolean updated = winnerDAO.updatePaymentStatus(winnerId, "PAID");
                if (updated) {
                    session.setAttribute("successMsg", "Payment successful! Your order has been placed.");
                } else {
                    session.setAttribute("errorMsg", "Payment failed. Please try again.");
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        // Redirect back to dashboard
        response.sendRedirect(request.getContextPath() + "/DashboardServlet");
    }
}
