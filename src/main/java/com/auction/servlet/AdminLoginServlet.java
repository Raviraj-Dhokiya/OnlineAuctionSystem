package com.auction.servlet;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * ════════════════════════════════════════════════════════
 *  AdminLoginServlet.java — Dedicated Admin Login Handler
 * ════════════════════════════════════════════════════════
 *
 * Yeh servlet admin ke liye ALAG login handle karta hai.
 * Credentials hardcoded hain — koi DB query nahi.
 *
 * URL:  /AdminLoginServlet
 * GET  → Admin login page dikhao
 * POST → Credentials verify karo → AdminServlet redirect
 *
 * ADMIN CREDENTIALS:
 *   Username: Admin223
 *   Password: Ravi@2026
 *
 * SESSION KEY: "adminLoggedIn" = true  (alag regular user session se)
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/AdminLoginServlet")
public class AdminLoginServlet extends HttpServlet {

    // ── Hardcoded Admin Credentials ─────────────────────────────────────
    private static final String ADMIN_USERNAME = "Admin223";
    private static final String ADMIN_PASSWORD = "Ravi@2026";

    /**
     * GET → Admin login page dikhao
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Agar admin pehle se logged in hai → seedha AdminServlet bhejo
        HttpSession session = req.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            res.sendRedirect(req.getContextPath() + "/AdminServlet");
            return;
        }

        req.getRequestDispatcher("/WEB-INF/views/admin-login.jsp").forward(req, res);
    }

    /**
     * POST → Credentials check karo
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        // ── Validate credentials ─────────────────────────────────────────
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            // ✅ Correct — admin session banao
            HttpSession session = req.getSession(true);
            session.setAttribute("adminLoggedIn", Boolean.TRUE);
            session.setAttribute("adminUsername", ADMIN_USERNAME);
            session.setMaxInactiveInterval(60 * 60); // 1 hour

            res.sendRedirect(req.getContextPath() + "/AdminServlet");
        } else {
            // ❌ Wrong credentials
            req.setAttribute("error", "Invalid admin credentials. Please try again.");
            req.getRequestDispatcher("/WEB-INF/views/admin-login.jsp").forward(req, res);
        }
    }
}
