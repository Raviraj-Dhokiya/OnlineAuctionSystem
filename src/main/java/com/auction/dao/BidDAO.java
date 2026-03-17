package com.auction.dao;

import com.auction.model.Bid;
import com.auction.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 *  BidDAO.java — Bids Table ke liye Database Operations
 * ════════════════════════════════════════════════════════
 *
 * FIXES APPLIED:
 *   1. Race Condition Fix: SELECT ... FOR UPDATE + end_time check added.
 *      Dono concurrent transactions pehle row lock karengi, doosri wait karegi.
 *   2. PreparedStatement / ResultSet Leaks Fixed:
 *      checkPs, insertPs, updatePs, aur unke ResultSets ab try-with-resources
 *      ke andar hain — exception pe bhi automatically close honge.
 *
 * TRANSACTION KYA HOTA HAI?
 *   placeBid() mein do kaam ek saath karne padte hain:
 *   1. BIDS table mein naya bid INSERT karo
 *   2. AUCTION_ITEMS table mein current_price UPDATE karo
 *   Agar ek successful ho aur doosra fail? → Data inconsistent ho jayega!
 *   Transaction isliye use karte hain:
 *   - setAutoCommit(false) → dono operations ek saath complete honge
 *   - commit()   → dono success → permanently save
 *   - rollback() → koi ek fail → dono cancel
 * ════════════════════════════════════════════════════════
 */
public class BidDAO {

    /**
     * placeBid() — Naya bid DB mein save karo.
     *
     * CALL: BidServlet.doPost() aur AuctionServiceImpl.placeBid() se
     *
     * TRANSACTION STEPS (setAutoCommit(false)):
     *   Step 1: SELECT ... FOR UPDATE → row lock karo (race condition fix)
     *           + end_time check → auction expire ho chuki ho toh reject (timing window fix)
     *   Step 2: Check — kya bidAmount > current_price hai? (warna reject)
     *   Step 3: INSERT INTO bids (item_id, bidder_id, bid_amount)
     *   Step 4: UPDATE auction_items SET current_price = bidAmount
     *   commit() — dono save permanently
     *
     * @return true = bid lagi, false = amount kam tha ya auction nahi mila
     */
    public boolean placeBid(Bid bid) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // BEGIN TRANSACTION

            // ── RACE CONDITION FIX + END_TIME FETCH ─────────────────────────────
            // FOR UPDATE → row lock (concurrent bids safe)
            // end_time bhi fetch karte hain taaki time extension check kar sakein
            String checkSql = "SELECT current_price, end_time FROM auction_items " +
                              "WHERE item_id=? AND status='ACTIVE' AND end_time > CURRENT_TIMESTAMP " +
                              "FOR UPDATE";

            Timestamp currentEndTime = null;

            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, bid.getItemId());

                try (ResultSet rs = checkPs.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false; // item not found, not active, or auction ended
                    }

                    double currentPrice = rs.getDouble("current_price");
                    if (bid.getBidAmount() <= currentPrice) {
                        conn.rollback();
                        return false; // bid too low
                    }

                    // End time save karo — time extension ke liye check karenge
                    currentEndTime = rs.getTimestamp("end_time");
                }
            }

            // INSERT bid
            String insertSql = "INSERT INTO bids (item_id, bidder_id, bid_amount) VALUES (?,?,?)";
            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setInt(1, bid.getItemId());
                insertPs.setInt(2, bid.getBidderId());
                insertPs.setDouble(3, bid.getBidAmount());
                insertPs.executeUpdate();
            }

            // UPDATE current price
            String updateSql = "UPDATE auction_items SET current_price=? WHERE item_id=?";
            try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                updatePs.setDouble(1, bid.getBidAmount());
                updatePs.setInt(2, bid.getItemId());
                updatePs.executeUpdate();
            }

            // ── AUCTION TIME EXTENSION (Real Auction Feature) ────────────────────
            // Agar auction ke end_time se abhi sirf 2 minute (120 sec) bache hain,
            // toh end_time ko 5 minutes (300 sec) aur badha do.
            // Yeh "sniping" rokta hai — last second mein bid karke win karna.
            if (currentEndTime != null) {
                long now          = System.currentTimeMillis();
                long endMillis    = currentEndTime.getTime();
                long remainingMs  = endMillis - now;

                if (remainingMs > 0 && remainingMs <= 2 * 60 * 1000) {
                    // 2 minutes ya kam bacha hai → 5 minutes extend karo
                    Timestamp newEndTime = new Timestamp(endMillis + 5 * 60 * 1000);
                    String extendSql =
                        "UPDATE auction_items SET end_time=? WHERE item_id=?";
                    try (PreparedStatement extPs = conn.prepareStatement(extendSql)) {
                        extPs.setTimestamp(1, newEndTime);
                        extPs.setInt(2, bid.getItemId());
                        extPs.executeUpdate();
                        System.out.println("[BidDAO] Auction #" + bid.getItemId() +
                            " extended by 5 minutes (last 2-min bid rule).");
                    }
                }
            }

            conn.commit(); // COMMIT — bid + price update + optional extension ek saath
            return true;

        } catch (SQLException e) {
            System.err.println("[BidDAO] placeBid error: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            return false;
        } finally {
            DBConnection.close(conn);
        }
    }

    /**
     * Get all bids for an item, highest first
     */
    public List<Bid> getBidsForItem(int itemId) {
        List<Bid> bids = new ArrayList<>();
        String sql = "SELECT b.*, u.username AS bidder_name " +
                     "FROM bids b JOIN users u ON b.bidder_id = u.user_id " +
                     "WHERE b.item_id = ? ORDER BY b.bid_amount DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) bids.add(mapBid(rs));
            }

        } catch (SQLException e) {
            System.err.println("[BidDAO] getBidsForItem error: " + e.getMessage());
        }
        return bids;
    }

    /**
     * Get all bids placed by a user
     */
    public List<Bid> getBidsByUser(int userId) {
        List<Bid> bids = new ArrayList<>();
        String sql = "SELECT b.*, u.username AS bidder_name, i.title AS item_title " +
                     "FROM bids b " +
                     "JOIN users u ON b.bidder_id = u.user_id " +
                     "JOIN auction_items i ON b.item_id = i.item_id " +
                     "WHERE b.bidder_id = ? ORDER BY b.bid_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Bid bid = mapBid(rs);
                    bid.setItemTitle(rs.getString("item_title"));
                    bids.add(bid);
                }
            }

        } catch (SQLException e) {
            System.err.println("[BidDAO] getBidsByUser error: " + e.getMessage());
        }
        return bids;
    }

    /**
     * Get the highest bid for an item
     */
    public Bid getHighestBid(int itemId) {
        String sql = "SELECT b.*, u.username AS bidder_name " +
                     "FROM bids b JOIN users u ON b.bidder_id = u.user_id " +
                     "WHERE b.item_id = ? " +
                     "ORDER BY b.bid_amount DESC FETCH FIRST 1 ROWS ONLY";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBid(rs);
            }

        } catch (SQLException e) {
            System.err.println("[BidDAO] getHighestBid error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Total number of bids on an item
     */
    public int getBidCount(int itemId) {
        String sql = "SELECT COUNT(*) FROM bids WHERE item_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[BidDAO] getBidCount error: " + e.getMessage());
        }
        return 0;
    }

    // ── HELPER ───────────────────────────────────────────────────────────────

    private Bid mapBid(ResultSet rs) throws SQLException {
        Bid b = new Bid();
        b.setBidId(rs.getInt("bid_id"));
        b.setItemId(rs.getInt("item_id"));
        b.setBidderId(rs.getInt("bidder_id"));
        b.setBidAmount(rs.getDouble("bid_amount"));
        b.setBidTime(rs.getTimestamp("bid_time"));
        b.setWinning(rs.getInt("is_winning") == 1);
        try { b.setBidderName(rs.getString("bidder_name")); } catch (Exception ignored) {}
        return b;
    }
}
