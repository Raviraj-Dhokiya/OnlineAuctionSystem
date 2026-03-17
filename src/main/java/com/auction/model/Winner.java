package com.auction.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * ════════════════════════════════════════════════════════
 *  Winner.java — Model Class
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   - Database ke WINNERS table ki ek row represent karti hai.
 *   - Jab koi auction close hoti hai, Oracle Stored Procedure
 *     sabse zyada bid lagane wale user ko winner declare karta hai
 *     aur WINNERS table mein record insert karta hai.
 *
 * PAYMENT STATUS ke 3 values:
 *   - "PENDING" → Winner ne abhi tak payment nahi ki
 *   - "PAID"    → Payment complete
 *   - "FAILED"  → Payment fail ho gayi
 *
 * KAHAN USE HOTA HAI?
 *   - BidServlet      → item-detail.jsp mein winner dikhata hai
 *   - DashboardServlet → "Mere wins" section
 *   - AdminServlet    → Admin payment status update karta hai
 *   - WinnerDAO       → DB se winner data la kar is object mein fill karta hai
 * ════════════════════════════════════════════════════════
 */
public class Winner implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── DB ke WINNERS table ke columns ───────────────────────────────────────
    private int       winnerId;        // DB: winner_id (Primary Key)
    private int       itemId;          // DB: item_id (kaun si auction)
    private int       userId;          // DB: user_id (kaun jeet a — users.user_id)
    private double    winningAmount;   // DB: winning_amount (jeetne wali bid ki amount)
    private String    paymentStatus;   // DB: payment_status → PENDING / PAID / FAILED
    private Timestamp awardedAt;       // DB: awarded_at (kab winner declare hua)

    // ── Joined fields — DB JOIN se milte hain ────────────────────────────────
    private String    winnerName;      // users.username
    private String    winnerEmail;     // users.email (email notification ke liye)
    private String    itemTitle;       // auction_items.title (display ke liye)

    // Khaali constructor: DAO jab DB se data fill karta hai
    public Winner() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public int       getWinnerId()              { return winnerId; }
    public void      setWinnerId(int id)        { this.winnerId = id; }

    public int       getItemId()                { return itemId; }
    public void      setItemId(int id)          { this.itemId = id; }

    public int       getUserId()                { return userId; }
    public void      setUserId(int id)          { this.userId = id; }

    public double    getWinningAmount()         { return winningAmount; }
    public void      setWinningAmount(double a) { this.winningAmount = a; }

    public String    getPaymentStatus()         { return paymentStatus; }
    public void      setPaymentStatus(String s) { this.paymentStatus = s; }

    public Timestamp getAwardedAt()             { return awardedAt; }
    public void      setAwardedAt(Timestamp t)  { this.awardedAt = t; }

    // Winner ka naam (users table se join karke aata hai)
    public String    getWinnerName()            { return winnerName; }
    public void      setWinnerName(String n)    { this.winnerName = n; }

    // Winner ki email (AuctionExpiryChecker se winner email bhejne ke liye use hota hai)
    public String    getWinnerEmail()           { return winnerEmail; }
    public void      setWinnerEmail(String e)   { this.winnerEmail = e; }

    // Auction item ka title (display ke liye)
    public String    getItemTitle()             { return itemTitle; }
    public void      setItemTitle(String t)     { this.itemTitle = t; }
}
