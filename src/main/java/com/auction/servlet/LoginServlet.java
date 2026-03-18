package com.auction.servlet;

import com.auction.dao.UserDAO;
import com.auction.model.User;
import com.auction.security.SecurityUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * ════════════════════════════════════════════════════════
 *  LoginServlet.java — Login Page Handler
 * ════════════════════════════════════════════════════════
 *
 * YEH SERVLET KYA KARTA HAI?
 *   Login page dikhata hai aur login process handle karta hai.
 *
 * URL: /LoginServlet
 *
 * DO METHODS HAIN:
 *   doGet()  → Login form dikhata hai (jab URL open karein)
 *   doPost() → Login form submit hone par validate karta hai
 *
 * FLOW (doPost):
 *   1. User username + password type karke Login click kare
 *   2. Input sanitize karo (XSS prevention)
 *   3. UserDAO se DB mein user dhundho
 *   4. BCrypt se password verify karo
 *   5. Sab sahi? → Session banao → Role ke hisaab se redirect karo
 *      ADMIN  → /AdminServlet
 *      BIDDER → /DashboardServlet
 *   6. Galat? → login.jsp mein error dikhao
 *
 * COOKIES:
 *   "Remember Me" check kiya? → 7 din ka cookie save karo
 *   Next baar login page open hone par username auto-fill hoga
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    // UserDAO: DB mein user dhundhne ke liye (ek baar banao, sab requests mein reuse hoga)
    private final UserDAO userDAO = new UserDAO();

    /**
     * GET Request — Login form dikhao.
     * Jab koi /LoginServlet URL open kare.
     *
     * Extra kaam: Agar "Remember Me" cookie stored hai,
     * toh username field auto-fill karo.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Agar user pehle se logged in hai toh login page ki zarurat nahi
        HttpSession session = req.getSession(false);  // false → naya session mat banao
        if (session != null && session.getAttribute("loggedUser") != null) {
            // Already logged in hai → Dashboard bhejo
            res.sendRedirect(req.getContextPath() + "/DashboardServlet");
            return;
        }



        // Login page dikhao (login.jsp)
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res);
    }

    /**
     * POST Request — Login form submit hone par yahan aata hai.
     * User "Login" button click kare tab.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Form se data nikalo
        // sanitizeInput() → HTML tags hata do (XSS attack prevention)
        String username   = SecurityUtil.sanitizeInput(req.getParameter("username"));
        String password   = req.getParameter("password");   // password sanitize mat karo (special chars hote hain)

        // ── Step 1: Input validation ──────────────────────────────────────────
        if (username == null || username.isEmpty() ||
            password == null || password.isEmpty()) {
            // Koi field khaali hai → error ke saath wapas login page
            req.setAttribute("error", "Username and password are required.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res);
            return;
        }

        // ── Step 2: DB mein user dhundho ──────────────────────────────────────
        // UserDAO Oracle DB mein SELECT karta hai jahan username match kare
        User user = userDAO.findByUsername(username);

        // ── Step 3: Password verify karo ─────────────────────────────────────
        // BCrypt.checkpw(): typed password ko stored hash se compare karta hai
        if (user == null || !SecurityUtil.verifyPassword(password, user.getPassword())) {
            req.setAttribute("error", "Invalid username or password.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res);
            return;
        }

        // ── Step 4: Account active hai? ───────────────────────────────────────
        if (!user.isActive()) {
            // Admin ne account block kiya hua hai
            req.setAttribute("error", "Your account has been deactivated. Contact admin.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res);
            return;
        }

        // ── Step 5: Session banao (login success!) ────────────────────────────
        // getSession(true) → naya session banao
        HttpSession session = req.getSession(true);
        session.setAttribute("loggedUser", user);        // poora User object store karo
        session.setAttribute("userId",     user.getUserId());
        session.setAttribute("username",   user.getUsername());
        session.setAttribute("role",       user.getRole()); // "ADMIN" ya "BIDDER"
        session.setMaxInactiveInterval(30 * 60);             // 30 minutes idle ke baad session khatam

        // ── Step 6: Role ke hisaab se redirect karo ──────────────────────────
        if (user.isAdmin()) {
            res.sendRedirect(req.getContextPath() + "/AdminServlet");  // Admin panel
        } else {
            res.sendRedirect(req.getContextPath() + "/DashboardServlet"); // Normal dashboard
        }
    }
}
