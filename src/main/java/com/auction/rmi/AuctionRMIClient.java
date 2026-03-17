package com.auction.rmi;

import com.auction.model.AuctionItem;
import com.auction.model.Bid;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

/**
 * ════════════════════════════════════════════════════════
 *  AuctionRMIClient.java — RMI Client Side (Unit 5)
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   - Yeh ek Standalone (console/terminal) program hai jo RMI ka istemal 
 *     karke alag machine/JVM se Auction server ki methods ko directly call karta hai.
 *
 * RMI FLOW (Client side):
 *   1. LocateRegistry: Pehle Network directory (Registry) ko dhoondho (Port 1099 par).
 *   2. lookup("AuctionService"): "AuctionService" naam wale Remote Object ka reference mango (Stub).
 *   3. Ab yeh stub web connection ka kaam chupati hai, aap bas method call karo:
 *      service.getActiveAuctions() — data seedha server se network se aayega.
 * ════════════════════════════════════════════════════════
 */
public class AuctionRMIClient {

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        // RMI Registry (Directory) dhundhna localhost par port 1099 par
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        
        // "AuctionService" namak reference (Stub) registry se nikalna.
        AuctionService service = (AuctionService) registry.lookup("AuctionService");

        System.out.println("╔═══════════════════════════════════╗");
        System.out.println("║   Online Auction - RMI Client     ║");
        System.out.println("╚═══════════════════════════════════╝");

        while (true) {
            System.out.println("\n1. View active auctions");
            System.out.println("2. View item details");
            System.out.println("3. Place a bid");
            System.out.println("4. View bid history");
            System.out.println("5. Exit");
            System.out.print("Choice: ");

            int choice = Integer.parseInt(sc.nextLine().trim());

            switch (choice) {
                case 1:
                    List<AuctionItem> items = service.getActiveAuctions();
                    System.out.println("\n── Active Auctions ──");
                    for (AuctionItem item : items) {
                        System.out.printf("  #%d | %-30s | Current: ₹%.2f | Ends: %s%n",
                            item.getItemId(), item.getTitle(),
                            item.getCurrentPrice(), item.getEndTime());
                    }
                    break;

                case 2:
                    System.out.print("Enter item ID: ");
                    int itemId = Integer.parseInt(sc.nextLine().trim());
                    AuctionItem item = service.getItemDetails(itemId);
                    if (item != null) {
                        System.out.println("\n── Item Details ──");
                        System.out.println("  Title       : " + item.getTitle());
                        System.out.println("  Description : " + item.getDescription());
                        System.out.println("  Category    : " + item.getCategory());
                        System.out.println("  Start Price : ₹" + item.getStartingPrice());
                        System.out.println("  Current     : ₹" + item.getCurrentPrice());
                        System.out.println("  Ends At     : " + item.getEndTime());
                    } else {
                        System.out.println("Item not found.");
                    }
                    break;

                case 3:
                    System.out.print("Your User ID: ");
                    int bidderId = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Item ID to bid on: ");
                    int bidItemId = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Your bid amount: ₹");
                    double amount = Double.parseDouble(sc.nextLine().trim());

                    boolean ok = service.placeBid(bidItemId, bidderId, amount);
                    System.out.println(ok ? "✓ Bid placed successfully!"
                                         : "✗ Bid failed — too low or auction closed.");
                    break;

                case 4:
                    System.out.print("Enter item ID: ");
                    int histId = Integer.parseInt(sc.nextLine().trim());
                    List<Bid> bids = service.getBidHistory(histId);
                    System.out.println("\n── Bid History ──");
                    bids.forEach(b -> System.out.printf(
                        "  Bidder #%d | ₹%.2f | %s%n",
                        b.getBidderId(), b.getBidAmount(), b.getBidTime()));
                    break;

                case 5:
                    System.out.println("Goodbye!");
                    return;
            }
        }
    }
}
