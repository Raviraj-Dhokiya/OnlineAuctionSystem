package com.auction.util;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * DBConnection - Connection Pool (Apache DBCP2)
 *
 * SECURITY NOTE: DB credentials hardcoded nahi hain.
 * - System.getenv("DB_URL") → DB URL environment variable se aata hai
 * - System.getenv("DB_USER") → Username environment variable se aata hai
 * - System.getenv("DB_PASS") → Password environment variable se aata hai
 *
 * SETUP: env.bat file banao (gitignore mein listed hai, commit mat karo):
 * set DB_URL=jdbc:oracle:thin:@<host>:<port>/<service>
 * set DB_USER=<your_db_username>
 * set DB_PASS=<your_db_password>
 *
 * startApp.bat automatically env.bat load karta hai agar woh present ho.
 *
 * PEHLE KI PROBLEM: Har DAO method mein DriverManager.getConnection() call hota
 * tha.
 * Oracle remote server par network connection banana 1-3 second leta tha.
 *
 * SOLUTION (Connection Pool):
 * - App startup par 3 connections ek baar Oracle se bante hain
 * - Har request pool se ek READY connection leta hai (0ms wait)
 * - Request complete hone par connection wapas pool mein chali jati hai
 * - Ek saath max 10 users serve kar sakta hai
 */
public class DBConnection {

    // ── Credentials environment variables se aate hain (NEVER hardcode!) ──────
    private static final String DB_URL = System.getenv("DB_URL");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASS = System.getenv("DB_PASS");
    // ─────────────────────────────────────────────────────────────────────────

    private static final BasicDataSource dataSource;

    // Static block: App start hote hi pool initialize hota hai
    static {
        if (DB_URL == null || DB_USER == null || DB_PASS == null) {
            throw new ExceptionInInitializerError(
                    "[DBConnection] FATAL: DB_URL, DB_USER, DB_PASS environment variables not set! " +
                            "Set them before starting the application.");
        }

        dataSource = new BasicDataSource();

        // --- Driver ---
        dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        dataSource.setUrl(DB_URL);
        dataSource.setUsername(DB_USER);
        dataSource.setPassword(DB_PASS);

        // --- Pool Size Settings ---
        dataSource.setInitialSize(3); // Startup par 3 connections ready rakho
        dataSource.setMaxTotal(10); // Ek saath max 10 connections
        dataSource.setMinIdle(2); // Kam se kam 2 idle connections hamesha ready
        dataSource.setMaxIdle(5); // Max 5 idle connections pool mein rakho
        dataSource.setMaxWaitMillis(5000); // 5 sec se zyada wait karna ho toh error

        // --- Connection Health Check ---
        dataSource.setValidationQuery("SELECT 1 FROM DUAL"); // Oracle ke liye health check query
        dataSource.setTestOnBorrow(true); // Pool se lene se pehle connection check karo
        dataSource.setTestWhileIdle(true); // Idle connections ka bhi test karo
        dataSource.setTimeBetweenEvictionRunsMillis(30000); // Har 30 sec mein stale connections hatao

        // --- Connection Timeout ---
        dataSource.setMinEvictableIdleTimeMillis(60000); // 60 sec se zyada idle ho toh hatao

        System.out.println("[DBConnection] Connection Pool initialized. InitialSize=3, MaxTotal=10");
    }

    /**
     * Pool se ek ready connection lo.
     * Caller ki jimmedari hai ise close() karna (try-with-resources use karo).
     * close() karne par connection pool mein wapas chali jati hai.
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Connection safely close karo (pool mein wapas jati hai).
     */
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close(); // DBCP pool mein wapas bhejta hai
            } catch (SQLException e) {
                System.err.println("[DBConnection] Error returning connection to pool: " + e.getMessage());
            }
        }
    }

    /**
     * Pool ka status check karo (debugging ke liye).
     */
    public static void printPoolStats() {
        System.out.println("[DBConnection Pool Stats]" +
                " Active=" + dataSource.getNumActive() +
                " Idle=" + dataSource.getNumIdle() +
                " MaxTotal=" + dataSource.getMaxTotal());
    }

    /**
     * App shutdown par pool band karo.
     * AppStartupListener.contextDestroyed() se call karo.
     */
    public static void shutdown() {
        try {
            dataSource.close();
            System.out.println("[DBConnection] Connection pool closed.");
        } catch (SQLException e) {
            System.err.println("[DBConnection] Error closing pool: " + e.getMessage());
        }
    }

    /**
     * Quick connectivity test.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("[DBConnection] Pool connection test: OK | DB=" +
                    conn.getMetaData().getDatabaseProductName());
            return true;
        } catch (SQLException e) {
            System.err.println("[DBConnection] Pool connection test FAILED: " + e.getMessage());
            return false;
        }
    }

    // BEKAR #2: main() test method removed — production code mein test entry point nahi hona chahiye.
    // testConnection() method separate diagnostic use ke liye abhi bhi hai.
}