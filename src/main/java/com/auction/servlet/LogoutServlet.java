package com.auction.servlet;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * ════════════════════════════════════════════════════════
 *  LogoutServlet.java — Logout Handler
 * ════════════════════════════════════════════════════════
 *
 * YEH SERVLET KYA KARTA HAI?
 *   Simple: User ka session destroy karta hai aur Login page par bhejta hai.
 *
 * URL: /LogoutServlet
 *
 * SESSION INVALIDATE KYA HOTA HAI?
 *   - session.invalidate() → Session mein stored sab data (loggedUser, role etc.)
 *     ek baar mein hata deta hai.
 *   - Iske baad user ko kisi bhi protected page par jaane ki permission nahi.
 *   - AuthFilter automatically Login page par redirect karega.
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {

    /**
     * GET Request — Logout karo.
     * "Logout" button click hone par yahan aata hai.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Existing session lo (false = naya session mat banao)
        HttpSession session = req.getSession(false);

        // Admin session tha kya? — invalidate karne se pehle check karo
        boolean wasAdmin = (session != null &&
                            Boolean.TRUE.equals(session.getAttribute("adminLoggedIn")));

        if (session != null) {
            // Session exist karti hai → sab data mita do (loggedUser, role, adminLoggedIn etc.)
            session.invalidate();
        }

        // Admin logout → AdminLoginServlet, normal user → LoginServlet
        if (wasAdmin) {
            res.sendRedirect(req.getContextPath() + "/AdminLoginServlet");
        } else {
            res.sendRedirect(req.getContextPath() + "/LoginServlet");
        }
    }
}
