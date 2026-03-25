package com.auction.network;

import com.auction.rmi.AuctionRMIServer;
import com.auction.util.DBConnection;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * AppStartupListener - Unit 6 (Servlet lifecycle)
 * Starts BidNotificationServer, AuctionChatServer, AuctionExpiryChecker,
 * and AuctionRMIServer when Tomcat deploys the application.
 */
@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[Startup] Application starting...");

        // Start Bid Notification Socket Server (Unit 3)
        BidNotificationServer.getInstance().start();
        System.out.println("[Startup] BidNotificationServer started on port 9090");

        // Start Chat Server (Unit 3)
        AuctionChatServer.getInstance().start();
        System.out.println("[Startup] AuctionChatServer started on port 9092");

        // Start Auction Expiry Checker - background thread
        AuctionExpiryChecker checker = new AuctionExpiryChecker();
        Thread t = new Thread(checker, "AuctionExpiryChecker");
        t.setDaemon(true);
        t.start();
        System.out.println("[Startup] AuctionExpiryChecker started.");

        // IMPROVEMENT #3 FIX: RMI Server auto-start karo (Unit 5 - RMI)
        // Pehle RMI server manually run karna padta tha. Ab webapp ke saath automatically start hoga.
        // Daemon thread mein chalta hai taaki JVM shutdown block na kare.
        Thread rmiThread = new Thread(() -> {
            try {
                AuctionRMIServer.startServer();
                System.out.println("[Startup] AuctionRMIServer started successfully.");
            } catch (Exception e) {
                System.err.println("[Startup] AuctionRMIServer failed to start: " + e.getMessage());
            }
        }, "AuctionRMIServer-Thread");
        rmiThread.setDaemon(true);
        rmiThread.start();
        System.out.println("[Startup] AuctionRMIServer starting on daemon thread...");

        System.out.println("\n");
        System.out.println("==========================================================");
        System.out.println("   >>> AUCTION SYSTEM IS RUNNING SUCCESSFULLY! <<<        ");
        System.out.println("   >>> OPEN BROWSER AND CLICK HERE: http://localhost:8080/");
        System.out.println("==========================================================\n");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        BidNotificationServer.getInstance().stop();
        AuctionChatServer.getInstance().stop();
        DBConnection.shutdown();  // Connection pool cleanly band karo
        System.out.println("[Startup] Servers stopped.");
    }
}

