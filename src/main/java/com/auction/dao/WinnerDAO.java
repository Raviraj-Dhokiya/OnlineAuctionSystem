package com.auction.dao;

import com.auction.model.Winner;
import com.auction.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 *  WinnerDAO.java — Winners Table Database Operations
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   - Winners tables se data padhti (Read) aur update karti hai.
 *   - Note: Naya winner insert karne ka kaam Java se nahi hota,
 *     wo Oracle Stored Procedure (determine_winner) automatically
 *     DB table mein set kar deta hai jab auction close hota hai.
 * ════════════════════════════════════════════════════════
 */
public class WinnerDAO {

    /**
     * Get winner for a specific item
     */
    public Winner getWinnerByItem(int itemId) {
        String sql = "SELECT w.*, u.username AS winner_name, u.email AS winner_email, " +
                     "i.title AS item_title " +
                     "FROM winners w " +
                     "JOIN users u ON w.user_id = u.user_id " +
                     "JOIN auction_items i ON w.item_id = i.item_id " +
                     "WHERE w.item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            // FIX: ResultSet try-with-resources
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapWinner(rs);
            }

        } catch (SQLException e) {
            System.err.println("[WinnerDAO] getWinnerByItem error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all auctions won by a user
     */
    public List<Winner> getWinsByUser(int userId) {
        List<Winner> wins = new ArrayList<>();
        String sql = "SELECT w.*, u.username AS winner_name, u.email AS winner_email, " +
                     "i.title AS item_title " +
                     "FROM winners w " +
                     "JOIN users u ON w.user_id = u.user_id " +
                     "JOIN auction_items i ON w.item_id = i.item_id " +
                     "WHERE w.user_id = ? ORDER BY w.awarded_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            // FIX: ResultSet try-with-resources
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) wins.add(mapWinner(rs));
            }

        } catch (SQLException e) {
            System.err.println("[WinnerDAO] getWinsByUser error: " + e.getMessage());
        }
        return wins;
    }

    /**
     * Get all winners - Admin view
     */
    public List<Winner> getAllWinners() {
        List<Winner> wins = new ArrayList<>();
        String sql = "SELECT w.*, u.username AS winner_name, u.email AS winner_email, " +
                     "i.title AS item_title " +
                     "FROM winners w " +
                     "JOIN users u ON w.user_id = u.user_id " +
                     "JOIN auction_items i ON w.item_id = i.item_id " +
                     "ORDER BY w.awarded_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) wins.add(mapWinner(rs));

        } catch (SQLException e) {
            System.err.println("[WinnerDAO] getAllWinners error: " + e.getMessage());
        }
        return wins;
    }

    /**
     * Update payment status (Admin confirms payment)
     */
    public boolean updatePaymentStatus(int winnerId, String status) {
        String sql = "UPDATE winners SET payment_status=? WHERE winner_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, winnerId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[WinnerDAO] updatePaymentStatus error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete winners record for a specific item — used before deleting an item
     */
    public boolean deleteWinnersForItem(int itemId) {
        String sql = "DELETE FROM winners WHERE item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[WinnerDAO] deleteWinnersForItem error: " + e.getMessage());
            return false;
        }
    }

    // ── HELPER ───────────────────────────────────────────────────────────────

    private Winner mapWinner(ResultSet rs) throws SQLException {
        Winner w = new Winner();
        w.setWinnerId(rs.getInt("winner_id"));
        w.setItemId(rs.getInt("item_id"));
        w.setUserId(rs.getInt("user_id"));
        w.setWinningAmount(rs.getDouble("winning_amount"));
        w.setPaymentStatus(rs.getString("payment_status"));
        w.setAwardedAt(rs.getTimestamp("awarded_at"));
        try { w.setWinnerName(rs.getString("winner_name")); }  catch (Exception ignored) {}
        try { w.setWinnerEmail(rs.getString("winner_email")); } catch (Exception ignored) {}
        try { w.setItemTitle(rs.getString("item_title")); }    catch (Exception ignored) {}
        return w;
    }
}
