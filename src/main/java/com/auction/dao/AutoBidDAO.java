package com.auction.dao;

import com.auction.model.AutoBid;
import com.auction.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AutoBidDAO — AUTO_BIDS table operations
 * KEY FIX: Uses Oracle MERGE (upsert) so enable->cancel->re-enable
 * never fails due to UNIQUE(item_id, user_id) constraint violation.
 */
public class AutoBidDAO {

    /**
     * UPSERT auto bid using Oracle MERGE.
     * Works whether the record exists (active or inactive) or not.
     * @return true = saved, false = error
     */
    public boolean saveAutoBid(AutoBid autoBid) {
        // Oracle MERGE: UPDATE if row exists (any is_active), INSERT if not
        String sql = "MERGE INTO auto_bids ab " +
                     "USING (SELECT ? AS item_id, ? AS user_id FROM dual) src " +
                     "ON (ab.item_id = src.item_id AND ab.user_id = src.user_id) " +
                     "WHEN MATCHED THEN " +
                     "  UPDATE SET ab.max_target = ?, ab.is_active = 1 " +
                     "WHEN NOT MATCHED THEN " +
                     "  INSERT (item_id, user_id, max_target, is_active) " +
                     "  VALUES (?, ?, ?, 1)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, autoBid.getItemId());       // USING src.item_id
            ps.setInt(2, autoBid.getUserId());        // USING src.user_id
            ps.setDouble(3, autoBid.getMaxTarget());  // UPDATE max_target
            ps.setInt(4, autoBid.getItemId());        // INSERT item_id
            ps.setInt(5, autoBid.getUserId());        // INSERT user_id
            ps.setDouble(6, autoBid.getMaxTarget());  // INSERT max_target

            int rows = ps.executeUpdate();
            System.out.println("[AutoBidDAO] MERGE auto bid: rows=" + rows +
                " user=" + autoBid.getUserId() + " item=" + autoBid.getItemId() +
                " target=" + autoBid.getMaxTarget());
            return true;

        } catch (SQLException e) {
            System.err.println("[AutoBidDAO] saveAutoBid MERGE error: " + e.getMessage());
            System.err.println("[AutoBidDAO]   SQLState=" + e.getSQLState() +
                "  ErrorCode=" + e.getErrorCode());
            System.err.println("[AutoBidDAO] >>> Make sure auto_bids table exists!" +
                " Run add_auto_bids_table.sql in Oracle SQL Developer.");
            return false;
        }
    }

    /**
     * Get ACTIVE auto bid for a specific user on a specific item.
     */
    public AutoBid getActiveAutoBid(int itemId, int userId) {
        String sql = "SELECT ab.*, u.username FROM auto_bids ab " +
                     "JOIN users u ON ab.user_id = u.user_id " +
                     "WHERE ab.item_id=? AND ab.user_id=? AND ab.is_active=1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapAutoBid(rs);
            }
        } catch (SQLException e) {
            System.err.println("[AutoBidDAO] getActiveAutoBid error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get ALL active auto bids for an item (used by AutoBidProcessorServlet).
     * Ordered by max_target DESC so highest-willing bidder gets priority.
     */
    public List<AutoBid> getAllActiveForItem(int itemId) {
        List<AutoBid> list = new ArrayList<>();
        String sql = "SELECT ab.*, u.username FROM auto_bids ab " +
                     "JOIN users u ON ab.user_id = u.user_id " +
                     "WHERE ab.item_id=? AND ab.is_active=1 " +
                     "ORDER BY ab.max_target DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapAutoBid(rs));
            }
        } catch (SQLException e) {
            System.err.println("[AutoBidDAO] getAllActiveForItem error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Deactivate auto bid (target reached or manual cancel).
     */
    public void deactivateAutoBid(int itemId, int userId) {
        String sql = "UPDATE auto_bids SET is_active=0 WHERE item_id=? AND user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            System.out.println("[AutoBidDAO] Deactivated auto bid user=" + userId +
                " item=" + itemId + " rows=" + rows);
        } catch (SQLException e) {
            System.err.println("[AutoBidDAO] deactivateAutoBid error: " + e.getMessage());
        }
    }

    /**
     * Check if user has active auto bid for this item.
     */
    public boolean hasActiveAutoBid(int itemId, int userId) {
        return getActiveAutoBid(itemId, userId) != null;
    }

    // ── HELPER ───────────────────────────────────────────────────────────────
    private AutoBid mapAutoBid(ResultSet rs) throws SQLException {
        AutoBid ab = new AutoBid();
        ab.setAutoBidId(rs.getInt("auto_bid_id"));
        ab.setItemId(rs.getInt("item_id"));
        ab.setUserId(rs.getInt("user_id"));
        ab.setMaxTarget(rs.getDouble("max_target"));
        ab.setActive(rs.getInt("is_active") == 1);
        ab.setCreatedAt(rs.getTimestamp("created_at"));
        try { ab.setUsername(rs.getString("username")); } catch (Exception ignored) {}
        return ab;
    }
}
