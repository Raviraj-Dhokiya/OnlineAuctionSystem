package com.auction.patterns;

import com.auction.model.Bid;
import com.auction.model.AuctionItem;

/**
 * ════════════════════════════════════════════════════════
 *  BidObserver.java — Observer Pattern (Unit 8)
 * ════════════════════════════════════════════════════════
 *
 * OBSERVER PATTERN KYA HAI?
 *   - "Publisher-Subscriber" ya "Event-Listener" pattern bhi kehte hain.
 *   - Ek SUBJECT (publisher) hota hai jiske paas OBSERVERS (subscribers) registered hote hain.
 *   - Jab subject mein kuchh badlav aata hai (e.g., nayi bid), woh sabhi observers ko
 *     notify() call karke bata deta hai — automatically.
 *   - Subject ko yeh nahi pata ki observer kya karega — loose coupling.
 *
 * YAHAN KAISE KAAM KARTA HAI?
 *   - BidObserver: Interface → subscriber logic define karta hai.
 *   - AuctionSubject: Interface → publisher (register/remove/notify).
 *   - BidEventPublisher: AuctionSubject implement karta hai — actual "mailing list".
 *   - Concrete observers:
 *     → EmailNotificationObserver: Email bhejta hai jab bid lagti hai.
 *     → ConsoleLogObserver: Console mein bid log karta hai (debugging ke liye).
 *     → WatchlistAlertObserver: Watchlist users ko alert karta hai.
 *
 * REAL AUCTION USE CASE:
 *   BidServlet.doPost() → placeBid() success → BidEventPublisher.notifyAll(bid, item)
 *   → email user, log, alert watchers — sab ek call se!
 * ════════════════════════════════════════════════════════
 */

// ── 1. Observer Interface ─────────────────────────────────────────────────────
interface BidObserver {
    /**
     * Nayi bid lagane pe yeh method call hota hai.
     * @param bid  Jo nayi bid lagi
     * @param item Jis item pe bid lagi
     */
    void onBidPlaced(Bid bid, AuctionItem item);
}

// ── 2. Subject Interface (Publisher) ─────────────────────────────────────────
interface AuctionSubject {
    void addObserver(BidObserver observer);
    void removeObserver(BidObserver observer);
    void notifyObservers(Bid bid, AuctionItem item);
}

// ── 3. Concrete Subject: BidEventPublisher ────────────────────────────────────

/**
 * BidEventPublisher — "Mailing list" of auction events.
 * Yeh janta hai ki kaun-kaun observers registered hain.
 * Jab bid lagi, notifyObservers() call karo — sab inform ho jaenge.
 */
class BidEventPublisher implements AuctionSubject {

    private final java.util.List<BidObserver> observers = new java.util.ArrayList<>();

    @Override
    public void addObserver(BidObserver observer) {
        observers.add(observer);
        System.out.println("[Observer] Registered: " + observer.getClass().getSimpleName());
    }

    @Override
    public void removeObserver(BidObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Bid bid, AuctionItem item) {
        System.out.println("[Observer] Notifying " + observers.size() +
                           " observers for bid on: " + item.getTitle());
        for (BidObserver observer : observers) {
            observer.onBidPlaced(bid, item);
        }
    }
}

// ── 4. Concrete Observer: EmailNotificationObserver ──────────────────────────

/**
 * Jab bid lagti hai → winner/outbid email bhejta hai.
 * JavaMail (Unit 7) integration yahan hoti hai.
 */
class EmailNotificationObserver implements BidObserver {

    @Override
    public void onBidPlaced(Bid bid, AuctionItem item) {
        System.out.println("[EmailObserver] Would send outbid email for item: " +
                           item.getTitle() + " | New bid: ₹" + bid.getBidAmount());
    }
}

// ── 5. Concrete Observer: ConsoleLogObserver ──────────────────────────────────

/**
 * Debugging ke liye: har bid ko console mein log karo.
 */
class ConsoleLogObserver implements BidObserver {

    @Override
    public void onBidPlaced(Bid bid, AuctionItem item) {
        System.out.printf("[LogObserver] BID EVENT: Item='%s' | Amount=₹%.2f | Bidder=BidderId#%d%n",
            item.getTitle(), bid.getBidAmount(), bid.getBidderId());
    }
}

// ── 6. Concrete Observer: WatchlistAlertObserver ─────────────────────────────

/**
 * Jab koi bid lagti hai → jo log is item ko watch kar rahe hain unhe alert karo.
 */
class WatchlistAlertObserver implements BidObserver {

    @Override
    public void onBidPlaced(Bid bid, AuctionItem item) {
        System.out.println("[WatchlistObserver] Alert all watchers of item: " +
                           item.getTitle() + " → New bid ₹" + bid.getBidAmount());
        // Real: WatchlistDAO.getWatchers(item.getItemId()) → send notifications
    }
}

// ── 7. AuctionBidNotifier: Facade class (easy to use from BidServlet) ─────────

/**
 * AuctionBidNotifier — Observer pattern ka ready-to-use wrapper.
 *
 * BidServlet mein sirf:
 *   AuctionBidNotifier.getInstance().publish(bid, item);
 * Isse sab observers (email, log, watchlist) automatically trigger ho jaenge.
 *
 * NOTE: Singleton se combine kiya — ek hi instance poori app mein.
 */
public class AuctionBidNotifier implements AuctionSubject {

    // Singleton instance
    private static AuctionBidNotifier instance;

    private final BidEventPublisher publisher = new BidEventPublisher();

    private AuctionBidNotifier() {
        // Default observers register karo
        publisher.addObserver(new ConsoleLogObserver());
        publisher.addObserver(new EmailNotificationObserver());
        publisher.addObserver(new WatchlistAlertObserver());
    }

    /** Singleton getInstance (Unit 8 - Singleton) */
    public static synchronized AuctionBidNotifier getInstance() {
        if (instance == null) {
            instance = new AuctionBidNotifier();
        }
        return instance;
    }

    @Override
    public void addObserver(BidObserver observer) {
        publisher.addObserver(observer);
    }

    @Override
    public void removeObserver(BidObserver observer) {
        publisher.removeObserver(observer);
    }

    @Override
    public void notifyObservers(Bid bid, AuctionItem item) {
        publisher.notifyObservers(bid, item);
    }

    /**
     * Shortcut method: BidServlet se call karo jab bid place ho.
     * Sab registered observers automatically trigger honge.
     */
    public void publish(Bid bid, AuctionItem item) {
        notifyObservers(bid, item);
    }
}
