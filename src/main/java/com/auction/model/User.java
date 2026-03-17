package com.auction.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * ════════════════════════════════════════════════════════
 *  User.java — Model Class (Data Blueprint)
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   - Database ke USERS table ka ek row ko Java object mein represent karti hai.
 *   - Jaise DB mein ek user ki row hoti hai, waise hi yahan ek User object
 *     uske saare data ko hold karta hai.
 *
 * KAHAN USE HOTI HAI?
 *   - LoginServlet  → user object session mein store karta hai
 *   - UserDAO       → DB se data padh kar User object bha deta hai
 *   - JSP pages     → ${loggedUser.username} jaise print karte hain
 *
 * ROLE ke do values hain:
 *   - "BIDDER" → normal user (bid laga sakta hai)
 *   - "ADMIN"  → admin user (sab manage kar sakta hai)
 * ════════════════════════════════════════════════════════
 */
public class User implements Serializable {

    // Serializable isliye hai taaki User object ko Session mein save/restore kar sakein
    private static final long serialVersionUID = 1L;

    // ── DB ke USERS table ke columns ke barabar fields ──────────────────────
    private int       userId;        // DB: user_id (Primary Key, auto-generated)
    private String    username;      // DB: username (unique, login ke liye)
    private String    email;         // DB: email (unique)
    private String    password;      // DB: password (BCrypt hashed — plain text NAHI)
    private String    fullName;      // DB: full_name
    private String    phone;         // DB: phone (optional)
    private String    role;          // DB: role → "BIDDER" ya "ADMIN"
    private boolean   isActive;      // DB: is_active → 1=active, 0=blocked
    private Timestamp createdAt;     // DB: created_at (account banane ka time)

    // ── Constructors ─────────────────────────────────────────────────────────

    // Khaali constructor: DAO jab ResultSet se data fill karta hai tab use hota hai
    public User() {}

    // New registration ke time use hota hai (role "BIDDER" by default)
    public User(String username, String email, String password, String fullName) {
        this.username = username;
        this.email    = email;
        this.password = password;   // Note: already BCrypt hashed hona chahiye
        this.fullName = fullName;
        this.role     = "BIDDER";   // Naye user ka role hamesha BIDDER hota hai
        this.isActive = true;       // Naya account active hota hai by default
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    // Getters: field ki value bahar dete hain (JSP mein ${user.userId} se call hota hai)
    // Setters: field mein value set karte hain (DAO DB se data fill karta hai)

    public int       getUserId()    { return userId; }
    public void      setUserId(int userId) { this.userId = userId; }

    public String    getUsername()  { return username; }
    public void      setUsername(String username) { this.username = username; }

    public String    getEmail()     { return email; }
    public void      setEmail(String email) { this.email = email; }

    public String    getPassword()  { return password; }
    public void      setPassword(String password) { this.password = password; }

    public String    getFullName()  { return fullName; }
    public void      setFullName(String fullName) { this.fullName = fullName; }

    public String    getPhone()     { return phone; }
    public void      setPhone(String phone) { this.phone = phone; }

    public String    getRole()      { return role; }
    public void      setRole(String role) { this.role = role; }

    public boolean   isActive()     { return isActive; }
    public void      setActive(boolean active) { isActive = active; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void      setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // ── Helper Methods ────────────────────────────────────────────────────────

    // Admin check: role "ADMIN" hai toh true return karta hai
    // Use: LoginServlet mein → if (user.isAdmin()) → AdminServlet redirect
    public boolean isAdmin() {
        return "ADMIN".equals(this.role);
    }

    // Debug ke liye: console mein print hota hai
    @Override
    public String toString() {
        return "User{id=" + userId + ", username='" + username + "', role='" + role + "'}";
    }
}
