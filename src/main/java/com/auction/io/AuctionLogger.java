package com.auction.io;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ════════════════════════════════════════════════════════
 *  AuctionLogger.java — System Logs & CSV Export (File I/O)
 * ════════════════════════════════════════════════════════
 *
 * SIKHNE LAYAK CONCEPTS (Java I/O - Unit 2):
 *   1. Char-basd Streams (FileWriter, BufferedWriter) -> Text likhne ke liye
 *   2. Byte-based Streams (FileInputStream, BufferedInputStream) -> Bytes mein file read karne ke liye
 *   3. RandomAccessFile -> File mein kisi bhi location se direct read/write karna
 *   4. Asynchronous I/O (Thread-based) -> Background thread log file update karti hai
 *      taki web app/main program slow na ho (non-blocking).
 * ════════════════════════════════════════════════════════
 */
public class AuctionLogger {

    private static final String LOG_DIR  = System.getProperty("user.home") +
                                           File.separator + "auction_logs";
    private static final String LOG_FILE = LOG_DIR + File.separator + "auction.log";

    // Thread-safe Async Logging Queue (Background writing ke liye)
    // Jab kisi ko log likhna hota hai, wo direct file access karne ke bajaye message yahan (queue mein) dalta hai.
    private static final java.util.concurrent.BlockingQueue<String> logQueue =
        new java.util.concurrent.LinkedBlockingQueue<>();

    // Start background writer thread
    static {
        Path dir = Paths.get(LOG_DIR);
        try {
            if (!Files.exists(dir)) Files.createDirectories(dir);
        } catch (IOException e) {
            System.err.println("[Logger] Cannot create log dir: " + e.getMessage());
        }

        Thread logWriter = new Thread(() -> {
            while (true) {
                try {
                    // queue se ek log message uthao (agar queue khali hai toh thread sleep/wait form me rehti hai - block karti hai)
                    String entry = logQueue.take();
                    writeToFile(entry); // message file mein likho
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "LogWriterThread");
        logWriter.setDaemon(true);
        logWriter.start();
    }

    /**
     * Add log entry to queue (non-blocking for callers)
     */
    public static void log(String message) {
        String entry = "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "] " + message;
        logQueue.offer(entry);
        System.out.println("[AuctionLog] " + entry);
    }

    /**
     * Write to log file using Character-based Stream (Unit 2)
     * FileWriter (char stream) + BufferedWriter (buffered = efficient)
     */
    private static void writeToFile(String entry) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true); // append=true
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(entry);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            System.err.println("[Logger] Write error: " + e.getMessage());
        }
    }

    /**
     * Read last N lines from log file efficiently by reading from the end.
     */
    public static List<String> readLastNLines(int n) {
        List<String> lines = new ArrayList<>();
        File logFile = new File(LOG_FILE);
        if (!logFile.exists() || n <= 0) return lines;

        try (org.apache.commons.io.input.ReversedLinesFileReader reader = 
             new org.apache.commons.io.input.ReversedLinesFileReader(logFile, java.nio.charset.StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null && lines.size() < n) {
                lines.add(0, line); // Add to beginning to keep chronological order
            }
        } catch (Exception e) {
            System.err.println("[Logger] Read error: " + e.getMessage());
        }

        return lines;
    }

    /**
     * Export auction results to CSV (Unit 2 - Sequential File Writing)
     * Called by Admin to export all winner data
     */
    public static void exportResultsToCSV(List<String[]> rows, String filename) {
        String csvPath = LOG_DIR + File.separator + filename;
        try (FileWriter fw = new FileWriter(csvPath);
             BufferedWriter bw = new BufferedWriter(fw)) {

            // Header
            bw.write("Item ID,Item Title,Winner,Winning Amount,Payment Status,Awarded At");
            bw.newLine();

            for (String[] row : rows) {
                bw.write(String.join(",", row));
                bw.newLine();
            }

            bw.flush();
            System.out.println("[Logger] CSV exported: " + csvPath);

        } catch (IOException e) {
            System.err.println("[Logger] CSV export error: " + e.getMessage());
        }
    }

    /**
     * Use RandomAccessFile to read a specific byte range in the log (Unit 2)
     */
    public static String readLogRange(long startByte, int length) {
        File logFile = new File(LOG_FILE);
        if (!logFile.exists()) return "";

        try (RandomAccessFile raf = new RandomAccessFile(logFile, "r")) {
            raf.seek(startByte);
            byte[] buffer = new byte[length];
            int bytesRead  = raf.read(buffer);
            return new String(buffer, 0, bytesRead);
        } catch (IOException e) {
            System.err.println("[Logger] RandomAccess error: " + e.getMessage());
            return "";
        }
    }
}
