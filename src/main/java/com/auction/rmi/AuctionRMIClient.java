package com.auction.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 *  AuctionRMIClient.java — RMI Client (Unit 5)
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   - AuctionRMIServer se RMI ke zariye connect hoti hai (port 1099).
 *   - Remote methods call karta hai jaise woh LOCAL calls hon.
 *   - RMI stub automatically network communication handle karta hai —
 *     developer ko TCP/sockets likhne ki zarurat nahi.
 *
 * KAISE CHALATE HAIN?
 *   1. Pehle AuctionRMIServer.java ka main() run karo.
 *   2. Phir is class ka main() run karo:
 *      java com.auction.rmi.AuctionRMIClient
 *
 * UNIT 5 CONCEPTS COVERED:
 *   - LocateRegistry.getRegistry() → RMI Registry se connect karna
 *   - registry.lookup("AuctionService") → naam se remote object dhundna
 *   - AuctionRMIInterface cast → local interface ki tarah use karna
 *   - RemoteException → network errors handle karna
 *
 * NOTE: Client aur Server alag JVM mein (ya alag machines par) chal sakte hain.
 * ════════════════════════════════════════════════════════
 */
public class AuctionRMIClient {

    private static final String SERVER_HOST = "localhost";
    private static final int    RMI_PORT    = 1099;
    private static final String SERVICE_NAME = "AuctionService";

    /**
     * RMI Server se connect karo aur AuctionRMIInterface ka stub return karo.
     * Yeh stub remotely server ke methods call karega — client ke liye transparent.
     *
     * @return AuctionRMIInterface stub (network-transparent proxy)
     * @throws Exception agar connection fail ho
     */
    public static AuctionRMIInterface connectToServer() throws Exception {
        // Step 1: RMI Registry se connect karo (localhost:1099)
        Registry registry = LocateRegistry.getRegistry(SERVER_HOST, RMI_PORT);

        // Step 2: "AuctionService" naam se remote object lookup karo
        // Yeh ek STUB return karta hai — locally callable proxy
        AuctionRMIInterface stub = (AuctionRMIInterface) registry.lookup(SERVICE_NAME);

        System.out.println("[RMI Client] ✅ Connected to " + SERVICE_NAME +
                           " @ " + SERVER_HOST + ":" + RMI_PORT);
        return stub;
    }

    /**
     * Standalone demo — RMI Client test karo.
     * Server chalu hona chahiye pehle se.
     */
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   Auction RMI Client Demo (Unit 5)       ║");
        System.out.println("╚══════════════════════════════════════════╝");

        try {
            // RMI Server se connect karo
            AuctionRMIInterface service = connectToServer();

            // ── Test 1: Ping Server ──────────────────────────────────────────
            System.out.println("\n[Test 1] Pinging server...");
            String pingResponse = service.ping();
            System.out.println("  → Response: " + pingResponse);

            // ── Test 2: Active Auctions dekho ───────────────────────────────
            System.out.println("\n[Test 2] Fetching active auctions...");
            List<String> auctions = service.getActiveAuctions();
            if (auctions.isEmpty()) {
                System.out.println("  → No active auctions.");
            } else {
                for (String auction : auctions) {
                    System.out.println("  → " + auction);
                }
            }

            // ── Test 3: Specific item ka current bid ─────────────────────────
            int testItemId = 1; // Change as needed
            System.out.println("\n[Test 3] Getting current bid for itemId=" + testItemId + "...");
            try {
                double currentBid = service.getCurrentBid(testItemId);
                System.out.println("  → Current Bid: ₹" + currentBid);
            } catch (Exception e) {
                System.out.println("  → " + e.getMessage());
            }

            // ── Test 4: Winner check ─────────────────────────────────────────
            System.out.println("\n[Test 4] Checking winner for itemId=" + testItemId + "...");
            try {
                String winner = service.getWinner(testItemId);
                System.out.println("  → " + winner);
            } catch (Exception e) {
                System.out.println("  → " + e.getMessage());
            }

            // ── Test 5: Remote bid place karo ───────────────────────────────
            // NOTE: Real use mein valid userId aur itemId use karo
            System.out.println("\n[Test 5] Placing a test bid (itemId=1, userId=1, amount=₹5000)...");
            System.out.println("  → (This may fail if item is not active or amount is too low)");
            try {
                boolean bidSuccess = service.placeBid(testItemId, 1, 5000.0);
                System.out.println("  → Bid " + (bidSuccess ? "PLACED ✅" : "REJECTED ❌ (too low or auction closed)"));
            } catch (Exception e) {
                System.out.println("  → Error: " + e.getMessage());
            }

            System.out.println("\n[RMI Client] Demo complete.");

        } catch (Exception e) {
            System.err.println("\n[RMI Client] ❌ Connection failed: " + e.getMessage());
            System.err.println("  Make sure AuctionRMIServer is running on " +
                               SERVER_HOST + ":" + RMI_PORT);
            e.printStackTrace();
        }
    }
}
