package com.auction.network;

import com.auction.dao.AuctionItemDAO;
import com.auction.dao.WinnerDAO;
import com.auction.mail.AuctionMailService;
import com.auction.model.AuctionItem;
import com.auction.model.Winner;

import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 *  AuctionExpiryChecker.java — Background Thread (Auto Close Auctions)
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   Ek background thread hai jo app start hone par shuru hoti hai
 *   aur har 60 seconds mein check karti hai:
 *   "Koi auction ka time khatam hua kya?"
 *
 *   Agar haan, toh automatically:
 *   1. Auction close kar deta hai (status → CLOSED)
 *   2. Oracle stored procedure call karta hai jo winner declare karta hai
 *   3. Winner ko email notification bhejta hai
 *   4. AuctionLogger mein event record karta hai
 *   5. UDP alert bhejta hai (agar koi listener ho)
 *
 * THREAD KAISE SHURU HOTI HAI?
 *   AppStartupListener.contextInitialized() → Thread t = new Thread(checker) → t.start()
 *   t.setDaemon(true) → App band hone par yeh thread bhi band ho jayegi
 *
 * IMPLEMENTS Runnable KYUN?
 *   Runnable = "mujhe ek alag thread mein chalao"
 *   run() method wo kaam karta hai jo background mein hona chahiye.
 * ════════════════════════════════════════════════════════
 */
public class AuctionExpiryChecker implements Runnable {

    private final AuctionItemDAO itemDAO   = new AuctionItemDAO();
    private final WinnerDAO      winnerDAO = new WinnerDAO();

    /**
     * run() — Thread shuru hone par yeh method execute hoti hai.
     * Jab tak app chale, infinite loop mein har 60 seconds mein check karta hai.
     */
    @Override
    public void run() {
        // Jab tak thread interrupt na ho tab tak chalta raho
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Expired auctions check karo
                checkExpiredAuctions();

                // 60 seconds wait karo (60,000 milliseconds)
                Thread.sleep(60_000);

            } catch (InterruptedException e) {
                // sleep() ke dauran interrupt aya → thread band karo
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // Koi aur error → log karo aur continue karo (loop band mat karo)
                System.err.println("[ExpiryChecker] Error: " + e.getMessage());
            }
        }
    }

    /**
     * checkExpiredAuctions() — Actual checking logic.
     *
     * FIX: Ab getExpiredActiveItems() use karta hai (getActiveItems() nahi).
     *
     * Pehle getActiveItems() use hota tha jisme "end_time > CURRENT_TIMESTAMP" tha.
     * Matlab expired auctions query mein AATE HI NAHI the → determine_winner() kabhi
     * nahi chalti thi → winners table empty rehti thi → dashboard mein "No wins" dikhta tha!
     *
     * Ab getExpiredActiveItems() laata hai:
     *   status='ACTIVE' AND end_time <= CURRENT_TIMESTAMP
     * → Sirf woh auctions jo expire ho chuki hain lekin abhi close nahi ki gayi
     * → determine_winner() sahi se chalti hai → winner dashboard mein dikhta hai ✅
     */
    private void checkExpiredAuctions() {
        // FIX: getExpiredActiveItems() → sirf expired-but-still-ACTIVE auctions
        List<AuctionItem> expiredItems = itemDAO.getExpiredActiveItems();

        for (AuctionItem item : expiredItems) {
            // No need for item.isEnded() check — query already ensures end_time <= NOW
            System.out.println("[ExpiryChecker] Closing auction: " + item.getTitle());

            // ── Step 1: UDP Alert bhejo (Unit 3 - Networking) ────────────
                BidNotificationServer.getInstance()
                    .sendUDPAlert("ENDING:" + item.getItemId() + ":" + item.getTitle());

                // ── Step 2: Auction close + Winner declare (Oracle Stored Procedure) ──
                itemDAO.closeAuctionAndDetermineWinner(item.getItemId());

                // ── Step 3: Winner ki info lao ───────────────────────────────
                Winner winner = winnerDAO.getWinnerByItem(item.getItemId());

                // ── Step 4: Winner ko email bhejo (JavaMail) ─────────────────
                if (winner != null && winner.getWinnerEmail() != null) {
                    AuctionMailService.sendWinnerEmail(
                        winner.getWinnerEmail(),
                        winner.getWinnerName(),
                        item.getTitle(),
                        winner.getWinningAmount()
                    );
                }

                // ── Step 5: File mein log likho (Java I/O) ───────────────────
                com.auction.io.AuctionLogger.log(
                    "CLOSED | Item #" + item.getItemId() +
                    " | " + item.getTitle() +
                    (winner != null ? " | Winner: " + winner.getWinnerName() : " | No bids")
                );
        }
    }
}
