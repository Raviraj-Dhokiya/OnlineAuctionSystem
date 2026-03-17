package com.auction.servlet;

import com.auction.model.User;
import com.auction.util.DBConnection;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

/**
 * ════════════════════════════════════════════════════════
 *  ChatSendServlet.java — Chat Message Save karna
 * ════════════════════════════════════════════════════════
 *
 * YEH SERVLET KYA KARTA HAI?
 *   Browser se POST aata hai jab user chat mein message bhejta hai.
 *   Message ko `messages` table mein INSERT karta hai.
 *
 * URL: POST /ChatSendServlet
 *   Body params:
 *     itemId  → Kis auction ka chat?
 *     content → Message text
 *
 * RESPONSE (JSON):
 *   { "ok": true, "msgId": 45 }   → Success
 *   { "ok": false, "error": "..." } → Failure
 *
 * SECURITY:
 *   - Session check: Sirf logged-in user message bhej sakta hai
 *   - Content length check: 500 chars max (DB VARCHAR2(500) limit)
 *   - Empty content reject
 *
 * DB TABLE: messages
 *   INSERT INTO messages (item_id, sender_id, content) VALUES (?, ?, ?)
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/ChatSendServlet")
public class ChatSendServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json;charset=UTF-8");

        // ── Auth check ────────────────────────────────────────────────────────
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"ok\":false,\"error\":\"not_logged_in\"}");
            return;
        }

        User loggedUser = (User) session.getAttribute("loggedUser");
        int  senderId   = loggedUser.getUserId();

        // ── Parameter extraction ──────────────────────────────────────────────
        int itemId = 0;
        try {
            itemId = Integer.parseInt(req.getParameter("itemId"));
        } catch (NumberFormatException e) {
            res.getWriter().write("{\"ok\":false,\"error\":\"invalid_item\"}");
            return;
        }

        String content = req.getParameter("content");

        // ── Validation ────────────────────────────────────────────────────────
        if (content == null || content.trim().isEmpty()) {
            res.getWriter().write("{\"ok\":false,\"error\":\"empty_message\"}");
            return;
        }
        content = content.trim();
        if (content.length() > 500) {
            content = content.substring(0, 500); // Truncate to DB limit
        }

        // ── DB Insert ─────────────────────────────────────────────────────────
        String sql = "INSERT INTO messages (item_id, sender_id, content) VALUES (?, ?, ?)";

        // Oracle mein IDENTITY column se generated msg_id wapas milega
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"MSG_ID"})) {

            ps.setInt(1, itemId);
            ps.setInt(2, senderId);
            ps.setString(3, content);
            ps.executeUpdate();

            int newMsgId = -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) newMsgId = keys.getInt(1);
            }

            res.getWriter().write("{\"ok\":true,\"msgId\":" + newMsgId + "}");

        } catch (SQLException e) {
            System.err.println("[ChatSendServlet] DB error: " + e.getMessage());
            res.getWriter().write("{\"ok\":false,\"error\":\"db_error\"}");
        }
    }
}
