package com.auction.servlet;

import com.auction.util.DBConnection;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

/**
 * ════════════════════════════════════════════════════════
 *  ChatPollServlet.java — Chat Messages Fetch (HTTP Polling)
 * ════════════════════════════════════════════════════════
 *
 * YEH SERVLET KYA KARTA HAI?
 *   Browser har 2 seconds mein yahan GET request karta hai taaki
 *   naye chat messages mil sakein (HTTP Short Polling technique).
 *
 *   Browser mein raw TCP sockets nahi chalte, isliye AuctionChatServer
 *   ke bajaaye hum messages ko DB mein store karte hain aur yahan se
 *   JSON mein laate hain.
 *
 * URL: GET /ChatPollServlet?itemId=5&since=42
 *   itemId → Kis auction ka chat?
 *   since  → Sirf msg_id > since wale naye messages do
 *             (0 = pehli baar, already loaded messages dobara mat bhejo)
 *
 * RESPONSE FORMAT (JSON):
 *   { "messages": [
 *       { "msgId": 43, "sender": "john", "content": "...", "sentAt": "..." },
 *       ...
 *   ]}
 *
 * DB TABLE: messages (msg_id, item_id, sender_id, content, sent_at)
 *           JOIN users ON sender_id = user_id → username milta hai
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/ChatPollServlet")
public class ChatPollServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Only logged-in users can poll
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        res.setContentType("application/json;charset=UTF-8");

        // Parameters nikalo
        int itemId = 0;
        int since  = 0;
        try {
            itemId = Integer.parseInt(req.getParameter("itemId"));
            String sinceStr = req.getParameter("since");
            if (sinceStr != null) since = Integer.parseInt(sinceStr);
        } catch (NumberFormatException e) {
            res.getWriter().write("{\"messages\":[]}");
            return;
        }

        // DB se naye messages lao (msg_id > since, is item ke)
        // JOIN users se username (sender) milta hai
        String sql = "SELECT m.msg_id, u.username AS sender, m.content, " +
                     "TO_CHAR(m.sent_at, 'YYYY-MM-DD\"T\"HH24:MI:SS') AS sent_at " +
                     "FROM messages m " +
                     "JOIN users u ON m.sender_id = u.user_id " +
                     "WHERE m.item_id = ? AND m.msg_id > ? " +
                     "ORDER BY m.msg_id ASC";

        StringBuilder json = new StringBuilder("{\"messages\":[");
        boolean first = true;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            ps.setInt(2, since);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!first) json.append(",");
                    first = false;

                    int    msgId   = rs.getInt("msg_id");
                    String sender  = rs.getString("sender");
                    String content = rs.getString("content");
                    String sentAt  = rs.getString("sent_at");

                    // JSON mein escape karo (special chars handle karo)
                    json.append("{")
                        .append("\"msgId\":").append(msgId).append(",")
                        .append("\"sender\":\"").append(escJson(sender)).append("\",")
                        .append("\"content\":\"").append(escJson(content)).append("\",")
                        .append("\"sentAt\":\"").append(escJson(sentAt)).append("\"")
                        .append("}");
                }
            }

        } catch (SQLException e) {
            System.err.println("[ChatPollServlet] DB error: " + e.getMessage());
            res.getWriter().write("{\"messages\":[],\"error\":\"db_error\"}");
            return;
        }

        json.append("]}");
        res.getWriter().write(json.toString());
    }

    /** JSON string mein special characters escape karo */
    private String escJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
