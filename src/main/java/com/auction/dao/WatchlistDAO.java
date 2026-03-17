package com.auction.dao;

import com.auction.model.Watchlist;
import com.auction.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 *  WatchlistDAO.java — Watchlist Table ke liye DB Operations
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   User ki watchlist manage karti hai:
 *   - addToWatchlist()      → item ko watchlist mein dalo
 *   - removeFromWatchlist() → item watchlist se hatao
 *   - getWatchlistByUser()  → user ki poori watchlist lao
 *   - isWatching()          → check karo user already watch kar raha hai?
 *
 * DB TABLE: watchlist (user_id + item_id UNIQUE constraint)
 *   Isliye same item dobara add nahi hoga (DB constraint handle karta hai).
 * ════════════════════════════════════════════════════════
 */
public class WatchlistDAO {

    /**
     * addToWatchlist() — Item ko user ki watchlist mein add karo.
     *
     * DB: INSERT INTO watchlist (user_id, item_id) VALUES (?, ?)
     * NOTE: UNIQUE constraint (user_id, item_id) hai — duplicate insert DB level
     *       par reject hogi (SQLException), jo gracefully false return karega.
     *
     * @return true = successfully added, false = already watching ya error
     */
    public boolean addToWatchlist(int userId, int itemId) {
        String sql = "INSERT INTO watchlist (user_id, item_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, itemId);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            // ORA-00001: unique constraint violated (already watching) - ignore silently
            if (e.getErrorCode() != 1) {
                System.err.println("[WatchlistDAO] addToWatchlist error: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * removeFromWatchlist() — Item ko user ki watchlist se hatao.
     *
     * DB: DELETE FROM watchlist WHERE user_id=? AND item_id=?
     *
     * @return true = successfully removed, false = nahi tha ya error
     */
    public boolean removeFromWatchlist(int userId, int itemId) {
        String sql = "DELETE FROM watchlist WHERE user_id=? AND item_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, itemId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[WatchlistDAO] removeFromWatchlist error: " + e.getMessage());
            return false;
        }
    }

    /**
     * getWatchlistByUser() — User ki poori watchlist lao (item details ke saath).
     *
     * DB: JOIN watchlist + auction_items taaki item ki saari info mile.
     *     Items order by added_at DESC (latest added pehle).
     *
     * @return List of Watchlist objects with item details filled
     */
    public List<Watchlist> getWatchlistByUser(int userId) {
        List<Watchlist> list = new ArrayList<>();
        String sql = "SELECT w.watch_id, w.user_id, w.item_id, w.added_at, " +
                     "i.title AS item_title, i.current_price, i.status AS item_status, " +
                     "i.end_time AS item_end_time, i.image_name AS item_image_name " +
                     "FROM watchlist w " +
                     "JOIN auction_items i ON w.item_id = i.item_id " +
                     "WHERE w.user_id = ? " +
                     "ORDER BY w.added_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapWatchlist(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[WatchlistDAO] getWatchlistByUser error: " + e.getMessage());
        }
        return list;
    }

    /**
     * isWatching() — Check karo ki user ne yeh item already watch kiya hua hai.
     *
     * DB: SELECT COUNT(*) FROM watchlist WHERE user_id=? AND item_id=?
     * Item detail page par "Add to Watchlist" / "Remove" button toggle ke liye use hota hai.
     *
     * @return true = user is watching this item
     */
    public boolean isWatching(int userId, int itemId) {
        String sql = "SELECT COUNT(*) FROM watchlist WHERE user_id=? AND item_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("[WatchlistDAO] isWatching error: " + e.getMessage());
        }
        return false;
    }

    /**
     * getWatchlistCount() — Kitne items user ki watchlist mein hain.
     */
    public int getWatchlistCount(int userId) {
        String sql = "SELECT COUNT(*) FROM watchlist WHERE user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[WatchlistDAO] getWatchlistCount error: " + e.getMessage());
        }
        return 0;
    }

    // ── HELPER ───────────────────────────────────────────────────────────────

    private Watchlist mapWatchlist(ResultSet rs) throws SQLException {
        Watchlist w = new Watchlist();
        w.setWatchId(rs.getInt("watch_id"));
        w.setUserId(rs.getInt("user_id"));
        w.setItemId(rs.getInt("item_id"));
        w.setAddedAt(rs.getTimestamp("added_at"));
        w.setItemTitle(rs.getString("item_title"));
        w.setItemCurrentPrice(rs.getDouble("current_price"));
        w.setItemStatus(rs.getString("item_status"));
        w.setItemEndTime(rs.getTimestamp("item_end_time"));
        w.setItemImageName(rs.getString("item_image_name"));
        return w;
    }
}
