package com.auction.network;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * ════════════════════════════════════════════════════════
 *  BidNotificationServer.java — Live Bid Update Server
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   - Jaise hi koi bid lagata hai, yeh server baaki sab jude hue logo (browsers)
 *     ko turant bata deta hai. (Taaki unki screen par naya price dikh jaye).
 *   - Isse page refresh karne ki zarurat nahi padti.
 *
 * SIKHNE LAYAK CONCEPTS (Java Networking - Unit 3):
 *   1. TCP Sockets (ServerSocket, Socket): Reliable connection ke liye.
 *   2. UDP Sockets (DatagramSocket, DatagramPacket): Ek sath sab networks
 *      ko alert bhejane ke liye (Broadcast) jisme reliability zaroori nahi.
 *   3. Singleton Pattern (Unit 8): Pure system mein is class ka 1 hi instance hoga.
 * ════════════════════════════════════════════════════════
 */
public class BidNotificationServer {

    private static final int    PORT     = 9090;
    private static BidNotificationServer instance;

    private ServerSocket                 serverSocket;
    private final Set<PrintWriter>       clients = ConcurrentHashMap.newKeySet();
    private boolean                      running = false;

    // Thread pool for handling multiple clients (Unit 3 - Thread-based I/O)
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    // ── Singleton (Unit 8) ───────────────────────────────────────────────────
    public static synchronized BidNotificationServer getInstance() {
        if (instance == null) instance = new BidNotificationServer();
        return instance;
    }

    private BidNotificationServer() {}

    // ── Start Server ─────────────────────────────────────────────────────────

    public void start() {
        if (running) return;
        threadPool.execute(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                running = true;
                System.out.println("[BidServer] Listening on port " + PORT);

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(new ClientHandler(clientSocket));
                }
            } catch (IOException e) {
                if (running) System.err.println("[BidServer] Error: " + e.getMessage());
            }
        });
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
            threadPool.shutdown();
        } catch (IOException e) {
            System.err.println("[BidServer] Stop error: " + e.getMessage());
        }
    }

    // ── Broadcast new bid to ALL connected clients ────────────────────────────

    public void broadcastBidUpdate(int itemId, String bidderName,
                                   double amount, String itemTitle) {
        // BUG #6 FIX: escapeJson() se special chars (" and \) sanitize karo
        // Bina escaping ke, malicious username se JSON break ho sakta tha.
        String msg = String.format(
            "{\"type\":\"BID\",\"itemId\":%d,\"bidder\":\"%s\",\"amount\":%.2f,\"item\":\"%s\",\"time\":\"%s\"}",
            itemId, escapeJson(bidderName), amount, escapeJson(itemTitle),
            new java.util.Date().toString()
        );
        broadcast(msg);
    }

    public void broadcastAuctionEnding(int itemId, String itemTitle, long secondsLeft) {
        String msg = String.format(
            "{\"type\":\"ENDING\",\"itemId\":%d,\"item\":\"%s\",\"secondsLeft\":%d}",
            itemId, escapeJson(itemTitle), secondsLeft
        );
        broadcast(msg);
    }

    /** BUG #6 FIX: JSON string escaping — prevent JSON injection. */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void broadcast(String message) {
        Iterator<PrintWriter> it = clients.iterator();
        while (it.hasNext()) {
            PrintWriter pw = it.next();
            try {
                pw.println(message);
                pw.flush();
                if (pw.checkError()) it.remove(); // remove disconnected client
            } catch (Exception e) {
                it.remove();
            }
        }
        System.out.println("[BidServer] Broadcast to " + clients.size() +
                           " clients: " + message);
    }

    // ── Client Handler (one thread per client) ────────────────────────────────

    private class ClientHandler implements Runnable {
        private final Socket socket;

        ClientHandler(Socket socket) { this.socket = socket; }

        @Override
        public void run() {
            PrintWriter out = null;
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                clients.add(out);

                String clientAddr = socket.getInetAddress().getHostAddress();
                System.out.println("[BidServer] Client connected: " + clientAddr +
                                   " | Total: " + clients.size());

                // Send welcome message
                out.println("{\"type\":\"CONNECTED\",\"msg\":\"Connected to Auction Bid Server\"}");
                out.flush();

                // Keep connection alive - read incoming (heartbeat)
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    // Client can send "PING" to keep alive
                    if ("PING".equals(line.trim())) {
                        out.println("{\"type\":\"PONG\"}");
                        out.flush();
                    }
                }

            } catch (IOException e) {
                System.out.println("[BidServer] Client disconnected.");
            } finally {
                if (out != null) clients.remove(out);
                try { socket.close(); } catch (IOException ignored) {}
            }
        }
    }

    // ── UDP Broadcast (Unit 3 - UDP Concepts) ───────────────────────────────
    // UDP: Data "packet" format mein bhejta hai, connection banana zaroori nahi.
    // Tej hota hai, par pahunchega iski 100% guarantee nahi hoti.
    public void sendUDPAlert(String message) {
        try (DatagramSocket udpSocket = new DatagramSocket()) {
            udpSocket.setBroadcast(true); // Broadcast ON (sabko bhejo)
            byte[] data   = message.getBytes();
            InetAddress addr = InetAddress.getByName("255.255.255.255");
            DatagramPacket packet = new DatagramPacket(data, data.length, addr, 9091);
            udpSocket.send(packet);
            System.out.println("[BidServer] UDP alert sent: " + message);
        } catch (IOException e) {
            System.err.println("[BidServer] UDP error: " + e.getMessage());
        }
    }
}
