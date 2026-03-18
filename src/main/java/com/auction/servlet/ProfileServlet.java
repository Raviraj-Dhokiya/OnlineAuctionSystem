package com.auction.servlet;

import com.auction.dao.BidDAO;
import com.auction.dao.UserDAO;
import com.auction.dao.WinnerDAO;
import com.auction.model.User;
import com.auction.security.SecurityUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * ════════════════════════════════════════════════════════
 *  ProfileServlet.java — User Profile Page
 * ════════════════════════════════════════════════════════
 *
 * YEH SERVLET KYA KARTA HAI?
 *   1. GET  → User ka profile page dikhata hai (profile info + bid history + wins)
 *   2. POST → User apni profile update karta hai (fullName, phone)
 *             Optional: password change bhi kar sakta hai
 *
 * URL: /ProfileServlet
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/ProfileServlet")
public class ProfileServlet extends HttpServlet {

    private final UserDAO   userDAO   = new UserDAO();
    private final BidDAO    bidDAO    = new BidDAO();
    private final WinnerDAO winnerDAO = new WinnerDAO();

    /**
     * GET — Profile page data load karo
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("loggedUser");

        // Fresh data DB se (session me purana ho sakta hai)
        User freshUser = userDAO.findById(user.getUserId());
        if (freshUser != null) user = freshUser;

        // Bid history + wins load karo
        req.setAttribute("profileUser",  user);
        req.setAttribute("myBids",       bidDAO.getBidsByUser(user.getUserId()));
        req.setAttribute("myWins",       winnerDAO.getWinsByUser(user.getUserId()));

        // Total stats
        int totalBids = bidDAO.getBidsByUser(user.getUserId()).size();
        int totalWins = winnerDAO.getWinsByUser(user.getUserId()).size();
        req.setAttribute("totalBids", totalBids);
        req.setAttribute("totalWins", totalWins);

        req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, res);
    }

    /**
     * POST — Profile update karo (name, phone, optional password)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User sessionUser = (User) session.getAttribute("loggedUser");

        String fullName = SecurityUtil.sanitizeInput(req.getParameter("fullName"));
        String phone    = SecurityUtil.sanitizeInput(req.getParameter("phone"));
        String newPass  = req.getParameter("newPassword");
        String confPass = req.getParameter("confirmPassword");

        // Validation
        if (fullName == null || fullName.trim().isEmpty()) {
            req.setAttribute("error", "Full name khali nahi ho sakta.");
            doGet(req, res);
            return;
        }

        // Profile update karo (name + phone)
        User updatedUser = userDAO.findById(sessionUser.getUserId());
        updatedUser.setFullName(fullName.trim());
        updatedUser.setPhone(phone != null ? phone.trim() : "");
        boolean profileUpdated = userDAO.updateProfile(updatedUser);

        // Password change (optional - sirf agar user ne bhara ho)
        boolean passUpdated = true;
        if (newPass != null && !newPass.trim().isEmpty()) {
            if (!newPass.equals(confPass)) {
                req.setAttribute("error", "Dono passwords match nahi karte.");
                doGet(req, res);
                return;
            }
            if (!SecurityUtil.isStrongPassword(newPass)) {
                req.setAttribute("error",
                    "Password mein kam se kam 8 characters, ek digit, ek uppercase aur ek special character hona chahiye.");
                doGet(req, res);
                return;
            }
            passUpdated = userDAO.updatePassword(sessionUser.getUserId(),
                                                  SecurityUtil.hashPassword(newPass));
        }

        if (profileUpdated && passUpdated) {
            // Session mein bhi update karo
            updatedUser = userDAO.findById(sessionUser.getUserId());
            session.setAttribute("loggedUser", updatedUser);
            session.setAttribute("username",   updatedUser.getUsername());

            req.setAttribute("success", "Profile successfully update ho gayi!");
        } else {
            req.setAttribute("error", "Profile update karne mein error aaya. Please try again.");
        }

        doGet(req, res);
    }
}
