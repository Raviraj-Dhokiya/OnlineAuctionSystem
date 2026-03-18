package com.auction.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * ════════════════════════════════════════════════════════
 *  AuctionItem.java — Model Class
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   - Database ke AUCTION_ITEMS table ki ek row ko represent karti hai.
 *   - Jab koi seller koi item auction pe laata hai, uski saari details
 *     is object mein store hoti hain.
 *
 * STATUS ke 4 values:
 *   - "ACTIVE"    → Auction abhi chal rahi hai, bid lag sakti hai
 *   - "CLOSED"    → Auction khatam, winner decide ho gaya
 *   - "CANCELLED" → Auction cancel kar di gayi
 *   - "PENDING"   → Auction shuru hone wali hai (start_time nahi aayi)
 *
 * KAHAN USE HOTA HAI?
 *   - DashboardServlet → active items list
 *   - BidServlet       → ek specific item ki detail
 *   - AuctionItemDAO   → DB se data la kar is class mein fill karta hai
 * ════════════════════════════════════════════════════════
 */
public class AuctionItem implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── DB ke AUCTION_ITEMS table ke columns ─────────────────────────────────
    private int       itemId;          // DB: item_id (Primary Key)
    private String    title;           // DB: title (item ka naam)
    private String    description;     // DB: description (item ki details)
    private String    category;        // DB: category (jaise "Electronics", "Art")
    private double    startingPrice;   // DB: starting_price (shuru ki lowest price)
    private double    currentPrice;    // DB: current_price (abhi tak ki highest bid)
    private double    reservePrice;    // DB: reserve_price (secret minimum price, optional)
    private byte[]    imageData;       // DB: image_data — BLOB type (image ka binary data)
    private String    imageName;       // DB: image_name (file naam jaise "guitar.jpg")
    private int       sellerId;        // DB: seller_id → users.user_id (kaun bech raha hai)
    private String    sellerName;      // DB se JOIN karke aata hai (users.username)
    private String    status;          // DB: status → ACTIVE / CLOSED / CANCELLED / PENDING
    private Timestamp startTime;       // DB: start_time (kab shuru hogi)
    private Timestamp endTime;         // DB: end_time (kab band hogi)
    private Timestamp createdAt;       // DB: created_at (kab list ki gayi)
    private int       bidCount;        // Transient: bids count (DB column nahi, query se fill hota hai)

    // Khaali constructor: DAO jab DB se data fill karta hai tab use hota hai
    public AuctionItem() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public int       getItemId()        { return itemId; }
    public void      setItemId(int id)  { this.itemId = id; }

    public String    getTitle()         { return title; }
    public void      setTitle(String t) { this.title = t; }

    public String    getDescription()           { return description; }
    public void      setDescription(String d)   { this.description = d; }

    public String    getCategory()              { return category; }
    public void      setCategory(String c)      { this.category = c; }

    public double    getStartingPrice()             { return startingPrice; }
    public void      setStartingPrice(double p)     { this.startingPrice = p; }

    public double    getCurrentPrice()              { return currentPrice; }
    public void      setCurrentPrice(double p)      { this.currentPrice = p; }

    public double    getReservePrice()              { return reservePrice; }
    public void      setReservePrice(double p)      { this.reservePrice = p; }

    // imageData: Item ki photo ka raw binary data (BLOB format mein DB mein store hota hai)
    public byte[]    getImageData()                 { return imageData; }
    public void      setImageData(byte[] data)      { this.imageData = data; }

    public String    getImageName()                 { return imageName; }
    public void      setImageName(String n)         { this.imageName = n; }

    public int       getSellerId()                  { return sellerId; }
    public void      setSellerId(int id)            { this.sellerId = id; }

    // sellerName DB se directly nahi aata — UserDAO/AuctionItemDAO JOIN se milta hai
    public String    getSellerName()                { return sellerName; }
    public void      setSellerName(String n)        { this.sellerName = n; }

    public String    getStatus()                    { return status; }
    public void      setStatus(String s)            { this.status = s; }

    public Timestamp getStartTime()                 { return startTime; }
    public void      setStartTime(Timestamp t)      { this.startTime = t; }

    public Timestamp getEndTime()                   { return endTime; }
    public void      setEndTime(Timestamp t)        { this.endTime = t; }

    public Timestamp getCreatedAt()                 { return createdAt; }
    public void      setCreatedAt(Timestamp t)      { this.createdAt = t; }

    public int       getBidCount()                  { return bidCount; }
    public void      setBidCount(int c)             { this.bidCount = c; }

    // ── Helper Methods ────────────────────────────────────────────────────────

    // Auction "ACTIVE" status mein hai toh true
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    // Auction ki endTime ab tak guzar gayi hai? (matlab auction time over ho gaya)
    // AuctionExpiryChecker yeh check karta hai har 60 seconds mein
    public boolean isEnded() {
        return endTime != null && endTime.before(new java.util.Date());
    }

    @Override
    public String toString() {
        return "AuctionItem{id=" + itemId + ", title='" + title +
               "', currentPrice=" + currentPrice + ", status='" + status + "'}";
    }
}
