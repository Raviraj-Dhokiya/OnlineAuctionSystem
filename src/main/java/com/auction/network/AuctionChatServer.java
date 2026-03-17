package com.auction.network;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * ════════════════════════════════════════════════════════
 *  AuctionChatServer.java — Live Chat Server (Sockets)
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   - Ek specific auction item par bidders aapas mein chat kar sakein,
 *     iske liye TCP Socket server banati hai.
 *   - Ye server alag thread mein port 9092 par run hota hai.
 *
 * PROTOCOL (Sandesh Bhejne ka Tareeka):
 *   1. Client (Browser/App) judta hai aur bhejta hai: JOIN:<itemId>:<username>
 *   2. Client message likhta hai: MSG:<itemId>:<username>:<text>
 *   3. Server us room (itemId) ke sab logo ko bhejta hai: CHAT:<itemId>:<username>:<text>:<timestamp>
 *
 * CONCEPT (Java Networking - Unit 3):
 *   - ServerSocket: Server banakar clients ka intezaar karna
 *   - Socket: Ek client ka connection
 *   - PrintWriter / BufferedReader: Message bhejna aur padhna (I/O Streams)
 * ════════════════════════════════════════════════════════
 */
public class AuctionChatServer {

    private static final int                    CHAT_PORT = 9092;
    private static       AuctionChatServer      instance;

    private ServerSocket                         serverSocket;
    private boolean                              running = false;

    // itemId -> set of client writers (for per-auction chat rooms)
    private final Map<Integer, Set<PrintWriter>> rooms =
        new ConcurrentHashMap<>();

    // All clients (for admin broadcast)
    private final Set<PrintWriter> allClients = ConcurrentHashMap.newKeySet();

    private final ExecutorService threadPool  = Executors.newCachedThreadPool();

    public static synchronized AuctionChatServer getInstance() {
        if (instance == null) instance = new AuctionChatServer();
        return instance;
    }

    private AuctionChatServer() {}

    public void start() {
        if (running) return;
        // Background pool mein naya thread shuru karo taaki main app block na ho
        threadPool.execute(() -> {
            try {
                serverSocket = new ServerSocket(CHAT_PORT);
                running = true;
                System.out.println("[ChatServer] Listening on port " + CHAT_PORT);
                while (running) {
            	    // accept() — Jab tak naya client connect nahi hota, yahan code ruk jata hai (blocking)
                    Socket client = serverSocket.accept();
                    // Naya client mila? Use kisi dusre thread ko de do aage baat karne ke liye
                    threadPool.execute(new ChatHandler(client));
                }
            } catch (IOException e) {
                if (running) System.err.println("[ChatServer] Error: " + e.getMessage());
            }
        });
    }

    public void stop() {
        running = false;
        try { if (serverSocket != null) serverSocket.close(); }
        catch (IOException ignored) {}
        threadPool.shutdown();
    }

    private void broadcastToRoom(int itemId, String message) {
        Set<PrintWriter> room = rooms.get(itemId);
        if (room == null) return;
        Iterator<PrintWriter> it = room.iterator();
        while (it.hasNext()) {
            PrintWriter pw = it.next();
            pw.println(message);
            pw.flush();
            if (pw.checkError()) it.remove();
        }
    }

    // ── Chat Handler ─────────────────────────────────────────────────────────

    private class ChatHandler implements Runnable {
        private final Socket socket;
        private PrintWriter  out;
        private int          currentItemId = -1;
        private String       username      = "Unknown";

        ChatHandler(Socket socket) { this.socket = socket; }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

                allClients.add(out);
                String line;

                while ((line = in.readLine()) != null) {
                    if (line.startsWith("JOIN:")) {
                        // JOIN:<itemId>:<username>
                        String[] parts = line.split(":", 3);
                        currentItemId = Integer.parseInt(parts[1]);
                        username      = parts[2];

                        rooms.computeIfAbsent(currentItemId,
                            k -> ConcurrentHashMap.newKeySet()).add(out);

                        broadcastToRoom(currentItemId,
                            "SYSTEM:" + currentItemId + ":" + username + " joined the chat.");

                    } else if (line.startsWith("MSG:")) {
                        // MSG:<itemId>:<username>:<text>
                        String[] parts = line.split(":", 4);
                        int    itemId  = Integer.parseInt(parts[1]);
                        String user    = parts[2];
                        String text    = parts[3];
                        String time    = new java.util.Date().toString();

                        broadcastToRoom(itemId,
                            "CHAT:" + itemId + ":" + user + ":" + text + ":" + time);
                    }
                }

            } catch (IOException e) {
                System.out.println("[ChatServer] Client disconnected: " + username);
            } finally {
                allClients.remove(out);
                if (currentItemId != -1 && rooms.containsKey(currentItemId)) {
                    rooms.get(currentItemId).remove(out);
                    broadcastToRoom(currentItemId,
                        "SYSTEM:" + currentItemId + ":" + username + " left.");
                }
                try { socket.close(); } catch (IOException ignored) {}
            }
        }
    }
}
