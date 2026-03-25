package com.auction.dao;

import com.auction.model.AuctionItem;
import com.auction.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 *  AuctionItemDAO.java — Auction Items Database Operations
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   - `auction_items` table par sabhi database operations (CRUD).
 *
 * SIKHNE LAYAK CONCEPTS (JDBC - Unit 4):
 *   1. PreparedStatement: SQL Injection rokne ke liye (?, ?, ? parameters)
 *   2. BLOB (Binary Large Object): Images ko byte array mein save/read karna
 *   3. CallableStatement: Oracle Stored Procedure call karna (winner decide karne ke liye)
 *   4. ResultSet & executeQuery(): Database se data lane ke liye
 *   5. executeUpdate(): INSERT/UPDATE queries chalane ke liye
 * ════════════════════════════════════════════════════════
 */
public class AuctionItemDAO {

    // ── Constructor ───────────────────────────────────────────────────────────────
    // BEKAR #7 FIX: DDL (CREATE TABLE, CREATE SEQUENCE) ab DAO constructor mein
    // nahi hai. Yeh database_schema.sql mein properly define hai. DAO ka kaam
    // sirf DML (SELECT/INSERT/UPDATE/DELETE) hai, DDL nahi.
    public AuctionItemDAO() {}

    /**
     * addAdditionalImages() — Extra gallery images save karo.
     *
     * BUG #3 FIX: Pehle MAX(image_id)+? use hota tha — concurrent batch execution
     * mein saari rows ek hi MAX value paati thi → duplicate PK violation.
     * FIX: item_images_seq Oracle SEQUENCE use karo (defined in database_schema.sql).
     * NEXTVAL hamesha unique number deta hai, even in concurrent inserts.
     */
    public void addAdditionalImages(int itemId, List<byte[]> imageDataList, List<String> imageNames) {
        if (imageDataList == null || imageDataList.isEmpty()) return;

        // BUG #3 FIX: item_images_seq.NEXTVAL se unique PK milta hai (race-condition safe)
        String sql = "INSERT INTO item_images (image_id, item_id, image_data, image_name) " +
                     "VALUES (item_images_seq.NEXTVAL, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < imageDataList.size(); i++) {
                ps.setInt(1, itemId);
                ps.setBytes(2, imageDataList.get(i));
                ps.setString(3, imageNames.get(i));
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            System.err.println("[AuctionItemDAO] addAdditionalImages error: " + e.getMessage());
        }
    }

    public List<Integer> getAdditionalImageIds(int itemId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT image_id FROM item_images WHERE item_id = ? ORDER BY image_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("image_id"));
            }
        } catch (SQLException e) {
            System.err.println("[AuctionItemDAO] getAdditionalImageIds error: " + e.getMessage());
        }
        return ids;
    }

    public byte[] getSpecificImage(int imageId) {
        String sql = "SELECT image_data FROM item_images WHERE image_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, imageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBytes("image_data");
            }
        } catch (SQLException e) {
            System.err.println("[AuctionItemDAO] getSpecificImage error: " + e.getMessage());
        }
        return null;
    }

    // ── CREATE ───────────────────────────────────────────────────────────────

    /**
     * addItem() — Naya auction item DB mein insert karta hai.
     * CALL: AuctionItemServlet.doPost() se
     * KHAAS BAAT: new String[]{"ITEM_ID"} se insert hone ke baad jo auto-generate
     * hone wali ID (Primary Key) hai, wo wapas milti hai (GeneratedKeys).
     */
    public int addItem(AuctionItem item) {
        String sql = "INSERT INTO auction_items " +
                     "(title, description, category, starting_price, current_price, " +
                     "reserve_price, image_data, image_name, seller_id, start_time, end_time) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     new String[]{"ITEM_ID"})) {

            ps.setString(1, item.getTitle());
            ps.setString(2, item.getDescription());
            ps.setString(3, item.getCategory());
            ps.setDouble(4, item.getStartingPrice());
            ps.setDouble(5, item.getStartingPrice()); // current = starting initially
            ps.setDouble(6, item.getReservePrice());

            // BLOB image save karna (Unit 4)
            // Agar user ne photo di hai, toh setBytes use karo
            if (item.getImageData() != null) {
                ps.setBytes(7, item.getImageData());
                ps.setString(8, item.getImageName());
            } else {
                // Photo nahi di toh NULL value set karo database mein
                ps.setNull(7, Types.BLOB);
                ps.setNull(8, Types.VARCHAR);
            }

            ps.setInt(9, item.getSellerId());
            ps.setTimestamp(10, item.getStartTime());
            ps.setTimestamp(11, item.getEndTime());

            ps.executeUpdate();
            // BUG #2 FIX: GeneratedKeys ResultSet kabhi close nahi hoti thi.
            // try-with-resources se automatically close hogi.
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("[AuctionItemDAO] addItem error: " + e.getMessage());
        }
        return -1;
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    public AuctionItem findById(int itemId) {
        String sql = "SELECT i.*, u.username AS seller_name " +
                     "FROM auction_items i JOIN users u ON i.seller_id = u.user_id " +
                     "WHERE i.item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapItem(rs, true); // include BLOB
            }

        } catch (SQLException e) {
            System.err.println("[AuctionItemDAO] findById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all ACTIVE items (for homepage listing) - without BLOB for speed
     * NOTE: This excludes expired items (end_time > NOW). Do NOT use in ExpiryChecker!
     * Use getExpiredActiveItems() for closing auctions.
     */
    public List<AuctionItem> getActiveItems() {
        String sql = "SELECT i.item_id, i.title, i.description, i.category, " +
                     "i.starting_price, i.current_price, i.reserve_price, " +
                     "i.image_name, i.seller_id, i.status, i.start_time, i.end_time, " +
                     "i.created_at, u.username AS seller_name " +
                     "FROM auction_items i JOIN users u ON i.seller_id = u.user_id " +
                     "WHERE i.status = 'ACTIVE' AND i.end_time > CURRENT_TIMESTAMP " +
                     "ORDER BY i.end_time ASC";
        return queryItems(sql);
    }

    /**
     * ═══════════════════════════════════════════════════════════════
     * THE KEY FIX FOR "WIN NOT SHOWING IN ACCOUNT":
     *
     * ExpiryChecker pehle getActiveItems() use karta tha, jis mein
     * "AND end_time > CURRENT_TIMESTAMP" filter tha.
     * Matlab: expired auctions is query mein AAYI HI NAHI thi!
     * ExpiryChecker ko kuch milta hi nahi tha → determine_winner() kabhi
     * nahi chali → winners table empty rahi → Dashboard mein kuch nahi dikha.
     *
     * YEH METHOD sirf expired-but-not-yet-closed items laata hai:
     *   status = 'ACTIVE' → abhi bhi ACTIVE (ExpiryChecker ne close nahi kiya)
     *   end_time <= NOW   → lekin end time beet chuka hai
     *
     * ExpiryChecker ab is method ko use karega. ✅
     * ═══════════════════════════════════════════════════════════════
     */
    public List<AuctionItem> getExpiredActiveItems() {
        String sql = "SELECT i.item_id, i.title, i.description, i.category, " +
                     "i.starting_price, i.current_price, i.reserve_price, " +
                     "i.image_name, i.seller_id, i.status, i.start_time, i.end_time, " +
                     "i.created_at, u.username AS seller_name " +
                     "FROM auction_items i JOIN users u ON i.seller_id = u.user_id " +
                     "WHERE i.status = 'ACTIVE' AND i.end_time <= CURRENT_TIMESTAMP " +
                     "ORDER BY i.end_time ASC";
        return queryItems(sql);
    }

    /**
     * Get all items - Admin view
     */
    public List<AuctionItem> getAllItems() {
        String sql = "SELECT i.item_id, i.title, i.description, i.category, " +
                     "i.starting_price, i.current_price, i.reserve_price, " +
                     "i.image_name, i.seller_id, i.status, i.start_time, i.end_time, " +
                     "i.created_at, u.username AS seller_name " +
                     "FROM auction_items i JOIN users u ON i.seller_id = u.user_id " +
                     "ORDER BY i.created_at DESC";
        return queryItems(sql);
    }

    /**
     * Get items listed by a specific seller
     */
    public List<AuctionItem> getItemsBySeller(int sellerId) {
        String sql = "SELECT i.item_id, i.title, i.description, i.category, " +
                     "i.starting_price, i.current_price, i.reserve_price, " +
                     "i.image_name, i.seller_id, i.status, i.start_time, i.end_time, " +
                     "i.created_at, u.username AS seller_name " +
                     "FROM auction_items i JOIN users u ON i.seller_id = u.user_id " +
                     "WHERE i.seller_id = ? ORDER BY i.created_at DESC";
        List<AuctionItem> items = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) items.add(mapItem(rs, false));
            }
        } catch (SQLException e) {
            System.err.println("[AuctionItemDAO] getItemsBySeller error: " + e.getMessage());
        }
        return items;
    }

    /**
     * Search items by keyword
     */
    public List<AuctionItem> searchItems(String keyword) {
        String sql = "SELECT i.item_id, i.title, i.description, i.category, " +
                     "i.starting_price, i.current_price, i.reserve_price, " +
                     "i.image_name, i.seller_id, i.status, i.start_time, i.end_time, " +
                     "i.created_at, u.username AS seller_name " +
                     "FROM auction_items i JOIN users u ON i.seller_id = u.user_id " +
                     "WHERE i.status='ACTIVE' AND " +
                     "(UPPER(i.title) LIKE UPPER(?) OR UPPER(i.category) LIKE UPPER(?)) " +
                     "ORDER BY i.end_time ASC";
        List<AuctionItem> items = new ArrayList<>();
        String like = "%" + keyword + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) items.add(mapItem(rs, false));
            }
        } catch (SQLException e) {
            System.err.println("[AuctionItemDAO] searchItems error: " + e.getMessage());
        }
        return items;
    }

    /**
     * searchItemsFiltered() — Advanced search with filters.
     *
     * @param keyword   title/description mein search (null = sab)
     * @param category  specific category filter (null = sab categories)
     * @param minPrice  minimum current price (0 = no limit)
     * @param maxPrice  maximum current price (0 = no limit)
     * @param sortBy    "price_asc", "price_desc", "ending_soon", "newest" (null = ending_soon)
     */
    public List<AuctionItem> searchItemsFiltered(String keyword, String category,
                                                  double minPrice, double maxPrice,
                                                  String sortBy) {
        List<AuctionItem> items = new ArrayList<>();

        // Dynamic WHERE clause build karo
        StringBuilder sql = new StringBuilder(
            "SELECT i.item_id, i.title, i.description, i.category, " +
            "i.starting_price, i.current_price, i.reserve_price, " +
            "i.image_name, i.seller_id, i.status, i.start_time, i.end_time, " +
            "i.created_at, u.username AS seller_name " +
            "FROM auction_items i JOIN users u ON i.seller_id = u.user_id " +
            "WHERE i.status='ACTIVE' AND i.end_time > CURRENT_TIMESTAMP"
        );

        // Keyword filter
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (UPPER(i.title) LIKE UPPER(?) OR UPPER(i.description) LIKE UPPER(?))");
        }
        // Category filter
        if (category != null && !category.trim().isEmpty() && !category.equals("ALL")) {
            sql.append(" AND UPPER(i.category) = UPPER(?)");
        }
        // Price range filter
        if (minPrice > 0) sql.append(" AND i.current_price >= ?");
        if (maxPrice > 0) sql.append(" AND i.current_price <= ?");

        // Sort order
        switch (sortBy != null ? sortBy : "ending_soon") {
            case "price_asc":   sql.append(" ORDER BY i.current_price ASC");  break;
            case "price_desc":  sql.append(" ORDER BY i.current_price DESC"); break;
            case "newest":      sql.append(" ORDER BY i.created_at DESC");    break;
            default:            sql.append(" ORDER BY i.end_time ASC");       break; // ending_soon
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            String like = keyword != null ? "%" + keyword.trim() + "%" : null;

            if (like != null) { ps.setString(idx++, like); ps.setString(idx++, like); }
            if (category != null && !category.trim().isEmpty() && !category.equals("ALL")) {
                ps.setString(idx++, category.trim());
            }
            if (minPrice > 0) ps.setDouble(idx++, minPrice);
            if (maxPrice > 0) ps.setDouble(idx++, maxPrice);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) items.add(mapItem(rs, false));
            }
        } catch (SQLException e) {
            System.err.println("[AuctionItemDAO] searchItemsFiltered error: " + e.getMessage());
        }
        return items;
    }

    /**
     * Get distinct categories from active auction items (for filter dropdown)
     * UPDATE: Ab user ko hamesha saari default categories dikhegi dropdown me
     */
    public List<String> getActiveCategories() {
        return java.util.Arrays.asList(
            "Electronics", "Vehicles", "Art", "Furniture", "Collectibles", "Music", 
            "Fashion & Accessories", "Real Estate", "Home Appliances", "Sports & Outdoors", 
            "Toys & Hobbies", "Jewelry & Watches", "Antiques", "Books & Comics", "Other"
        );
    }

    /**
     * Get bid count for an item (for displaying on auction cards)
     */
    public int getBidCount(int itemId) {
        String sql = "SELECT COUNT(*) FROM bids WHERE item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[AuctionItemDAO] getBidCount error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieve BLOB image bytes for a given item
     * (Database se binary format mein photo read karna)
     * CALL: AuctionItemServlet.doGet(?action=image) - Browser pe photo dikhane ke liye
     */
    public byte[] getItemImage(int itemId) {
        String sql = "SELECT image_data FROM auction_items WHERE item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Blob blob = rs.getBlob("image_data");
                    if (blob != null) return blob.getBytes(1, (int) blob.length()); // BLOB → byte array
                }
            }
        } catch (SQLException e) {
            System.err.println("[AuctionItemDAO] getItemImage error: " + e.getMessage());
        }
        return null;
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    /**
     * Update current_price after a new bid is placed
     */
    public boolean updateCurrentPrice(int itemId, double newPrice) {
        String sql = "UPDATE auction_items SET current_price=? WHERE item_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newPrice);
            ps.setInt(2, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AuctionItemDAO] updateCurrentPrice error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update item status
     */
    public boolean updateStatus(int itemId, String status) {
        String sql = "UPDATE auction_items SET status=? WHERE item_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AuctionItemDAO] updateStatus error: " + e.getMessage());
            return false;
        }
    }

    // ── STORED PROCEDURE (Unit 4 - CallableStatement) ────────────────────────

    /**
     * Oracle Stored Procedure ko Java se call karna.
     * Stored Procedure ka kaam: Yeh DB mein hi script chalata hai jo check karta hai
     * ki sabse badi bid kiski hai, aur use winner declare kar deta hai.
     * Java ko alag se queries nahi likhni padti, bas proceduce ko call (CALL) karna hota hai.
     */
    public void closeAuctionAndDetermineWinner(int itemId) {
        String call = "{ CALL determine_winner(?) }"; // ? = itemId parameter
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(call)) { // prepareCall use hota hai Procedures ke liye

            cs.setInt(1, itemId);
            cs.execute();
            System.out.println("[AuctionItemDAO] Winner determined for item #" + itemId);

        } catch (SQLException e) {
            System.err.println("[AuctionItemDAO] closeAuction error: " + e.getMessage());
        }
    }

    // ── HELPER ───────────────────────────────────────────────────────────────

    // FIX: Statement → PreparedStatement (best practice consistency, even without user input)
    private List<AuctionItem> queryItems(String sql) {
        List<AuctionItem> items = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) items.add(mapItem(rs, false));
        } catch (SQLException e) {
            System.err.println("[AuctionItemDAO] queryItems error: " + e.getMessage());
        }
        return items;
    }

    private AuctionItem mapItem(ResultSet rs, boolean includeBlob) throws SQLException {
        AuctionItem item = new AuctionItem();
        item.setItemId(rs.getInt("item_id"));
        item.setTitle(rs.getString("title"));
        item.setDescription(rs.getString("description"));
        item.setCategory(rs.getString("category"));
        item.setStartingPrice(rs.getDouble("starting_price"));
        item.setCurrentPrice(rs.getDouble("current_price"));
        item.setReservePrice(rs.getDouble("reserve_price"));
        item.setImageName(rs.getString("image_name"));
        item.setSellerId(rs.getInt("seller_id"));
        item.setStatus(rs.getString("status"));
        item.setStartTime(rs.getTimestamp("start_time"));
        item.setEndTime(rs.getTimestamp("end_time"));
        item.setCreatedAt(rs.getTimestamp("created_at"));

        // BLOB - load only when needed (Unit 4)
        if (includeBlob) {
            try {
                Blob blob = rs.getBlob("image_data");
                if (blob != null) item.setImageData(blob.getBytes(1, (int) blob.length()));
            } catch (Exception ignored) {}
        }

        // Joined field
        try { item.setSellerName(rs.getString("seller_name")); }
        catch (Exception ignored) {}

        return item;
    }
}
