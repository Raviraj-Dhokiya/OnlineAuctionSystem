package com.auction.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 *  AuctionRMIInterface.java — RMI Remote Interface (Unit 5)
 * ════════════════════════════════════════════════════════
 *
 * YEH INTERFACE KYA HAI?
 *   - Java RMI (Remote Method Invocation) ka base interface hai.
 *   - Isme woh methods declare hote hain jo CLIENT remotely call kar sakta hai.
 *   - Har method `RemoteException` throw karta hai — yeh required hai RMI mein
 *     kyunki network errors ho sakti hain.
 *
 * UNIT 5 CONCEPTS:
 *   - Remote interface: java.rmi.Remote se extend karna zaroori hai.
 *   - Stub/Skeleton: rmiregistry automatically stub banata hai client ke liye.
 *   - Port 1099: Default RMI registry port.
 * ════════════════════════════════════════════════════════
 */
public interface AuctionRMIInterface extends Remote {

    /**
     * Current highest bid amount return karo kisi item ke liye.
     * @param itemId Auction item ID
     * @return Current highest bid (ya starting price agar koi bid nahi)
     */
    double getCurrentBid(int itemId) throws RemoteException;

    /**
     * Nayi bid lagao remotely (RMI se).
     * @param itemId   Auction item ID
     * @param userId   Bidder ka user ID
     * @param bidAmount Bid amount (₹)
     * @return true agar bid successfully place hui, false agar bid bahut kam thi
     */
    boolean placeBid(int itemId, int userId, double bidAmount) throws RemoteException;

    /**
     * Active auction items ki list lo.
     * @return List of String descriptions of active items
     */
    List<String> getActiveAuctions() throws RemoteException;

    /**
     * Kisi item ka winner kaun hai (agar auction close ho gayi).
     * @param itemId Auction item ID
     * @return Winner ka naam ya "No winner yet"
     */
    String getWinner(int itemId) throws RemoteException;

    /**
     * Server ka status check karo (ping).
     * @return "Auction RMI Server is running on port 1099"
     */
    String ping() throws RemoteException;
}
