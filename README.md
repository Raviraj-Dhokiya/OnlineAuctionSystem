# 🏆 Online Auction Management System

**Academic Project — BE Computer Science (Advanced Java Technologies)**
Maharaja Sayajirao University of Baroda — SS BE III

---

## 📚 Syllabus Coverage

| Unit | Topic | Where Used |
|------|-------|-----------|
| Unit 1 | Introduction — Application Programming | Architecture (n-tier: Client → Servlet → DAO → DB) |
| Unit 2 | Java I/O | `AuctionLogger.java` — File streams, RandomAccessFile, CSV export |
| Unit 3 | Java Networking | `BidNotificationServer.java`, `AuctionChatServer.java` — TCP Socket, UDP |
| Unit 4 | JDBC | All DAO classes — PreparedStatement, BLOB, CallableStatement, MetaData |
| Unit 5 | RMI | `AuctionService.java`, `AuctionRMIServer.java`, `AuctionRMIClient.java` |
| Unit 6 | Servlets & JSP | All Servlets, all JSP pages, Filter, Session, Cookies, FileUpload |
| Unit 7 | Mail, WebServices, REST, Security | `AuctionMailService.java`, `AuctionRestAPI.java`, `SecurityUtil.java` |
| Unit 8 | Design Patterns, Hibernate, JSF | `DesignPatterns.java` — Singleton, Factory, Observer, Decorator, Builder |

---

## 🗂️ Project Structure

```
OnlineAuctionSystem/
├── pom.xml
├── database_schema.sql
├── README.md
└── src/main/
    ├── java/com/auction/
    │   ├── model/          User, AuctionItem, Bid, Winner
    │   ├── dao/            UserDAO, AuctionItemDAO, BidDAO, WinnerDAO
    │   ├── servlet/        Login, Register, Dashboard, Bid, AuctionItem,
    │   │                   Admin, Search, BidPoll, Logout servlets
    │   ├── filter/         AuthFilter
    │   ├── network/        BidNotificationServer, AuctionChatServer,
    │   │                   AppStartupListener, AuctionExpiryChecker
    │   ├── rmi/            AuctionService, AuctionServiceImpl,
    │   │                   AuctionRMIServer, AuctionRMIClient
    │   ├── mail/           AuctionMailService
    │   ├── webservice/     AuctionRestAPI (Jersey JAX-RS)
    │   ├── io/             AuctionLogger
    │   ├── security/       SecurityUtil (BCrypt)
    │   └── patterns/       DesignPatterns (All Unit 8 patterns)
    └── webapp/
        ├── css/            style.css
        ├── js/             bid-live.js
        └── WEB-INF/
            ├── web.xml
            └── views/      login, register, dashboard, item-detail,
                            add-item, admin, search-results, error pages
```

---

## ⚙️ Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| Java JDK | 11+ | https://www.oracle.com/java/technologies/downloads/ |
| Apache Tomcat | 9.x | https://tomcat.apache.org/download-90.cgi |
| Oracle Database | XE 21c | https://www.oracle.com/database/technologies/xe-downloads.html |
| Maven | 3.8+ | https://maven.apache.org/download.cgi |
| VS Code | Latest | https://code.visualstudio.com/ |

VS Code Extensions needed:
- Extension Pack for Java
- Maven for Java
- Tomcat for Java

---

## 🚀 Step-by-Step Setup

### Step 1 — Oracle Database Setup

1. Install Oracle XE and start it
2. Open SQL Developer or SQL*Plus
3. Create a new user:
```sql
CREATE USER auction_user IDENTIFIED BY auction_pass;
GRANT CONNECT, RESOURCE, DBA TO auction_user;
```
4. Connect as `auction_user` and run:
```
File → Run Script → select database_schema.sql
```

### Step 2 — Oracle JDBC Driver

The ojdbc8.jar is NOT in Maven Central. Install it manually:

```bash
# Download ojdbc8.jar from:
# https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html

# Then install into local Maven repo:
mvn install:install-file \
  -Dfile=ojdbc8.jar \
  -DgroupId=com.oracle \
  -DartifactId=ojdbc8 \
  -Dversion=19.3 \
  -Dpackaging=jar
```

### Step 3 — Configure DB Connection

Create a file called `env.bat` in the project root (this file is in `.gitignore` — never commit it):
```bat
set DB_URL=jdbc:oracle:thin:@localhost:1521:XE
set DB_USER=auction_user
set DB_PASS=auction_pass
```
`startApp.bat` automatically loads `env.bat` at startup. The app reads credentials via `System.getenv()` — credentials are never hardcoded in source files.

### Step 4 — Configure JavaMail (Optional)

Edit `src/main/java/com/auction/mail/AuctionMailService.java`:
```java
private static final String MAIL_FROM = "your.email@gmail.com";
private static final String MAIL_USER = "your.email@gmail.com";
private static final String MAIL_PASS = "your_app_password"; // Gmail App Password
```
> **Gmail App Password:** Google Account → Security → 2-Step Verification → App Passwords

### Step 5 — Build the Project

```bash
cd OnlineAuctionSystem
mvn clean package -DskipTests
```
This creates: `target/OnlineAuctionSystem.war`

### Step 6 — Deploy to Tomcat

**Option A — VS Code:**
1. In VS Code, right-click on the project → "Run on Tomcat Server"
2. Select your Tomcat 9 installation

**Option B — Manual:**
```bash
cp target/OnlineAuctionSystem.war /path/to/tomcat/webapps/
# Start Tomcat:
/path/to/tomcat/bin/startup.sh   # Linux/Mac
/path/to/tomcat/bin/startup.bat  # Windows
```

### Step 7 — Start RMI Server (for Unit 5)

```bash
# In a separate terminal:
java -cp target/OnlineAuctionSystem.war com.auction.rmi.AuctionRMIServer
```

### Step 8 — Access the Application

| URL | Description |
|-----|-------------|
| http://localhost:8080/OnlineAuctionSystem/ | Login page |
| http://localhost:8080/OnlineAuctionSystem/RegisterServlet | Register |
| http://localhost:8080/OnlineAuctionSystem/DashboardServlet | Dashboard |
| http://localhost:8080/OnlineAuctionSystem/AdminServlet | Admin panel |
| http://localhost:8080/OnlineAuctionSystem/api/items | REST API |

### Default Credentials

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `Admin@123` |
| Bidder | `john_doe` | `Test@123` |

---

## 🔌 REST API Endpoints (Unit 7)

```
GET  /api/items              → All active auctions (JSON)
GET  /api/items/{id}         → Single item details
GET  /api/items/{id}/bids    → Bid history for an item
GET  /api/items/search?q=... → Search items
POST /api/bids               → Place a bid
GET  /api/winners            → All auction winners
GET  /api/winners/item/{id}  → Winner for a specific item
```

---

## 🔌 Socket Servers (Unit 3)

| Server | Port | Purpose |
|--------|------|---------|
| BidNotificationServer | 9090 | Real-time bid broadcasts (TCP) |
| AuctionChatServer | 9092 | Per-auction chat rooms (TCP) |
| UDP Broadcast | 9091 | "Ending soon" alerts |

Both start automatically when Tomcat deploys the app (`AppStartupListener`).

---

## 🔌 RMI Server (Unit 5)

```
RMI Registry Port: 1099
Service Name:      AuctionService
Lookup URL:        rmi://localhost/AuctionService
```

Run `AuctionRMIClient.java` as a standalone Java application for console-based bidding.

---

## 🎨 Design Patterns Used (Unit 8)

| Pattern | Class | How |
|---------|-------|-----|
| Singleton | `AuctionManager`, `BidNotificationServer` | One global instance |
| Factory | `AuctionItemFactory` | Creates Electronics/Vehicles/Art items |
| Observer | `BidEventPublisher` + `BidObserver` | Notifies on new bids |
| Decorator | `PremiumBidDecorator`, `ReserveBidDecorator` | Adds features to bids |
| Builder | `AuctionItemBuilder` | Fluent item creation |

---

## 📊 Database Schema

```
users          → user_id, username, email, password, role
auction_items  → item_id, title, category, current_price, image_data (BLOB)
bids           → bid_id, item_id, bidder_id, bid_amount
winners        → winner_id, item_id, user_id, winning_amount
watchlist      → watch_id, user_id, item_id
messages       → msg_id, item_id, sender_id, content
```

Stored Procedure: `determine_winner(item_id)` — called via `CallableStatement`

---

## ⚠️ Common Issues & Fixes

**Problem:** `ORA-12505 — SID not recognized`
**Fix:** Change URL to `jdbc:oracle:thin:@localhost:1521/XEPDB1` for Oracle 21c

**Problem:** `ClassNotFoundException: oracle.jdbc.driver.OracleDriver`
**Fix:** Run the `mvn install:install-file` command for ojdbc8.jar

**Problem:** `Port 9090 already in use`
**Fix:** Change `PORT` in `BidNotificationServer.java` to 9093 or any free port

**Problem:** `javax.mail.AuthenticationFailedException`
**Fix:** Use Gmail App Password, not your regular Gmail password

---

*Project by: [Your Name] | Roll No: [Your Roll No] | BE CSE Sem 5*


---

## 🚀 Quick Run (Windows)

```bat
:: First time only — kill any existing Java process
taskkill /F /IM java.exe

:: Run the app
startApp.bat
```

Access at: `http://localhost:8080/OnlineAuctionSystem/`