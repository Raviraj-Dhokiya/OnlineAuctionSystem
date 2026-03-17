package com.auction.model;

import java.sql.Timestamp;

/**
 * ════════════════════════════════════════════════════════
 *  Watchlist.java — Model Class
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   - Database ke WATCHLIST table ki ek row ko represent karti hai.
 *   - User jab koi auction item "watch" karta hai (save karta hai future mein
 *     dekhne ke liye), toh us entry ko yeh class represent karti hai.
 *
 * DB TABLE: watchlist
 *   watch_id  → Primary Key
 *   user_id   → kaun watch kar raha hai (FK → users)
 *   item_id   → kaun sa item watch kar raha hai (FK → auction_items)
 *   added_at  → kab watchlist mein dala
 *
 * EXTRA JOINED FIELDS (DB JOIN se aate hain):
 *   itemTitle       → auction_items.title
 *   itemCurrentPrice → auction_items.current_price
 *   itemStatus      → auction_items.status
 *   itemEndTime     → auction_items.end_time
 * ════════════════════════════════════════════════════════
 */
public class Watchlist {

    private int       watchId;           // DB: watch_id (Primary Key)
    private int       userId;            // DB: user_id (kaun)
    private int       itemId;            // DB: item_id (kaun sa item)
    private Timestamp addedAt;           // DB: added_at

    // ── JOIN se aane wale extra fields ───────────────────────────────────────
    private String    itemTitle;         // auction_items.title
    private double    itemCurrentPrice;  // auction_items.current_price
    private String    itemStatus;        // auction_items.status
    private Timestamp itemEndTime;       // auction_items.end_time
    private String    itemImageName;     // auction_items.image_name

    // Default constructor
    public Watchlist() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int       getWatchId()                    { return watchId; }
    public void      setWatchId(int watchId)         { this.watchId = watchId; }

    public int       getUserId()                     { return userId; }
    public void      setUserId(int userId)           { this.userId = userId; }

    public int       getItemId()                     { return itemId; }
    public void      setItemId(int itemId)           { this.itemId = itemId; }

    public Timestamp getAddedAt()                    { return addedAt; }
    public void      setAddedAt(Timestamp addedAt)   { this.addedAt = addedAt; }

    public String    getItemTitle()                          { return itemTitle; }
    public void      setItemTitle(String itemTitle)          { this.itemTitle = itemTitle; }

    public double    getItemCurrentPrice()                   { return itemCurrentPrice; }
    public void      setItemCurrentPrice(double p)           { this.itemCurrentPrice = p; }

    public String    getItemStatus()                         { return itemStatus; }
    public void      setItemStatus(String itemStatus)        { this.itemStatus = itemStatus; }

    public Timestamp getItemEndTime()                        { return itemEndTime; }
    public void      setItemEndTime(Timestamp itemEndTime)   { this.itemEndTime = itemEndTime; }

    public String    getItemImageName()                      { return itemImageName; }
    public void      setItemImageName(String n)              { this.itemImageName = n; }

    @Override
    public String toString() {
        return "Watchlist{watchId=" + watchId + ", userId=" + userId +
               ", itemId=" + itemId + ", item='" + itemTitle + "'}";
    }
}
