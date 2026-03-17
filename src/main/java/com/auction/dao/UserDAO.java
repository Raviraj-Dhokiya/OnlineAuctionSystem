package com.auction.dao;

import com.auction.model.User;
import com.auction.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 *  UserDAO.java — User Table ke liye Database Operations
 * ════════════════════════════════════════════════════════
 *
 * DAO PATTERN KYA HOTA HAI?
 *   DAO = Data Access Object
 *   Ek class jo sirf ek table ke CRUD operations handle kare.
 *   Servlet directly SQL nahi likhta — DAO ke through jata hai.
 *
 * ══ Is DAO ke methods ════════════════════════════════════
 *   CREATE: registerUser()        → naya user INSERT
 *   READ:   findByUsername()      → login ke liye user dhundho
 *           findById()            → user_id se dhundho
 *           findByEmail()         → email se dhundho
 *           getAllUsers()         → admin panel ke liye sab users
 *           usernameExists()      → username liya hua hai?
 *           emailExists()         → email registered hai?
 *   UPDATE: updateProfile()       → naam aur phone update
 *           updatePassword()      → password change
 *           setActiveStatus()     → user block/unblock
 *   EXTRA:  printTableMetaData()  → table ka structure print karo
 * ════════════════════════════════════════════════════════
 *
 * JDBC KAISE KAAM KARTA HAI?
 *   1. DBConnection.getConnection() → Oracle se ek connection pool se lo
 *   2. PreparedStatement banao → SQL query mein ? ke jagah values set karo
 *   3. executeQuery() → SELECT, executeUpdate() → INSERT/UPDATE/DELETE
 *   4. ResultSet → DB ke rows read karo
 *   5. Connection automatically close → pool mein wapas
 * ════════════════════════════════════════════════════════
 */
public class UserDAO {

    // ── CREATE ──────────────────────────────────────────────────────────────

    /**
     * registerUser() — Naya user DB mein save karo.
     *
     * CALL: RegisterServlet.doPost() → yahan aata hai
     * SQL:  INSERT INTO users (username, email, password, ...) VALUES (?,?,?,...)
     *
     * PreparedStatement kyun? → SQL Injection attack rokne ke liye.
     * (? = placeholder → values set karo, SQL inject nahi ho sakta)
     *
     * @return true = success, false = duplicate email/username ya DB error
     */
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, email, password, full_name, phone, role) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getRole() != null ? user.getRole() : "BIDDER");

            return ps.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("[UserDAO] Duplicate username/email: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.err.println("[UserDAO] registerUser error: " + e.getMessage());
            return false;
        }
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    /**
     * findByUsername() — Login ke liye username se user dhundho.
     *
     * CALL: LoginServlet.doPost() → username DB mein check karo
     * SQL:  SELECT * FROM users WHERE username=? AND is_active=1
     *       (sirf active users, blocked nahi)
     *
     * @return User object agar mila, null agar nahi mila
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return mapUser(rs);

        } catch (SQLException e) {
            System.err.println("[UserDAO] findByUsername error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Find user by ID
     */
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return mapUser(rs);

        } catch (SQLException e) {
            System.err.println("[UserDAO] findById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Find user by email (for forgot password / mail)
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ? AND is_active = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return mapUser(rs);

        } catch (SQLException e) {
            System.err.println("[UserDAO] findByEmail error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all users (Admin panel)
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) users.add(mapUser(rs));

        } catch (SQLException e) {
            System.err.println("[UserDAO] getAllUsers error: " + e.getMessage());
        }
        return users;
    }

    /**
     * Check if username is already taken
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] usernameExists error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check if email is already registered
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] emailExists error: " + e.getMessage());
        }
        return false;
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    /**
     * Update user profile (full name, phone)
     */
    public boolean updateProfile(User user) {
        String sql = "UPDATE users SET full_name=?, phone=? WHERE user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getPhone());
            ps.setInt(3, user.getUserId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] updateProfile error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update password (BCrypt hash)
     */
    public boolean updatePassword(int userId, String hashedPassword) {
        String sql = "UPDATE users SET password=? WHERE user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] updatePassword error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Activate or deactivate a user (Admin)
     */
    public boolean setActiveStatus(int userId, boolean active) {
        String sql = "UPDATE users SET is_active=? WHERE user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] setActiveStatus error: " + e.getMessage());
            return false;
        }
    }

    // ── METADATA (Unit 4 - MetaData APIs) ───────────────────────────────────

    /**
     * Print column metadata of users table (Unit 4 - MetaData)
     */
    public void printTableMetaData() {
        String sql = "SELECT * FROM users WHERE ROWNUM = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            System.out.println("\n=== USERS Table Metadata ===");
            for (int i = 1; i <= cols; i++) {
                System.out.printf("Col %d: %-20s | Type: %-15s | Nullable: %s%n",
                    i,
                    meta.getColumnName(i),
                    meta.getColumnTypeName(i),
                    meta.isNullable(i) == ResultSetMetaData.columnNullable ? "YES" : "NO");
            }
            DatabaseMetaData dbMeta = conn.getMetaData();
            System.out.println("DB: " + dbMeta.getDatabaseProductName() +
                               " v" + dbMeta.getDatabaseProductVersion());

        } catch (SQLException e) {
            System.err.println("[UserDAO] metadata error: " + e.getMessage());
        }
    }

    // ── HELPER ───────────────────────────────────────────────────────────────

    /**
     * mapUser() — ResultSet ki current row ko User object mein convert karo.
     *
     * ResultSet ek table ki tarah hota hai. rs.getString("column_name")
     * se specific column ki value milti hai. Yeh sab values User object mein set karo.
     *
     * Yeh method private hai — sirf is class ke andar use hota hai.
     */
    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setFullName(rs.getString("full_name"));
        u.setPhone(rs.getString("phone"));
        u.setRole(rs.getString("role"));
        u.setActive(rs.getInt("is_active") == 1);
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
