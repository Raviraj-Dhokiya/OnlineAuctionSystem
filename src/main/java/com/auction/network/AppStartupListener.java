package com.auction.network;

import com.auction.util.DBConnection;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * AppStartupListener - Unit 6 (Servlet lifecycle)
 * Starts BidNotificationServer and AuctionChatServer
 * when Tomcat deploys the application.
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
