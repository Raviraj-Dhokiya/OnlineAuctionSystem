package com.auction.mail;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * ════════════════════════════════════════════════════════
 *  AuctionMailService.java — Background Email Sender
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   - Logon ke pass JavaMail (Unit 7) APIs use karke real email bhejti hai.
 *   - Examples: Winner notification, Outbid alert.
 *
 * KAISE CHALTA HAI?
 *   - Gmail ka SMTP server use hota hai ("smtp.gmail.com").
 *   - Iske chalne ke liye Google account mein "App Passwords" ON hona zaruri hai.
 *   - Email bhejna lamba kaam ho sakta hai (network lag), isliye use background
 *     Thread mein bheja jata hai taaki server slow na ho (Non-blocking).
 * ════════════════════════════════════════════════════════
 */
public class AuctionMailService {

    // ── Credentials environment variables se aate hain (NEVER hardcode!) ──────
    // SETUP: env.bat mein add karo (gitignore mein listed hai, commit mat karo):
    //   set MAIL_FROM=yourname@gmail.com
    //   set MAIL_USER=yourname@gmail.com
    //   set MAIL_PASS=your_gmail_app_password
    // Google Account > Security > App Passwords se generate karo MAIL_PASS.
    private static final String SMTP_HOST  = "smtp.gmail.com";
    private static final int    SMTP_PORT  = 587;
    private static final String MAIL_FROM  = System.getenv("MAIL_FROM");
    private static final String MAIL_USER  = System.getenv("MAIL_USER");
    private static final String MAIL_PASS  = System.getenv("MAIL_PASS");

    // BUG #7 FIX: Improvement #6: Base URL env var se lo (hardcoded localhost hata do)
    private static final String AUCTION_BASE_URL = System.getenv("AUCTION_BASE_URL") != null
            ? System.getenv("AUCTION_BASE_URL")
            : "http://localhost:8080/OnlineAuctionSystem";

    // BUG #7 FIX: Startup pe null check — agar env vars set nahi hain toh
    // background thread mein silent NPE hone ke bajaye pehle hi warn karo.
    static {
        if (MAIL_FROM == null || MAIL_USER == null || MAIL_PASS == null) {
            System.err.println("[AuctionMailService] WARNING: MAIL_FROM, MAIL_USER, or MAIL_PASS " +
                    "environment variable(s) not set. Email notifications will be disabled. " +
                    "Add these to env.bat to enable mail sending.");
        }
    }
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Get a configured JavaMail Session (Unit 7 - JavaMail Environment Setup)
     */
    private static Session getMailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host",            SMTP_HOST);
        props.put("mail.smtp.port",            String.valueOf(SMTP_PORT));
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MAIL_USER, MAIL_PASS);
            }
        });
    }

    /**
     * Send winner notification email (Unit 7 - Sending mail)
     */
    public static void sendWinnerEmail(String toEmail, String winnerName,
                                       String itemTitle, double winningAmount) {
        String subject = "🎉 Congratulations! You won the auction for: " + itemTitle;
        String body = String.format(
            "<html><body>" +
            "<h2>Congratulations, %s!</h2>" +
            "<p>You have won the auction for <strong>%s</strong>.</p>" +
            "<p>Winning Bid: <strong>₹%.2f</strong></p>" +
            "<p>Please login to your account to complete the payment.</p>" +
            "<br><p>— Online Auction System</p>" +
            "</body></html>",
            winnerName, itemTitle, winningAmount
        );
        sendEmail(toEmail, subject, body);
    }



    /**
     * Send "outbid" notification - Unit 7 (Forwarding/Replying concept)
     */
    public static void sendOutbidEmail(String toEmail, String username,
                                       String itemTitle, double newBid) {
        String subject = "You've been outbid on: " + itemTitle;
        String body = String.format(
            "<html><body>" +
            "<h3>Hello %s,</h3>" +
            "<p>Someone placed a higher bid on <strong>%s</strong>.</p>" +
            "<p>New highest bid: <strong>₹%.2f</strong></p>" +
            // IMPROVEMENT #6 FIX: URL ab env var se aata hai, hardcoded localhost nahi
            "<p><a href='%s/DashboardServlet'>" +
            "Click here to bid again</a></p>" +
            "</body></html>",
            username, itemTitle, newBid, AUCTION_BASE_URL
        );
        sendEmail(toEmail, subject, body);
    }

    /**
     * Core Email bhejney ka method - (JavaMail APIs)
     * Java Mail API use karke HTML format mein email SMTP over TLS/SSL send karta hai.
     */
    private static void sendEmail(String to, String subject, String htmlBody) {
        // BUG #7 FIX: Agar mail credentials set nahi hain toh silently fail mat karo.
        if (MAIL_FROM == null || MAIL_USER == null || MAIL_PASS == null) {
            System.err.println("[Mail] Cannot send email to " + to +
                    " — mail credentials not configured (MAIL_FROM/MAIL_USER/MAIL_PASS).");
            return;
        }
        new Thread(() -> {
            try {
                Session session = getMailSession();
                MimeMessage message = new MimeMessage(session);

                message.setFrom(new InternetAddress(MAIL_FROM, "Auction System"));
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
                message.setSubject(subject, "UTF-8");
                message.setContent(htmlBody, "text/html; charset=UTF-8");

                Transport.send(message);
                System.out.println("[Mail] Sent to: " + to + " | Subject: " + subject);

            } catch (Exception e) {
                System.err.println("[Mail] Failed to send to " + to + ": " + e.getMessage());
            }
        }, "MailSenderThread").start();
    }
}
