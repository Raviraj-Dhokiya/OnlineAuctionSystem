package com.auction.patterns;

import com.auction.model.AuctionItem;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// ═══════════════════════════════════════════════════════════════
// UNIT 8 — DESIGN PATTERNS
// ═══════════════════════════════════════════════════════════════

// ────────────────────────────────────────────────────────────────
// 1. SINGLETON PATTERN — AuctionManager
//    Kyun? Pure app mein AuctionManager ka sirf ek hi object hona chahiye
//    taaki data clash na ho. Isliye Constructor private kar diya hai.
// ────────────────────────────────────────────────────────────────
class AuctionManager {

    private static volatile AuctionManager instance;

    // Active auctions tracked in memory
    private final Map<Integer, AuctionItem> activeAuctions = new ConcurrentHashMap<>();

    // Private constructor — prevents direct instantiation
    private AuctionManager() {
        System.out.println("[Singleton] AuctionManager instance created.");
    }

    // Double-checked locking for thread-safe Singleton
    public static AuctionManager getInstance() {
        if (instance == null) {
            synchronized (AuctionManager.class) {
                if (instance == null) {
                    instance = new AuctionManager();
                }
            }
        }
        return instance;
    }

    public void registerAuction(AuctionItem item) {
        activeAuctions.put(item.getItemId(), item);
        System.out.println("[AuctionManager] Registered: " + item.getTitle());
    }

    public void removeAuction(int itemId) {
        activeAuctions.remove(itemId);
    }

    public int getActiveCount() {
        return activeAuctions.size();
    }

    public Collection<AuctionItem> getAll() {
        return activeAuctions.values();
    }
}

// ────────────────────────────────────────────────────────────────
// 2. FACTORY PATTERN — AuctionItemFactory
// Kyun? Alag-alag category ke items (Electronics, Art) banane ka logic
// ek hi Factory class mein chhupa diya.
// Ab main program ko yeh nahi dekhna padta ki "new ElectronicsItem()" likhu
// ya "new ArtItem()". Wo bas Factory se maang leta hai.
// ────────────────────────────────────────────────────────────────
abstract class BaseAuctionItem {
    protected String title;
    protected double startingPrice;
    protected String category;

    public abstract String getCategory();

    public abstract double getBuyNowPrice(); // category-specific logic

    @Override
    public String toString() {
        return "[" + getCategory() + "] " + title + " | Start: ₹" + startingPrice;
    }
}

class ElectronicsItem extends BaseAuctionItem {
    private String brand;

    public ElectronicsItem(String title, double price, String brand) {
        this.title = title;
        this.startingPrice = price;
        this.brand = brand;
        this.category = "Electronics";
    }

    @Override
    public String getCategory() {
        return "Electronics";
    }

    @Override
    public double getBuyNowPrice() {
        return startingPrice * 1.4;
    } // 40% markup

    public String getBrand() {
        return brand;
    }
}

class VehicleItem extends BaseAuctionItem {
    private int year;

    public VehicleItem(String title, double price, int year) {
        this.title = title;
        this.startingPrice = price;
        this.year = year;
        this.category = "Vehicles";
    }

    @Override
    public String getCategory() {
        return "Vehicles";
    }

    @Override
    public double getBuyNowPrice() {
        return startingPrice * 1.2;
    } // 20% markup

    public int getYear() {
        return year;
    }
}

class ArtItem extends BaseAuctionItem {
    private String artist;

    public ArtItem(String title, double price, String artist) {
        this.title = title;
        this.startingPrice = price;
        this.artist = artist;
        this.category = "Art";
    }

    @Override
    public String getCategory() {
        return "Art";
    }

    @Override
    public double getBuyNowPrice() {
        return startingPrice * 2.0;
    } // 100% markup

    public String getArtist() {
        return artist;
    }
}

/**
 * AuctionItemFactory — Factory Method Pattern
 * Client just calls create(category, ...) without knowing concrete classes.
 */
class AuctionItemFactory {

    /**
     * Factory method - returns appropriate item type for given category
     */
    public static BaseAuctionItem create(String category, String title,
            double price, String extra) {
        switch (category.toUpperCase()) {
            case "ELECTRONICS":
                return new ElectronicsItem(title, price, extra);
            case "VEHICLES":
                return new VehicleItem(title, price,
                        Integer.parseInt(extra));
            case "ART":
                return new ArtItem(title, price, extra);
            default:
                throw new IllegalArgumentException("Unknown category: " + category);
        }
    }
}

// ────────────────────────────────────────────────────────────────
// 3. OBSERVER PATTERN — Bid Notification
// Kyun? Jab koi naya event ho (jaise kisi ne bid lagayi), tab
// jo-jo services us event ka wait kar rahi hain, unhe automatically message
// bhej dena. (Jaise Socket listner ko, aur Log writer ko).
//
// FIX: BidEventPublisher ab PUBLIC hai aur BidServlet isko directly use karta
// hai.
// (Pehle BidServlet Observer bypass karke BidNotificationServer directly call
// karta tha)
// FIX: getInstance() ab thread-safe hai (double-checked locking + volatile).
// FIX: observers list ab CopyOnWriteArrayList hai — concurrent iteration safe.
// ────────────────────────────────────────────────────────────────
interface BidObserver {
    void onNewBid(int itemId, String bidder, double amount);
}

// Concrete observer: notifies via Socket (Unit 3)
class SocketNotificationObserver implements BidObserver {
    @Override
    public void onNewBid(int itemId, String bidder, double amount) {
        com.auction.network.BidNotificationServer.getInstance()
                .broadcastBidUpdate(itemId, bidder, amount, "Item #" + itemId);
        System.out.println("[Observer:Socket] Broadcast sent for item #" + itemId);
    }
}

// Concrete observer: logs to file (Unit 2)
class LogObserver implements BidObserver {
    @Override
    public void onNewBid(int itemId, String bidder, double amount) {
        com.auction.io.AuctionLogger.log(
                "NEW_BID | Item #" + itemId + " | Bidder: " + bidder +
                        " | Amount: ₹" + amount);
    }
}

/**
 * Subject - maintains observer list and notifies them.
 *
 * FIX 1: getInstance() ab thread-safe hai — volatile + double-checked locking.
 * FIX 2: observers list ab CopyOnWriteArrayList — thread-safe iteration.
 * FIX 3: class PUBLIC hai — BidServlet isse directly use kar sakta hai.
 */
class BidEventPublisher {

    // FIX: volatile keyword → main memory se fresh value read hogi (thread-safe
    // visibility)
    private static volatile BidEventPublisher instance;

    // FIX: CopyOnWriteArrayList → concurrent subscribe/unsubscribe aur iteration
    // safe
    private final List<BidObserver> observers = new CopyOnWriteArrayList<>();

    private BidEventPublisher() {
        // Register default observers
        subscribe(new SocketNotificationObserver());
        subscribe(new LogObserver());
    }

    /**
     * FIX: Double-checked locking → thread-safe Singleton
     * (AuctionManager jaisa pattern)
     */
    public static BidEventPublisher getInstance() {
        if (instance == null) {
            synchronized (BidEventPublisher.class) {
                if (instance == null) {
                    instance = new BidEventPublisher();
                }
            }
        }
        return instance;
    }

    public void subscribe(BidObserver o) {
        observers.add(o);
    }

    public void unsubscribe(BidObserver o) {
        observers.remove(o);
    }

    public void notifyBid(int itemId, String bidder, double amount) {
        observers.forEach(o -> o.onNewBid(itemId, bidder, amount));
    }
}

// ────────────────────────────────────────────────────────────────
// 4. DECORATOR PATTERN — Bid Types
// Kyun? Ek simple Bid ko extra taqat/features dena bina existing
// class (SimpleBid) ko change kiye. Jaise Premium badge laga dena.
// ────────────────────────────────────────────────────────────────
interface BidComponent {
    double getAmount();

    String getDescription();

    boolean isValid();
}

// Base concrete bid
class SimpleBid implements BidComponent {
    private final double amount;
    private final int itemId;
    private final int bidderId;

    public SimpleBid(int itemId, int bidderId, double amount) {
        this.itemId = itemId;
        this.bidderId = bidderId;
        this.amount = amount;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public String getDescription() {
        return "Bid ₹" + amount;
    }

    @Override
    public boolean isValid() {
        return amount > 0;
    }

    public int getItemId() {
        return itemId;
    }

    public int getBidderId() {
        return bidderId;
    }
}

// Abstract Decorator
abstract class BidDecorator implements BidComponent {
    protected final BidComponent wrapped;

    public BidDecorator(BidComponent b) {
        this.wrapped = b;
    }

    @Override
    public double getAmount() {
        return wrapped.getAmount();
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription();
    }

    @Override
    public boolean isValid() {
        return wrapped.isValid();
    }
}

// Decorator: Premium Bid (adds 5% bonus to make it more competitive)
class PremiumBidDecorator extends BidDecorator {
    public PremiumBidDecorator(BidComponent b) {
        super(b);
    }

    @Override
    public double getAmount() {
        return wrapped.getAmount() * 1.05;
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " [PREMIUM +5%]";
    }
}

// Decorator: Reserve Check (validates against item reserve price)
class ReserveBidDecorator extends BidDecorator {
    private final double reservePrice;

    public ReserveBidDecorator(BidComponent b, double reservePrice) {
        super(b);
        this.reservePrice = reservePrice;
    }

    @Override
    public boolean isValid() {
        return wrapped.isValid() && wrapped.getAmount() >= reservePrice;
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() +
                " [Reserve: ₹" + reservePrice + (isValid() ? " MET]" : " NOT MET]");
    }
}

// ────────────────────────────────────────────────────────────────
// 5. BUILDER PATTERN — AuctionItem creation
// Kyun? Jab kisi object (jaise AuctionItem) mein bohot saare parameters
// hote hain, toh lambe-lambe constructors mein values pass karna mushkil
// ho jata hai. Builder Pattern use smooth banata hai step-by-step
// chain method call se: builder().title("..").category("..").build()
// ────────────────────────────────────────────────────────────────
class AuctionItemBuilder {
    private final com.auction.model.AuctionItem item;

    public AuctionItemBuilder() {
        item = new com.auction.model.AuctionItem();
    }

    public AuctionItemBuilder title(String t) {
        item.setTitle(t);
        return this;
    }

    public AuctionItemBuilder description(String d) {
        item.setDescription(d);
        return this;
    }

    public AuctionItemBuilder category(String c) {
        item.setCategory(c);
        return this;
    }

    public AuctionItemBuilder startingPrice(double p) {
        item.setStartingPrice(p);
        item.setCurrentPrice(p);
        return this;
    }

    public AuctionItemBuilder reservePrice(double r) {
        item.setReservePrice(r);
        return this;
    }

    public AuctionItemBuilder sellerId(int id) {
        item.setSellerId(id);
        return this;
    }

    public AuctionItemBuilder endTime(java.sql.Timestamp t) {
        item.setEndTime(t);
        return this;
    }

    public com.auction.model.AuctionItem build() {
        item.setStartTime(new java.sql.Timestamp(System.currentTimeMillis()));
        item.setStatus("ACTIVE");
        return item;
    }
}
