package com.auction.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * ════════════════════════════════════════════════════════
 *  AuctionRMIServer.java — RMI Server Side (Unit 5)
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   - Yeh ek Standalone program hai jo ek Service (AuctionServiceImpl) banati hai,
 *     aur usko network directory (Registry) mein "AuctionService" naam se save 
 *     kar deti hai (Bind karti hai).
 *   - Iske baad, koi bhi RMI Client (jaise AuctionRMIClient) us directory se
 *     is service ko dhund kar iski methods call kar sakta hai.
 *
 * NOTE: Web App (Tomcat) chalane se pehle is RMI Server ko alag terminal 
 *       mein chalana padta hai (agar RMI feature use karna hai toh).
 * ════════════════════════════════════════════════════════
 */
public class AuctionRMIServer {

    private static final int    RMI_PORT     = 1099;
    private static final String SERVICE_NAME = "AuctionService";

    public static void main(String[] args) {
        try {
            // 1. Ek remote object banao jo actual kaam karega
            AuctionService service = new AuctionServiceImpl();

            // 2. RMI Registry (Network Directory) start karo port 1099 par
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);

            // 3. Service ko Registry mein bind karo (naam do taaki client dhund sake)
            registry.rebind(SERVICE_NAME, service);

            System.out.println("╔══════════════════════════════════════╗");
            System.out.println("║   Auction RMI Server STARTED         ║");
            System.out.println("║   Port    : " + RMI_PORT + "                       ║");
            System.out.println("║   Service : " + SERVICE_NAME + "          ║");
            System.out.println("║   Lookup  : rmi://localhost/AuctionService ║");
            System.out.println("╚══════════════════════════════════════╝");

        } catch (Exception e) {
            System.err.println("[RMIServer] Failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
