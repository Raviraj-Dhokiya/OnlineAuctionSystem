package com.auction.patterns;

import com.auction.model.Bid;

/**
 * ════════════════════════════════════════════════════════
 *  PremiumBidDecorator.java — Decorator Pattern (Unit 8)
 * ════════════════════════════════════════════════════════
 *
 * DECORATOR PATTERN KYA HAI?
 *   - Kisi existing object mein RUNTIME par nayi functionality add karta hai
 *     bina us class ko modify kiye (Open/Closed Principle).
 *   - Inheritance ki jagah COMPOSITION use karta hai.
 *   - "Wrapper" pattern bhi kehte hain — ek object doosre object ko wrap karta hai.
 *
 * YAHAN KAISE KAAM KARTA HAI?
 *   - BidComponent: Interface → ek bid ka kaam define karta hai.
 *   - BasicBid: Simple concrete implementation.
 *   - BidDecorator: Abstract decorator → BidComponent wrap karta hai.
 *   - PremiumBidDecorator: Extra features add karta hai premium users ke liye:
 *       → Auto-increment (agar outbid ho toh khud badh jaata hai)
 *       → VIP badge display
 *       → Priority queue mein jaata hai
 *
 * EXAMPLE USE:
 *   BidComponent bid = new BasicBid(itemId, userId, 5000.0);
 *   BidComponent premiumBid = new PremiumBidDecorator(bid, maxLimit=10000.0);
 *   premiumBid.execute();  // Premium features ke saath bid lagegi
 * ════════════════════════════════════════════════════════
 */

// ── 1. Component Interface ────────────────────────────────────────────────────
interface BidComponent {
    /** Bid ka amount return karo. */
    double getBidAmount();

    /** Bid execute karo (place karo). */
    String execute();

    /** Bid description return karo. */
    String getDescription();
}

// ── 2. Concrete Component: BasicBid ──────────────────────────────────────────

/**
 * BasicBid — Normal user ka regular bid.
 * Yeh wrapper nahi hai — actual Bid model ka lightweight wrapper hai.
 */
class BasicBid implements BidComponent {

    private final int    itemId;
    private final int    userId;
    private final double amount;

    public BasicBid(int itemId, int userId, double amount) {
        this.itemId  = itemId;
        this.userId  = userId;
        this.amount  = amount;
    }

    @Override
    public double getBidAmount() {
        return amount;
    }

    @Override
    public String execute() {
        return String.format("[BasicBid] Placed bid of ₹%.2f by userId=%d on itemId=%d",
                             amount, userId, itemId);
    }

    @Override
    public String getDescription() {
        return "Basic Bid: ₹" + amount;
    }
}

// ── 3. Abstract Decorator ─────────────────────────────────────────────────────

/**
 * BidDecorator — Abstract base for all decorators.
 * Wrapped component ko delegate karta hai by default.
 */
abstract class BidDecorator implements BidComponent {

    protected final BidComponent wrappedBid; // jo component wrap kar rahe hain

    protected BidDecorator(BidComponent bid) {
        this.wrappedBid = bid;
    }

    @Override
    public double getBidAmount() {
        return wrappedBid.getBidAmount(); // delegate to wrapped
    }

    @Override
    public String execute() {
        return wrappedBid.execute(); // delegate by default
    }

    @Override
    public String getDescription() {
        return wrappedBid.getDescription(); // delegate by default
    }
}

// ── 4. Concrete Decorator: PremiumBidDecorator ───────────────────────────────

/**
 * PremiumBidDecorator — Premium user ki bid ke liye extra features:
 *   1. Auto-increment up to maxLimit (agar outbid ho toh khud badh jaata hai)
 *   2. VIP badge description mein add hota hai
 *   3. Priority routing (log mein mark hota hai)
 */
public class PremiumBidDecorator extends BidDecorator {

    private final double maxAutoIncrementLimit; // Max tak auto-increment karega
    private final String vipLabel;

    /**
     * @param bid      Wrap karne wala bid
     * @param maxLimit Agar outbid ho, auto-increment maximum yahan tak karega
     */
    public PremiumBidDecorator(BidComponent bid, double maxAutoIncrementLimit) {
        super(bid);
        this.maxAutoIncrementLimit = maxAutoIncrementLimit;
        this.vipLabel = "⭐ VIP";
    }

    /**
     * Premium execute: extra logging + priority tag.
     */
    @Override
    public String execute() {
        String baseResult = wrappedBid.execute();
        System.out.println("[PremiumDecorator] " + vipLabel +
                           " bid executed. Auto-increment max limit: ₹" + maxAutoIncrementLimit);
        return baseResult + " [" + vipLabel + "] [AutoMax=₹" + maxAutoIncrementLimit + "]";
    }

    /**
     * Agar current bid outbid ho gayi, auto-increment by 5% (up to maxLimit).
     * @param currentHighestBid Abhi tak ka highest bid
     * @return Nayi auto-incremented bid amount, ya -1 agar limit exceed ho gayi
     */
    public double autoIncrement(double currentHighestBid) {
        double increment = currentHighestBid * 0.05; // 5% increment
        double newBid    = currentHighestBid + Math.max(increment, 1.0);
        if (newBid <= maxAutoIncrementLimit) {
            System.out.printf("[PremiumDecorator] Auto-increment: ₹%.2f → ₹%.2f%n",
                              currentHighestBid, newBid);
            return newBid;
        } else {
            System.out.println("[PremiumDecorator] Max limit reached. No auto-increment.");
            return -1; // limit exceed — user ko manually bid karna padega
        }
    }

    @Override
    public double getBidAmount() {
        return wrappedBid.getBidAmount();
    }

    @Override
    public String getDescription() {
        return wrappedBid.getDescription() + " + " + vipLabel +
               " (AutoMax=₹" + maxAutoIncrementLimit + ")";
    }

    // ── Convenience Factory Method ─────────────────────────────────────────────
    /**
     * Directly ek decorated bid banao — client code ke liye shortcut.
     */
    public static PremiumBidDecorator wrap(int itemId, int userId,
                                           double bidAmount, double maxLimit) {
        BasicBid basic = new BasicBid(itemId, userId, bidAmount);
        return new PremiumBidDecorator(basic, maxLimit);
    }

    // ── Demo ──────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        System.out.println("=== Decorator Pattern Demo ===");

        // Normal bid
        BidComponent normalBid = new BasicBid(1, 42, 5000.0);
        System.out.println("Normal: " + normalBid.getDescription());
        System.out.println(normalBid.execute());

        System.out.println();

        // Premium bid (auto-increment up to ₹8000)
        BidComponent premiumBid = PremiumBidDecorator.wrap(1, 99, 5000.0, 8000.0);
        System.out.println("Premium: " + premiumBid.getDescription());
        System.out.println(premiumBid.execute());

        // Auto-increment test
        PremiumBidDecorator premium = (PremiumBidDecorator) premiumBid;
        double next = premium.autoIncrement(5000.0);
        System.out.println("Next auto bid: ₹" + (next > 0 ? next : "max reached"));
    }
}
