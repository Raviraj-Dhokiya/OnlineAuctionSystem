package com.auction.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * ════════════════════════════════════════════════════════
 *  AutoBid.java — Model Class
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   - Database ke AUTO_BIDS table ki ek row ko represent karti hai.
 *   - Jab user "Auto Bid" enable karta hai, uski max limit aur target
 *     is object mein store hoti hai.
 *
 * AUTO BID LOGIC:
 *   - Jab koi dusra user bid kare, 30 seconds baad automatically
 *     current price + 10% ki bid automatically lag jati hai.
 *   - Agar naya auto-bid amount > maxTarget ho, toh auto bid BAND ho jata hai.
 *
 * KAHAN USE HOTA HAI?
 *   - AutoBidServlet     → naya AutoBid save/fetch karna
 *   - AutoBidDAO         → DB operations
 *   - AutoBidProcessor   → 30-second delay ke baad bid trigger karna
 * ════════════════════════════════════════════════════════
 */
public class AutoBid implements Serializable {

    private static final long serialVersionUID = 1L;

    private int       autoBidId;    // DB: auto_bid_id (Primary Key)
    private int       itemId;       // DB: item_id (kis item ke liye)
    private int       userId;       // DB: user_id (kisne set kiya)
    private double    maxTarget;    // DB: max_target (yahan tak auto bid chalegi)
    private boolean   isActive;     // DB: is_active (1=chalti hai, 0=band ho gayi)
    private Timestamp createdAt;    // DB: created_at

    // Joined fields
    private String    username;     // users.username (display ke liye)

    public AutoBid() {}

    public AutoBid(int itemId, int userId, double maxTarget) {
        this.itemId    = itemId;
        this.userId    = userId;
        this.maxTarget = maxTarget;
        this.isActive  = true;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public int       getAutoBidId()              { return autoBidId; }
    public void      setAutoBidId(int id)        { this.autoBidId = id; }

    public int       getItemId()                 { return itemId; }
    public void      setItemId(int id)           { this.itemId = id; }

    public int       getUserId()                 { return userId; }
    public void      setUserId(int id)           { this.userId = id; }

    public double    getMaxTarget()              { return maxTarget; }
    public void      setMaxTarget(double t)      { this.maxTarget = t; }

    public boolean   isActive()                  { return isActive; }
    public void      setActive(boolean a)        { this.isActive = a; }

    public Timestamp getCreatedAt()              { return createdAt; }
    public void      setCreatedAt(Timestamp t)   { this.createdAt = t; }

    public String    getUsername()               { return username; }
    public void      setUsername(String u)       { this.username = u; }

    @Override
    public String toString() {
        return "AutoBid{id=" + autoBidId + ", item=" + itemId +
               ", user=" + userId + ", maxTarget=" + maxTarget +
               ", active=" + isActive + "}";
    }
}
