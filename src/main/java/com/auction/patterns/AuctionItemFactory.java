package com.auction.patterns;

import com.auction.model.AuctionItem;
import java.sql.Timestamp;

/**
 * ════════════════════════════════════════════════════════
 *  AuctionItemFactory.java — Factory Pattern (Unit 8)
 * ════════════════════════════════════════════════════════
 *
 * FACTORY PATTERN KYA HAI?
 *   - Object creation ko centralize karta hai ek "factory" method mein.
 *   - Client (calling code) ko yeh nahi pata hota ki exact object kaise bana hai.
 *   - Agar AuctionItem ki creation logic badalni ho, sirf factory badlo —
 *     baaki code untouched rahega.
 *
 * YAHAN KAISE USE HOTA HAI?
 *   - AuctionItemFactory different "types" ke auction items banata hai:
 *     → createElectronicsItem() — Electronics category ke liye default setup
 *     → createArtItem()        — Art/Collectibles ke liye
 *     → createVehicleItem()    — Vehicle auctions ke liye
 *     → createFromForm()       — Servlet form params se (actual use case)
 *
 * BENEFIT: AuctionItemServlet.java mein sirf factory.createFromForm() call hoga —
 * koi manual field-setting nahi.
 * ════════════════════════════════════════════════════════
 */
public class AuctionItemFactory {

    /**
     * Electronics category item banana — sensible defaults ke saath.
     * Seller/price manually set karo iske baad.
     */
    public static AuctionItem createElectronicsItem(String title, String description,
                                                    double startingPrice, int sellerId) {
        AuctionItem item = new AuctionItem();
        item.setTitle(title);
        item.setDescription(description);
        item.setCategory("Electronics");
        item.setStartingPrice(startingPrice);
        item.setCurrentPrice(startingPrice);         // current = starting at first
        item.setReservePrice(startingPrice * 1.1);   // 10% above starting = reserve
        item.setSellerId(sellerId);
        item.setStatus("PENDING");                   // Admin approve karne tak PENDING

        // Default: auction 7 days baad khulegi aur 7 din chalegi
        long now = System.currentTimeMillis();
        item.setStartTime(new Timestamp(now));
        item.setEndTime(new Timestamp(now + 7L * 24 * 60 * 60 * 1000)); // +7 days
        item.setCreatedAt(new Timestamp(now));

        System.out.println("[Factory] Created Electronics item: " + title);
        return item;
    }

    /**
     * Art/Collectibles category item — reserve price higher hoti hai art mein.
     */
    public static AuctionItem createArtItem(String title, String description,
                                            double startingPrice, int sellerId) {
        AuctionItem item = new AuctionItem();
        item.setTitle(title);
        item.setDescription(description);
        item.setCategory("Art & Collectibles");
        item.setStartingPrice(startingPrice);
        item.setCurrentPrice(startingPrice);
        item.setReservePrice(startingPrice * 1.5);   // Art mein 50% above starting = reserve
        item.setSellerId(sellerId);
        item.setStatus("PENDING");

        long now = System.currentTimeMillis();
        item.setStartTime(new Timestamp(now));
        item.setEndTime(new Timestamp(now + 14L * 24 * 60 * 60 * 1000)); // Art auctions: 14 days
        item.setCreatedAt(new Timestamp(now));

        System.out.println("[Factory] Created Art item: " + title);
        return item;
    }

    /**
     * Vehicle auction item — zyada duration, high reserve.
     */
    public static AuctionItem createVehicleItem(String title, String description,
                                                double startingPrice, int sellerId) {
        AuctionItem item = new AuctionItem();
        item.setTitle(title);
        item.setDescription(description);
        item.setCategory("Vehicles");
        item.setStartingPrice(startingPrice);
        item.setCurrentPrice(startingPrice);
        item.setReservePrice(startingPrice * 1.2);   // Vehicles: 20% reserve
        item.setSellerId(sellerId);
        item.setStatus("PENDING");

        long now = System.currentTimeMillis();
        item.setStartTime(new Timestamp(now));
        item.setEndTime(new Timestamp(now + 21L * 24 * 60 * 60 * 1000)); // 21 days for vehicles
        item.setCreatedAt(new Timestamp(now));

        System.out.println("[Factory] Created Vehicle item: " + title);
        return item;
    }

    /**
     * Generic item creator — category parameter se decide hota hai.
     * AuctionItemServlet.java se direct use hota hai (form data se).
     *
     * @param category "Electronics", "Art & Collectibles", "Vehicles", ya kuch bhi
     */
    public static AuctionItem createFromForm(String title, String description,
                                             String category, double startingPrice,
                                             double reservePrice, int sellerId,
                                             Timestamp startTime, Timestamp endTime) {
        AuctionItem item = new AuctionItem();
        item.setTitle(title);
        item.setDescription(description);
        item.setCategory(category);
        item.setStartingPrice(startingPrice);
        item.setCurrentPrice(startingPrice);    // naya item: current = starting price
        item.setReservePrice(reservePrice > 0 ? reservePrice : startingPrice * 1.1);
        item.setSellerId(sellerId);
        item.setStatus("ACTIVE");               // Form se seedha ACTIVE (Admin approved)
        item.setStartTime(startTime);
        item.setEndTime(endTime);
        item.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        System.out.println("[Factory] Created item from form: " + title +
                           " | Category: " + category + " | Start: ₹" + startingPrice);
        return item;
    }
}
