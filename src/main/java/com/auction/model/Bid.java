package com.auction.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * ════════════════════════════════════════════════════════
 *  Bid.java — Model Class
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   - Database ke BIDS table ki ek row ko represent karti hai.
 *   - Jab bhi koi user kisi auction par bid lagata hai, ek Bid object
 *     banta hai aur DB mein save hota hai.
 *
 * KAHAN USE HOTA HAI?
 *   - BidServlet      → naya Bid object bana ke BidDAO.placeBid() ko deta hai
 *   - BidDAO          → DB se bid data la kar Bid object fill karta hai
 *   - BidPollServlet  → JavaScript ko bid updates JSON mein bhejne ke liye
 *   - dashboard.jsp   → "Mere bids" section mein dikhata hai
 * ════════════════════════════════════════════════════════
 */
public class Bid implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── DB ke BIDS table ke columns ───────────────────────────────────────────
    private int       bidId;       // DB: bid_id (Primary Key, auto-generated)
    private int       itemId;      // DB: item_id (kis item par bid lagi)
    private int       bidderId;    // DB: bidder_id (kisne bid lagayi → users.user_id)
    private double    bidAmount;   // DB: bid_amount (kitna bid lagaya)
    private Timestamp bidTime;     // DB: bid_time (kab bid lagayi gayi)
    private boolean   isWinning;   // DB: is_winning → 1 if yeh sabse zyada bid hai

    // ── Joined fields — DB se JOIN karke milte hain, alag columns nahi hain ──
    private String    bidderName;  // users.username (display ke liye)
    private String    itemTitle;   // auction_items.title (dashboard mein dikhane ke liye)

    // ── Constructors ──────────────────────────────────────────────────────────

    // Khaali constructor: DAO jab DB se data fill karta hai
    public Bid() {}

    // BidServlet jab naya bid place karta hai tab yeh use hota hai
    // (bidId aur bidTime DB auto-generate karta hai)
    public Bid(int itemId, int bidderId, double bidAmount) {
        this.itemId    = itemId;
        this.bidderId  = bidderId;
        this.bidAmount = bidAmount;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public int       getBidId()             { return bidId; }
    public void      setBidId(int id)       { this.bidId = id; }

    public int       getItemId()            { return itemId; }
    public void      setItemId(int id)      { this.itemId = id; }

    public int       getBidderId()          { return bidderId; }
    public void      setBidderId(int id)    { this.bidderId = id; }

    public double    getBidAmount()         { return bidAmount; }
    public void      setBidAmount(double a) { this.bidAmount = a; }

    public Timestamp getBidTime()           { return bidTime; }
    public void      setBidTime(Timestamp t){ this.bidTime = t; }

    // isWinning: abhi tak is item par yeh sabse badi bid hai
    public boolean   isWinning()            { return isWinning; }
    public void      setWinning(boolean w)  { this.isWinning = w; }

    public String    getBidderName()        { return bidderName; }
    public void      setBidderName(String n){ this.bidderName = n; }

    public String    getItemTitle()         { return itemTitle; }
    public void      setItemTitle(String t) { this.itemTitle = t; }

    @Override
    public String toString() {
        return "Bid{id=" + bidId + ", item=" + itemId +
               ", bidder=" + bidderId + ", amount=" + bidAmount + "}";
    }
}
