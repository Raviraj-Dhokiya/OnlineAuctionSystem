package com.auction.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 *  AuthFilter.java — Security Gate (HTTP Filter)
 * ════════════════════════════════════════════════════════
 *
 * YEH CLASS KYA KARTI HAI?
 *   - Ek SECURITY GUARD ki tarah kaam karta hai jo har request
 *     ko rok ke check karta hai: "Kya user logged in hai?"
 *   - Bina login ke koi bhi Dashboard, Admin, etc. nahi dekh sakta.
 *   - @WebFilter("/*") → matlab YEH FILTER SABSE PEHLE CHALTA HAI,
 *     har URL par (login, register, css...sab par).
 *
 * KAISE KAAM KARTA HAI?
 *   Browser request → AuthFilter.doFilter() → Servlet
 *
 * PUBLIC PATHS (bina login ke accessible):
 *   /LoginServlet    → Login page
 *   /RegisterServlet → Register page
 *   /css/            → CSS files (styling)
 *   /js/             → JavaScript files
 *   /images/         → Images
 *
 * BAAKI SAB → Session check → Login nahi? Redirect to /LoginServlet
 * ════════════════════════════════════════════════════════
 */
@WebFilter("/*")  // /* matlab: App ki EVERY URL par yeh filter lagega
public class AuthFilter implements Filter {

    // In URLs par login check NAHI hoga (public access)
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/LoginServlet",
        "/RegisterServlet",
        "/index.jsp",
        "/css/",
        "/js/",
        "/images/"
    );

    /**
     * doFilter() — har request par automatically call hota hai.
     *
     * FLOW:
     *  1. Request kis URL par hai? → check karo
     *  2. Public path hai? → seedha allow karo (chain.doFilter)
     *  3. Private path hai? → Session mein "loggedUser" hai?
     *       - Haan? → allow karo
     *       - Nahi? → /LoginServlet par redirect karo
     *  4. Admin page? → extra check: role "ADMIN" hona chahiye
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        // Raw request/response ko HTTP-specific type mein cast karo
        HttpServletRequest  req = (HttpServletRequest)  request;
        HttpServletResponse res = (HttpServletResponse) response;

        // User kis URL par ja raha hai? (jaise "/DashboardServlet")
        String path = req.getServletPath();

        // Kya yeh URL public hai? (login, register, css, js...)
        // anyMatch → list mein se koi bhi path match kare toh true
        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith);

        if (isPublic) {
            // Public page hai → seedha aage bhejo, login check ki zarurat nahi
            chain.doFilter(request, response);
            return;
        }

        // Private page hai → session check karo
        // getSession(false) → false matlab: naya session mat banao, sirf check karo
        HttpSession session = req.getSession(false);
        boolean loggedIn = (session != null &&
                            session.getAttribute("loggedUser") != null);

        if (!loggedIn) {
            // Session nahi hai → user logged in nahi hai → Login page bhejo
            res.sendRedirect(req.getContextPath() + "/LoginServlet");
            return;
        }

        // Admin-only pages: /AdminServlet → sirf ADMIN role wale dekh sakte hain
        if (path.startsWith("/AdminServlet")) {
            String role = (String) session.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                // Normal user admin try kare → Dashboard par bhejo with error
                res.sendRedirect(req.getContextPath() +
                    "/DashboardServlet?error=access_denied");
                return;
            }
        }

        // Authenticated pages ka caching rok do (browser back button se nahi aana chahiye)
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma",        "no-cache");
        res.setHeader("Expires",       "0");

        // Sab check pass → request aage Servlet tak bhejo
        chain.doFilter(request, response);
    }

    // init() aur destroy() ke koi special kaam nahi hain yahan
    @Override public void init(FilterConfig f)    {}
    @Override public void destroy()               {}
}
