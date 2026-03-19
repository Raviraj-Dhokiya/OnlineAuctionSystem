package com.auction.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * ════════════════════════════════════════════════════════
 *  SecurityUtil.java — Security Helper Class
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   Security se related sab kaam yahan hote hain:
 *   1. Password ko hash karna (plain text kabhi DB mein save nahi hota)
 *   2. Login ke time hashed password verify karna
 *   3. User ka input clean karna (XSS attack rokna)
 *   4. Email aur password format validate karna
 *
 * BCrypt KYA HOTA HAI?
 *   - Ek strong hashing algorithm jo password ko unreadable banata hai.
 *   - Example: "Hello@123" → "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
 *   - One-way: hash se original password nahi nikala ja sakta.
 *   - BCrypt.checkpw() hash compare karta hai bina decrypt kiye.
 *
 * XSS ATTACK KYA HOTA HAI?
 *   - User agar input mein HTML/JS code type kare jaise: <script>alert('hacked')</script>
 *   - sanitizeInput() usse safe text mein convert karta hai: &lt;script&gt;...
 *   - Isse browser script execute nahi karta.
 * ════════════════════════════════════════════════════════
 */
public class SecurityUtil {

    // BCrypt ka "cost factor" — 10 ka matlab hai hashing 2^10 rounds mein hogi
    // Zyada rounds → slower hashing → brute force attack mushkil hoga
    private static final int BCRYPT_ROUNDS = 10;

    /**
     * Plain password ko BCrypt hash mein convert karo.
     *
     * USE: RegisterServlet mein — user ka naya password hash karke DB mein save karo.
     * Example: "Hello@123" → "$2a$10$xyz..." (hamesha alag output aata hai)
     */
    public static String hashPassword(String plainPassword) {
        // BCrypt.gensalt() ek random "salt" banata hai (extra security ke liye)
        // BCrypt.hashpw() salt + password ko mila kar hash karta hai
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Login ke time user ka typed password aur DB ka hashed password compare karo.
     *
     * USE: LoginServlet mein — DB se user nikalo, phir yeh check karo.
     * BCrypt.checkpw() internally same process karke match dekhta hai.
     *
     * @param plainPassword  → user ne jo type kiya
     * @param hashedPassword → DB mein jo stored hai
     * @return true = match, false = galat password
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            // Agar hash format galat ho toh false return karo (safe fallback)
            return false;
        }
    }

    /**
     * Password strong hai ya nahi check karo.
     * Rules:
     *   - Minimum 8 characters
     *   - Kam se kam ek digit (0-9)
     *   - Kam se kam ek uppercase letter (A-Z)
     *   - Kam se kam ek special character (!@#$%^&*...)
     *
     * USE: RegisterServlet mein — weak password accept nahi hoga.
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;

        // .chars() → password ke har character ka int value milta hai
        // anyMatch() → koi bhi character condition match kare toh true
        boolean hasDigit   = password.chars().anyMatch(Character::isDigit);
        boolean hasUpper   = password.chars().anyMatch(Character::isUpperCase);
        boolean hasSpecial = password.chars().anyMatch(c -> "!@#$%^&*()_+-=".indexOf(c) >= 0);

        return hasDigit && hasUpper && hasSpecial;
    }

    /**
     * User ke input se HTML special characters hata do (XSS Prevention).
     *
     * USE: Har servlet mein user input ko DB ya page par dikhane se pehle.
     * Transform:
     *   &  →  &amp;
     *   <  →  &lt;
     *   >  →  &gt;
     *   "  →  &quot;
     *   '  →  &#x27;
     */
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.replace("&",  "&amp;")
                    .replace("<",  "&lt;")
                    .replace(">",  "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'",  "&#x27;");
    }

    /**
     * Email format valid hai ya nahi.
     * Pattern: user@domain.com (2-6 character extension)
     *
     * USE: RegisterServlet mein — galat format email accept nahi hoga.
     */
    public static boolean isValidEmail(String email) {
        // Regex: [\w.-]+ = username part, @ = at sign,
        //        [\w.-]+ = domain, \. = dot, [a-zA-Z]{2,6} = .com/.in/.org etc
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$");
    }


}
