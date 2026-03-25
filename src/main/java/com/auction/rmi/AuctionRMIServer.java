package com.auction.rmi;

import com.auction.dao.AuctionItemDAO;
import com.auction.dao.BidDAO;
import com.auction.dao.WinnerDAO;
import com.auction.model.AuctionItem;
import com.auction.model.Bid;
import com.auction.model.Winner;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 *  AuctionRMIServer.java — RMI Server (Unit 5)
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   - AuctionRMIInterface ke methods implement karti hai.
 *   - UnicastRemoteObject se extend karta hai — isse yeh class
 *     network par remotely callable ho jati hai.
 *   - RMI Registry port 1099 par bind hota hai.
 *
 * KAISE CHALATE HAIN?
 *   1. main() method run karo: java com.auction.rmi.AuctionRMIServer
 *   2. RMI Registry automatically port 1099 par start hogi.
 *   3. Client AuctionRMIClient.java use karke connect kar sakta hai.
 *
 * UNIT 5 CONCEPTS COVERED:
 *   - UnicastRemoteObject.exportObject() → object ko network-accessible banata hai
 *   - LocateRegistry.createRegistry(1099) → RMI registry start karna
 *   - registry.bind("AuctionService", stub) → naam se register karna
 *   - RemoteException → har remote method mein declare honi chahiye
 * ════════════════════════════════════════════════════════
 */
public class AuctionRMIServer extends UnicastRemoteObject implements AuctionRMIInterface {

    private static final long serialVersionUID = 1L;
    private static final int  RMI_PORT = 1099;
    private static final String SERVICE_NAME = "AuctionService";

    // DAO objects to interact with the database
    private final AuctionItemDAO itemDAO   = new AuctionItemDAO();
    private final BidDAO         bidDAO    = new BidDAO();
    private final WinnerDAO      winnerDAO = new WinnerDAO();

    /**
     * Constructor: UnicastRemoteObject ka constructor call karna zaroori hai
     * taki yeh object remote calls ke liye export ho sake.
     */
    protected AuctionRMIServer() throws RemoteException {
        super(); // UnicastRemoteObject.exportObject() internally call hota hai
    }

    // ── Remote Method Implementations ─────────────────────────────────────────

    /**
     * Kisi item ka current highest bid amount return karo.
     * Client remotely yeh call karke latest price dekh sakta hai without HTTP.
     */
    @Override
    public double getCurrentBid(int itemId) throws RemoteException {
        try {
            AuctionItem item = itemDAO.findById(itemId);
            if (item == null) {
                throw new RemoteException("Item not found: itemId=" + itemId);
            }
            return item.getCurrentPrice();
        } catch (Exception e) {
            throw new RemoteException("Error fetching current bid: " + e.getMessage(), e);
        }
    }

    /**
     * Remotely bid lagao (RMI se — bina browser ke).
     * BidDAO.placeBid() internally DB transaction use karta hai.
     */
    @Override
    public boolean placeBid(int itemId, int userId, double bidAmount) throws RemoteException {
        try {
            Bid bid = new Bid(itemId, userId, bidAmount);
            boolean success = bidDAO.placeBid(bid);
            if (success) {
                System.out.println("[RMI] Bid placed: itemId=" + itemId +
                                   " userId=" + userId + " amount=₹" + bidAmount);
            }
            return success;
        } catch (Exception e) {
            throw new RemoteException("Error placing bid via RMI: " + e.getMessage(), e);
        }
    }

    /**
     * Saari active auctions ki list return karo (human-readable strings).
     */
    @Override
    public List<String> getActiveAuctions() throws RemoteException {
        try {
            List<AuctionItem> items = itemDAO.getAllItems();
            List<String> result = new ArrayList<>();
            for (AuctionItem item : items) {
                if ("ACTIVE".equals(item.getStatus())) {
                    result.add(String.format("[ID:%d] %s | Current: ₹%.2f | Ends: %s",
                        item.getItemId(),
                        item.getTitle(),
                        item.getCurrentPrice(),
                        item.getEndTime()));
                }
            }
            if (result.isEmpty()) {
                result.add("No active auctions at the moment.");
            }
            return result;
        } catch (Exception e) {
            throw new RemoteException("Error fetching active auctions: " + e.getMessage(), e);
        }
    }

    /**
     * Kisi auction ka winner kaun hai.
     */
    @Override
    public String getWinner(int itemId) throws RemoteException {
        try {
            Winner winner = winnerDAO.getWinnerByItem(itemId);
            if (winner == null) {
                return "No winner yet for itemId=" + itemId;
            }
            return String.format("Winner: %s | Winning Bid: ₹%.2f | Payment: %s",
                winner.getWinnerName(),
                winner.getWinningAmount(),
                winner.getPaymentStatus());
        } catch (Exception e) {
            throw new RemoteException("Error fetching winner: " + e.getMessage(), e);
        }
    }

    /**
     * Server health check.
     */
    @Override
    public String ping() throws RemoteException {
        return "Auction RMI Server is running on port " + RMI_PORT;
    }

    // ── Main Method: RMI Server Start ─────────────────────────────────────────

    /**
     * RMI Server ko start karo:
     *   1. AuctionRMIServer ka object banao (implement karta hai AuctionRMIInterface)
     *   2. Port 1099 par RMI Registry create karo
     *   3. "AuctionService" naam se is object ko bind karo
     *
     * Client LocateRegistry.getRegistry("localhost", 1099) se connect karega.
     */
    public static void main(String[] args) {
        try {
            System.out.println("[RMI Server] Starting Auction RMI Server on port " + RMI_PORT + "...");

            // Step 1: Server object banao (UnicastRemoteObject se extend kiya hai)
            AuctionRMIServer server = new AuctionRMIServer();

            // Step 2: RMI Registry port 1099 par create karo
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);

            // Step 3: "AuctionService" naam se bind karo
            registry.bind(SERVICE_NAME, server);

            System.out.println("[RMI Server] ✅ Bound as '" + SERVICE_NAME + "' on port " + RMI_PORT);
            System.out.println("[RMI Server] Waiting for client connections...");
            System.out.println("[RMI Server] Client can connect with:");
            System.out.println("             LocateRegistry.getRegistry(\"localhost\", 1099)");
            System.out.println("             registry.lookup(\"AuctionService\")");

            // Server tab tak chalta rahega jab tak manually band na karo
            // (main thread alive rakhta hai)
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("[RMI Server] ❌ Failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
