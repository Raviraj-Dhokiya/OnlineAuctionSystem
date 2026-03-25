package com.auction.patterns;

import com.auction.model.AuctionItem;
import java.sql.Timestamp;

/**
 * ════════════════════════════════════════════════════════
 *  AuctionItemBuilder.java — Builder Pattern (Unit 8)
 * ════════════════════════════════════════════════════════
 *
 * BUILDER PATTERN KYA HAI?
 *   - Step-by-step complex object banane ka pattern.
 *   - Ek class ke bahut saare optional fields hone par constructor ugly ho jaata hai.
 *   - Builder pattern mein method chaining (fluent API) se readable code likhta hai.
 *
 * PROBLEM WITHOUT BUILDER:
 *   new AuctionItem(1, "Guitar", "desc", "Music", 500.0, 500.0, 600.0, null, null,
 *                   3, "Seller", "ACTIVE", start, end, created, 0);  // ← confusing!
 *
 * SOLUTION WITH BUILDER:
 *   AuctionItem guitar = new AuctionItemBuilder()
 *       .title("Vintage Guitar")
 *       .category("Music")
 *       .startingPrice(5000.0)
 *       .sellerId(3)
 *       .durationDays(7)
 *       .build();
 *
 * UNIT 8 CONCEPT:
 *   - Inner static Builder class with method chaining (fluent interface).
 *   - build() method final object return karta hai.
 *   - Validation build() ke andar hoti hai.
 * ════════════════════════════════════════════════════════
 */
public class AuctionItemBuilder {

    // ── Required fields ───────────────────────────────────────────────────────
    private String    title;
    private double    startingPrice;
    private int       sellerId;

    // ── Optional fields (defaults set kiye hain) ─────────────────────────────
    private String    description   = "";
    private String    category      = "General";
    private double    reservePrice  = 0.0;         // 0 = no reserve
    private String    status        = "ACTIVE";
    private int       durationDays  = 7;           // default 7-day auction
    private Timestamp startTime     = null;        // null = now
    private Timestamp endTime       = null;        // null = startTime + durationDays
    private String    imageName     = null;

    public AuctionItemBuilder() {}

    // ── Fluent Setter Methods (return this for chaining) ──────────────────────

    public AuctionItemBuilder title(String title) {
        this.title = title;
        return this;
    }

    public AuctionItemBuilder description(String description) {
        this.description = description;
        return this;
    }

    public AuctionItemBuilder category(String category) {
        this.category = category;
        return this;
    }

    public AuctionItemBuilder startingPrice(double price) {
        this.startingPrice = price;
        return this;
    }

    public AuctionItemBuilder reservePrice(double reservePrice) {
        this.reservePrice = reservePrice;
        return this;
    }

    public AuctionItemBuilder sellerId(int sellerId) {
        this.sellerId = sellerId;
        return this;
    }

    public AuctionItemBuilder status(String status) {
        this.status = status;
        return this;
    }

    /**
     * Duration in days set karo. build() mein endTime calculate hogi.
     */
    public AuctionItemBuilder durationDays(int days) {
        this.durationDays = days;
        return this;
    }

    public AuctionItemBuilder startTime(Timestamp startTime) {
        this.startTime = startTime;
        return this;
    }

    public AuctionItemBuilder endTime(Timestamp endTime) {
        this.endTime = endTime;
        return this;
    }

    public AuctionItemBuilder imageName(String imageName) {
        this.imageName = imageName;
        return this;
    }

    // ── Build Method ──────────────────────────────────────────────────────────

    /**
     * Final AuctionItem object banao — validation ke baad.
     * @throws IllegalStateException agar required fields missing hain
     */
    public AuctionItem build() {
        // ── Validation ────────────────────────────────────────────────────────
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalStateException("[Builder] title is required!");
        }
        if (startingPrice <= 0) {
            throw new IllegalStateException("[Builder] startingPrice must be > 0!");
        }
        if (sellerId <= 0) {
            throw new IllegalStateException("[Builder] sellerId must be a valid user ID!");
        }

        // ── Defaults calculate karo ───────────────────────────────────────────
        long now = System.currentTimeMillis();
        Timestamp effectiveStart = (startTime != null) ? startTime : new Timestamp(now);
        Timestamp effectiveEnd   = (endTime != null)   ? endTime :
                new Timestamp(effectiveStart.getTime() + (long) durationDays * 24 * 60 * 60 * 1000);

        double effectiveReserve = (reservePrice > 0) ? reservePrice : startingPrice * 1.1;

        // ── Build the object ──────────────────────────────────────────────────
        AuctionItem item = new AuctionItem();
        item.setTitle(title.trim());
        item.setDescription(description);
        item.setCategory(category);
        item.setStartingPrice(startingPrice);
        item.setCurrentPrice(startingPrice);        // current = starting for new item
        item.setReservePrice(effectiveReserve);
        item.setSellerId(sellerId);
        item.setStatus(status);
        item.setStartTime(effectiveStart);
        item.setEndTime(effectiveEnd);
        item.setCreatedAt(new Timestamp(now));
        item.setImageName(imageName);

        System.out.printf("[Builder] Built AuctionItem: '%s' | ₹%.2f | %s | %d days%n",
                item.getTitle(), item.getStartingPrice(), item.getCategory(), durationDays);

        return item;
    }

    // BEKAR #3: main() demo method removed — production WAR mein unnecessary hai.
    // Builder pattern ka demo already code comments mein explain hai.
}

