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
 *  RegisterServlet.java — New User Registration Handler
 * ════════════════════════════════════════════════════════
 *
 * YEH SERVLET KYA KARTA HAI?
 *   Naye user ka registration form dikhata hai aur process karta hai.
 *
 * URL: /RegisterServlet
 *
 * FLOW (doPost - form submit hone par):
 *   1. Form se saara data nikalo
 *   2. Validations:
 *      a) Phone compulsory hai?
 *      b) Password aur confirm match kare?
 *      c) Password strong hai? (8+ char, uppercase, digit, special)
 *      d) Email format sahi hai?
 *      e) Username already taken toh nahi?
 *      f) Email already registered toh nahi?
 *   3. Sab pass? → Password BCrypt hash karo → DB mein save karo
 *   4. Success → Login page par "Registration successful" message
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    /**
     * GET Request — Registration form dikhao.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res);
    }

    /**
     * POST Request — Registration form submit hone par yahan aata hai.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Form se saara data nikalo + sanitize karo (XSS prevention)
        String username  = SecurityUtil.sanitizeInput(req.getParameter("username"));
        String email     = SecurityUtil.sanitizeInput(req.getParameter("email"));
        String password  = req.getParameter("password");          // password sanitize mat karo
        String confirm   = req.getParameter("confirmPassword");   // confirm password
        String fullName  = SecurityUtil.sanitizeInput(req.getParameter("fullName"));
        String phone     = SecurityUtil.sanitizeInput(req.getParameter("phone"));

        // ── Validation 0: Phone compulsory ────────────────────────────────────
        if (phone == null || phone.trim().isEmpty()) {
            req.setAttribute("error", "Phone number is compulsory.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res);
            return;
        }

        // MEDIUM BUG #3 FIX: Username validation — sirf alphanumeric aur underscore allow karo
        // Spaces, special chars, bahut chhota ya bada username reject karo.
        if (username == null || !username.matches("[a-zA-Z0-9_]{3,30}")) {
            req.setAttribute("error",
                "Username must be 3-30 characters and can only contain letters, numbers, and underscores.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res);
            return;
        }

        // ── Validation 1: Password aur Confirm Password match kare? ──────────────────
        if (!password.equals(confirm)) {
            req.setAttribute("error", "Passwords do not match.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res);
            return;
        }

        // ── Validation 2: Password strong hai? ───────────────────────────────
        // SecurityUtil.isStrongPassword() → 8+ chars, uppercase, digit, special char
        if (!SecurityUtil.isStrongPassword(password)) {
            req.setAttribute("error",
                "Password must be 8+ chars with uppercase, digit, and special character.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res);
            return;
        }

        // ── Validation 3: Email format sahi hai? ─────────────────────────────
        if (!SecurityUtil.isValidEmail(email)) {
            req.setAttribute("error", "Please enter a valid email address.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res);
            return;
        }

        // ── Validation 4: Username already exist karta hai? ──────────────────
        // DB mein SELECT COUNT(*) query run hoti hai
        if (userDAO.usernameExists(username)) {
            req.setAttribute("error", "Username already taken. Choose another.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res);
            return;
        }

        // ── Validation 5: Email already registered hai? ──────────────────────
        if (userDAO.emailExists(email)) {
            req.setAttribute("error", "Email already registered.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res);
            return;
        }

        // ── Sab validations pass! → User object banao ────────────────────────
        // SecurityUtil.hashPassword() → plain password ka BCrypt hash banao
        // (kabhi bhi plain text DB mein save nahi hota)
        User user = new User(username, email, SecurityUtil.hashPassword(password), fullName);
        user.setPhone(phone);

        // ── DB mein save karo ─────────────────────────────────────────────────
        // userDAO.registerUser() → DB mein INSERT query run karta hai
        if (userDAO.registerUser(user)) {
            // Success → Login page par jaao aur success message dikhao
            req.setAttribute("success", "Registration successful! Please login.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res);
        } else {
            // DB error → wapas register page
            req.setAttribute("error", "Registration failed. Please try again.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res);
        }
    }
}
