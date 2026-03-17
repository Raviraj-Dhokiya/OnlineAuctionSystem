package com.auction.rmi;

import com.auction.model.AuctionItem;
import com.auction.model.Bid;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * AuctionService - RMI Remote Interface (Unit 5)
 * Defines remote methods callable from distributed clients.
 * Demonstrates: RMI, Remote interface, RPC concepts
 */
public interface AuctionService extends Remote {

    /**
     * Get all currently active auction items
     */
    List<AuctionItem> getActiveAuctions() throws RemoteException;

    /**
     * Get details of a specific item
     */
    AuctionItem getItemDetails(int itemId) throws RemoteException;

    /**
     * Place a bid remotely
     * @return true if bid placed successfully, false if too low or auction closed
     */
    boolean placeBid(int itemId, int bidderId, double amount) throws RemoteException;

    /**
     * Get highest current bid for an item
     */
    double getCurrentPrice(int itemId) throws RemoteException;

    /**
     * Get bid history for an item
     */
    List<Bid> getBidHistory(int itemId) throws RemoteException;
}
