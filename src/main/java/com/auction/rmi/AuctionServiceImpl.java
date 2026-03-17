package com.auction.rmi;

import com.auction.dao.AuctionItemDAO;
import com.auction.dao.BidDAO;
import com.auction.model.AuctionItem;
import com.auction.model.Bid;
import com.auction.network.BidNotificationServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 *  AuctionServiceImpl.java — RMI Server Side Implementation
 * ════════════════════════════════════════════════════════
 *
 * RMI KYA HOTA HAI? (Remote Method Invocation)
 *   Normally methods sirf usi machine par call ho sakti hain jahan code run ho.
 *   RMI se alag machine ka Java program bhi is server ke methods call kar sakta hai,
 *   aise jaise local methods call kar raha ho.
 *
 * KAISE KAAM KARTA HAI?
 *   Server:
 *     - AuctionServiceImpl extend karta hai UnicastRemoteObject
 *     - RMI registry mein register hota hai (ek phone directory jaisi)
 *
 *   Client (AuctionRMIClient.java):
 *     - Registry se "AuctionService" lookup karta hai
 *     - Methods call karta hai → calls automatically network se server par jaati hain
 *     - Server response wapas bhejta hai
 *
 * FLOW:
 *   Client.getActiveAuctions() → Network → Server.getActiveAuctions() → DB → return
 *
 * NOTE: Web app mein RMI directly use nahi hota — yeh academic demonstration hai.
 * ════════════════════════════════════════════════════════
 */
public class AuctionServiceImpl extends UnicastRemoteObject
        implements AuctionService {

    private final AuctionItemDAO itemDAO = new AuctionItemDAO();
    private final BidDAO         bidDAO  = new BidDAO();

    // Constructor: UnicastRemoteObject ki zarurat hai RMI ke liye
    // super() call automatically object ko RMI runtime se connect karta hai
    public AuctionServiceImpl() throws RemoteException {
        super();
    }

    /**
     * Sab active auctions ki list return karo.
     * Remote client yeh method call karta hai → DB se items aate hain → network se wapas.
     */
    @Override
    public List<AuctionItem> getActiveAuctions() throws RemoteException {
        System.out.println("[RMI] getActiveAuctions() called by remote client");
        return itemDAO.getActiveItems(); // DB se ACTIVE status wale items
    }

    /**
     * Specific item ki full details return karo.
     */
    @Override
    public AuctionItem getItemDetails(int itemId) throws RemoteException {
        System.out.println("[RMI] getItemDetails(" + itemId + ") called");
        return itemDAO.findById(itemId); // DB se ek item ki details
    }

    /**
     * Remote client ki taraf se bid lagao.
     * (Same kaam BidServlet karta hai web browser se, yeh RMI se karta hai)
     *
     * @return true = bid success, false = bid reject (amount kam tha ya auction close)
     */
    @Override
    public boolean placeBid(int itemId, int bidderId, double amount)
            throws RemoteException {
        System.out.println("[RMI] placeBid(item=" + itemId +
                           ", bidder=" + bidderId + ", amount=" + amount + ")");

        // BidDAO.placeBid() → DB transaction (check + insert + update)
        Bid bid = new Bid(itemId, bidderId, amount);
        boolean success = bidDAO.placeBid(bid);

        if (success) {
            // Bid success → sab connected web browsers ko bhi notify karo (Socket)
            AuctionItem item = itemDAO.findById(itemId);
            BidNotificationServer.getInstance().broadcastBidUpdate(
                itemId, "Bidder#" + bidderId, amount,
                item != null ? item.getTitle() : "Unknown"
            );
        }
        return success;
    }

    /**
     * Kisi item ki abhi ki highest bid kitni hai?
     */
    @Override
    public double getCurrentPrice(int itemId) throws RemoteException {
        AuctionItem item = itemDAO.findById(itemId);
        return item != null ? item.getCurrentPrice() : 0; // 0 agar item nahi mila
    }

    /**
     * Kisi item par sab bids ki history (highest first).
     */
    @Override
    public List<Bid> getBidHistory(int itemId) throws RemoteException {
        return bidDAO.getBidsForItem(itemId);
    }
}
