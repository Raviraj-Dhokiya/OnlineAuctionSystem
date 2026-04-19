<div align="center">

  <img src="src/main/webapp/css/logo.png" alt="Auction Logo" width="90" />

  <h1>🏆 Online Auction System</h1>
  <p><strong>Advanced Java Technologies — Academic Project</strong></p>

  <p>
    <img src="https://img.shields.io/badge/Java-11+-orange?style=flat-square&logo=java" />
    <img src="https://img.shields.io/badge/Maven-Build-blue?style=flat-square&logo=apachemaven" />
    <img src="https://img.shields.io/badge/Oracle%20DB-21c%20XE-red?style=flat-square&logo=oracle" />
    <img src="https://img.shields.io/badge/Tomcat-9.x-yellow?style=flat-square&logo=apachetomcat" />
    <img src="https://img.shields.io/badge/Jersey%20JAX--RS-REST%20API-green?style=flat-square" />
  </p>

</div>

---

## 📌 Overview

**Online Auction System** is a full-stack Java web application demonstrating every major topic of the Advanced Java Technologies curriculum. It features real-time TCP/UDP socket bidding, Java RMI, REST APIs, JDBC with connection pooling, Java Mail, Design Patterns, and more — all wired together in a clean, n-tier MVC architecture.

> Developed as an academic project for *Advanced Java Technologies* using Java Servlets, JSP, Oracle DB, and Maven.

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 11, Java Servlets (javax.servlet 4.0.1), JSP 2.3, JSTL 1.2 |
| **Database** | Oracle Database 21c XE, Raw JDBC (ojdbc8 19.3), Apache DBCP2 + Pool2 |
| **Networking** | TCP Sockets (Chat + Bid Notifications), UDP Multicast (Expiry Alerts) |
| **RMI** | Java RMI Registry (Port 1099) — Remote bidding interface |
| **REST API** | Jersey JAX-RS 2.41, Jackson JSON 2.15.2 |
| **Email** | JavaMail (javax.mail 1.6.2) — Bid winner notifications |
| **Security** | BCrypt (jBCrypt 0.4), CSRF token validation, Session-based Auth |
| **File Handling** | Apache Commons FileUpload 1.5, Commons IO 2.13.0 |
| **PDF Export** | iText PDF 5.5.13.3 — Downloadable bid history |
| **Frontend** | HTML5, Vanilla CSS3, Vanilla JavaScript |
| **Build & Deploy** | Maven 3, Apache Tomcat 7 Maven Plugin (embedded), WAR packaging |

---

## 🗂️ Project Structure

```text
OnlineAuctionSystem/
├── pom.xml                          # Maven build config & all dependencies
├── database_schema.sql              # Full Oracle DB schema (tables, sequences)
├── env.bat                          # DB credentials (created by you, git-ignored)
├── startApp.bat                     # One-click Windows startup script
├── README.md                        # Project documentation
└── src/main/
    ├── java/com/auction/
    │   ├── model/                   # POJOs / Data Models
    │   │   ├── AuctionItem.java
    │   │   ├── AutoBid.java
    │   │   ├── Bid.java
    │   │   ├── User.java
    │   │   ├── Watchlist.java
    │   │   └── Winner.java
    │   ├── dao/                     # JDBC Data Access Objects
    │   │   ├── AuctionItemDAO.java
    │   │   ├── AutoBidDAO.java
    │   │   ├── BidDAO.java
    │   │   ├── UserDAO.java
    │   │   ├── WatchlistDAO.java
    │   │   └── WinnerDAO.java
    │   ├── servlet/                 # HTTP Controllers (Servlets)
    │   │   ├── AuctionItemServlet.java
    │   │   ├── AutoBidServlet.java
    │   │   ├── AutoBidProcessorServlet.java
    │   │   ├── BidServlet.java
    │   │   ├── BidPollServlet.java
    │   │   ├── ChatSendServlet.java
    │   │   ├── ChatPollServlet.java
    │   │   ├── DashboardServlet.java
    │   │   ├── DownloadBidsPdfServlet.java
    │   │   ├── LoginServlet.java
    │   │   ├── LogoutServlet.java
    │   │   ├── PaymentServlet.java
    │   │   ├── ProfileServlet.java
    │   │   ├── RegisterServlet.java
    │   │   ├── SearchServlet.java
    │   │   ├── WatchlistServlet.java
    │   │   ├── AdminServlet.java
    │   │   └── AdminLoginServlet.java
    │   ├── filter/                  # Servlet Filters
    │   │   └── AuthFilter.java      # Session-based authentication guard
    │   ├── network/                 # TCP/UDP Socket Servers
    │   │   ├── AppStartupListener.java
    │   │   ├── AuctionChatServer.java
    │   │   ├── AuctionExpiryChecker.java
    │   │   └── BidNotificationServer.java
    │   ├── rmi/                     # Java RMI
    │   │   ├── AuctionRMIInterface.java
    │   │   ├── AuctionRMIServer.java
    │   │   └── AuctionRMIClient.java
    │   ├── mail/                    # Email Notifications
    │   │   └── AuctionMailService.java
    │   ├── webservice/              # REST API Endpoints (Jersey JAX-RS)
    │   │   ├── AuctionRestAPI.java
    │   │   ├── BidRestAPI.java
    │   │   └── WinnerRestAPI.java
    │   ├── io/                      # File I/O & Logging
    │   │   └── AuctionLogger.java
    │   ├── security/                # Password Hashing & Utils
    │   │   └── SecurityUtil.java
    │   ├── patterns/                # GoF Design Patterns
    │   │   ├── AuctionBidNotifier.java  (Observer)
    │   │   ├── AuctionItemBuilder.java  (Builder)
    │   │   ├── AuctionItemFactory.java  (Factory)
    │   │   └── PremiumBidDecorator.java (Decorator)
    │   └── util/                    # DB Connection Pool
    │       └── DBConnection.java    (Singleton + DBCP2)
    └── webapp/
        ├── css/
        │   ├── style.css            # Global dark-themed stylesheet
        │   └── logo.png             # Application logo
        ├── js/
        │   └── bid-live.js          # Real-time bid polling (AJAX)
        └── WEB-INF/
            ├── web.xml              # Deployment descriptor (Servlets, Filters, Jersey)
            └── views/              # JSP Templates
                ├── dashboard.jsp
                ├── item-detail.jsp  # Live bidding UI + chat
                ├── add-item.jsp
                ├── search-results.jsp
                ├── watchlist.jsp
                ├── payment.jsp
                ├── profile.jsp
                ├── login.jsp
                ├── register.jsp
                ├── admin.jsp
                ├── admin-login.jsp
                ├── error-403.jsp
                ├── error-404.jsp
                └── error-500.jsp
```

---

## 📖 Syllabus / Unit-Wise Implementation

| Unit | Topic | Implementation |
|---|---|---|
| **Unit 1** | N-Tier Application Architecture | Client → Servlet → DAO → Oracle DB pipeline; JSP Views separated from business logic |
| **Unit 2** | Java I/O | `AuctionLogger.java` — FileStreams, ReversedLinesFileReader for memory-efficient backward log reading, CSV export |
| **Unit 3** | Java Networking | `BidNotificationServer.java` — TCP broadcast for live bids; `AuctionChatServer.java` — per-auction TCP chat rooms; `AuctionExpiryChecker.java` — UDP "Ending Soon" multicast alerts |
| **Unit 4** | JDBC | `DBConnection.java` (Singleton + Apache DBCP2 pool); `PreparedStatement`, `CallableStatement`, `ResultSet`, BLOB image storage across all DAOs |
| **Unit 5** | Java RMI | `AuctionRMIServer` + `AuctionRMIClient` over port 1099 — remote bidding via console |
| **Unit 6** | Servlets & JSP | 18 Servlets covering all features; Sessions + Cookies for auth; `AuthFilter.java` guards all protected URLs; `web.xml` deployment descriptor; JSTL + EL in JSP views |
| **Unit 7** | REST APIs & Java Mail | `AuctionRestAPI`, `BidRestAPI`, `WinnerRestAPI` (Jersey JAX-RS, JSON); `AuctionMailService.java` for email alerts on auction end |
| **Unit 8** | Design Patterns | Observer (`AuctionBidNotifier`), Factory (`AuctionItemFactory`), Builder (`AuctionItemBuilder`), Decorator (`PremiumBidDecorator`), Singleton (`DBConnection`, `AuctionManager`) |

---

## ✨ Key Features

- 🔐 **Authentication** — BCrypt-hashed passwords, session/cookie management, CSRF-protected forms
- 📦 **Auction Item Management** — Create, edit, delete listings with image upload (multipart/form-data)
- 💰 **Live Bidding** — Real-time bid updates via AJAX polling (`bid-live.js` + `BidPollServlet`)
- 🤖 **Auto-Bid** — Set a maximum bid; `AutoBidProcessorServlet` automatically outbids on your behalf
- 💬 **Per-Auction Chat** — TCP socket chat rooms per item, polled via `ChatPollServlet`
- 🔔 **Notifications** — UDP socket "Ending Soon" alerts + Java Mail winner emails
- 👀 **Watchlist** — Save and track favourite auctions
- 🔍 **Search** — Full-text item search via `SearchServlet`
- 📄 **PDF Downloads** — `DownloadBidsPdfServlet` exports bid history via iText PDF
- 🛡️ **Admin Panel** — Manage users, items, and bids from `admin.jsp` (separate admin login)
- 🌐 **REST API** — JSON endpoints for items, bids, and winners (Jersey JAX-RS)
- 💳 **Mock Payment** — `PaymentServlet` simulates end-to-end payment workflow after winning

---

## ⚙️ Recent Optimizations & Fixes

- **N+1 Query Elimination** — Replaced loop-based DB calls on the dashboard with a single `LEFT JOIN` subquery inside `AuctionItemDAO`, resolving `ORA-22848` CLOB issues and boosting performance drastically.
- **Memory-Efficient Log Reading** — `AuctionLogger.java` uses Apache Commons IO `ReversedLinesFileReader` to read large logs bottom-up without loading the entire file into RAM.
- **REST API Security Hardening** — Removed insecure session-less endpoints from `AuctionRestAPI.java`; introduced dedicated `BidRestAPI.java` and `WinnerRestAPI.java` with strict session verification.
- **Seller-Bidding Prevention** — `BidServlet.java` now natively blocks sellers from bidding on their own listings.
- **Factory Pattern Integration** — `AuctionItemFactory.java` fully wired into `AuctionItemServlet.java`, reducing redundant instantiation logic.
- **CSRF Fix for File Uploads** — Correctly unwraps `csrfToken` from `multipart/form-data` chunks, eliminating 403 Forbidden errors during image uploads.
- **Dependency Cleanup** — Removed dead `hibernate-core` and `log4j-core` from `pom.xml`, drastically reducing WAR size.

---

## 🚀 Setup & Run Guide

### 1️⃣ Prerequisites

Make sure the following are installed:

| Tool | Version |
|---|---|
| Java JDK | 11 or higher |
| Oracle Database XE | 21c (or compatible) |
| Maven | 3.6+ |
| (Optional) Apache Tomcat | 9.x |

---

### 2️⃣ Database Setup

1. Open **SQL Developer** or **SQL\*Plus** and connect as `SYSTEM`.
2. Create the project user:
   ```sql
   CREATE USER auction_user IDENTIFIED BY auction_pass;
   GRANT CONNECT, RESOURCE, DBA TO auction_user;
   ```
3. Reconnect as `auction_user`.
4. Run the SQL schema file from the project root:
   ```sql
   @database_schema.sql
   ```
   This creates all tables: `USERS`, `AUCTION_ITEMS`, `BIDS`, `AUTO_BIDS`, `WATCHLIST`, `WINNERS`, etc.

---

### 3️⃣ Configure `env.bat`

Create a file named **`env.bat`** in the project root with your DB credentials:

```bat
:: env.bat — DO NOT commit this file to git
set DB_URL=jdbc:oracle:thin:@localhost:1521:XE
set DB_USER=auction_user
set DB_PASS=auction_pass
```

> ⚠️ `env.bat` is already in `.gitignore` — never push your credentials.

---

### 4️⃣ Run the Application

Open a terminal in the project root and run:

```bat
startApp.bat
```

This script will:
1. Load `env.bat` (DB credentials)
2. Run `mvn clean package` to compile and build the WAR
3. Deploy and start the embedded Tomcat server

---

### 🌐 Access the Application

Once started, open your browser and go to:

| Service | URL / Port |
|---|---|
| **Web App** | [http://localhost:8080](http://localhost:8080) |
| **RMI Registry** | Port `1099` |
| **Bid Notification Socket** | Port `9090` |
| **Chat Socket** | Port `9091` |

---

### 🧭 Quick Start Flow

1. **Register** a new user → `/register`
2. **Login** → `/login`
3. **Browse** active auctions on the **Dashboard**
4. **Add Item** to create your own auction listing
5. **Bid** on any live item — bids refresh live every 3 seconds
6. **Chat** in the per-auction chat room
7. **Watchlist** items you're interested in
8. **Download** your bid history as a PDF
9. When you win — complete the **Mock Payment** flow

---

## 🔗 REST API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/auctions` | List all active auction items (JSON) |
| `GET` | `/api/auctions/{id}` | Get details of a specific item |
| `POST` | `/api/bids` | Place a bid (session-authenticated) |
| `GET` | `/api/winners` | Get list of past auction winners |

---

<div align="center">
  <br/>
  <b>Built to demonstrate Advanced Java Engineering</b><br/>
  <sub>Java Servlets • JSP • JDBC • Sockets • RMI • REST • Design Patterns</sub>
</div>