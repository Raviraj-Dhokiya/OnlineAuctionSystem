<div align="center">
  <h1>🏆 Online Auction System</h1>
  <p><strong>Advanced Java Technologies — Academic Project</strong></p>
</div>

<br />

Welcome to the **Online Auction System**, a comprehensive web application built to demonstrate advanced concepts in Java programming. This project follows an n-tier architecture and incorporates features such as real-time bidding, simple authentication (direct registration/login), fully functional mock payment portals, live socket servers, REST APIs, and database management.


---

## 🛠️ Technologies Used

- **Backend:** Java 11+, Java Servlets, JSP
- **Database:** Oracle Database 21c XE, JDBC
- **Networking:** TCP/UDP Sockets, Java RMI (Remote Method Invocation)
- **Web Services & APIs:** REST API (Jersey JAX-RS), Java Mail API
- **Build & Server:** Maven, Apache Tomcat 9.x
- **Frontend:** HTML5, CSS3, Vanilla JavaScript
- **Security & Patterns:** BCrypt, Singleton, Factory, Observer, Decorator, Builder

---

## 🗂️ Project Structure

```text
OnlineAuctionSystem/
├── pom.xml                   # Maven dependencies and build config
├── database_schema.sql       # Database schema creation script
├── env.bat                   # Database credentials (to be created)
├── startApp.bat              # Quick start script for Windows
├── README.md                 # Project documentation
└── src/main/
    ├── java/com/auction/
    │   ├── model/            # Data models (User, AuctionItem, Bid)
    │   ├── dao/              # Database Access Objects (JDBC logic)
    │   ├── servlet/          # Controllers (Login, Dashboard, Bid)
    │   ├── filter/           # Authentication and Security filters
    │   ├── network/          # TCP/UDP Socket Servers
    │   ├── rmi/              # Remote Method Invocation implementation
    │   ├── mail/             # Java Mail Service integration
    │   ├── webservice/       # RESTful API Endpoints
    │   ├── io/               # File I/O operations and Logs
    │   ├── security/         # Password hashing and Utils
    │   └── patterns/         # Gang of Four Design Patterns
    └── webapp/
        ├── css/              # Stylesheets
        ├── js/               # Client-side validation and live bidding
        └── WEB-INF/
            ├── web.xml       # Deployment descriptor
            └── views/        # JSP templates for rendering
```

---

## 📖 Chapter-Wise Implementation (Syllabus Mapping)

This project strictly follows the Advanced Java Technologies curriculum, featuring practical tools mapping to specific chapters:

* **Unit 1: Introduction to Application Programming**
  * Built an optimal **n-tier architecture** incorporating Client, Web Controller (Servlet), Business Logic, Data Access (DAO), and Database layers.
* **Unit 2: Java I/O**
  * **`AuctionLogger.java`:** Utilized `FileStreams` and `RandomAccessFile` to export auction bidding records sequentially to CSV files and system logs securely. 
* **Unit 3: Java Networking**
  * **`BidNotificationServer.java` & `AuctionChatServer.java`:** Implemented TCP Sockets for real-time bid broadcasting and per-auction chat rooms. Used UDP datagrams to multicast "Ending soon" alerts seamlessly.
* **Unit 4: Java Database Connectivity (JDBC)**
  * Established secure DB connectivity via Oracle JDBC driver. Utilized `PreparedStatement`, `CallableStatement`, `ResultSet`, and `MetaData` APIs in the DAO layer securely. Stored items via BLOB data types.
* **Unit 5: Java RMI (Remote Method Invocation)**
  * **`AuctionRMIServer` & `AuctionRMIClient`:** Registered services over an RMI Registry (`port 1099`) defining stubs and skeletons for remote independent interactions and console-based bidding.
* **Unit 6: Java Servlets & JSP**
  * Used `HttpServlet` API to serve pages and endpoints (e.g., `PaymentServlet` for end-to-end mock payment workflows). Utilized sessions and cookies for authentication tracking, configured `web.xml` deployment descriptors, implemented Filters, and served dynamic JSP views avoiding CGI bottlenecks.
* **Unit 7: Web Services, REST APIs, & Java Mail**
  * **`AuctionRestAPI.java`:** Exposed clean REST APIs returning real-time JSON for checking item catalogs, prices, and past winners. Used Java Mail APIs (`AuctionMailService.java`) for email notifications.
* **Unit 8: Design Patterns & Frameworks**
  * Heavily used **Creational, Structural, and Behavioral Design Patterns**:
    * *Singleton:* For DB connections and `AuctionManager`.
    * *Factory:* `AuctionItemFactory` dynamic instantiation.
    * *Observer:* Broadcasting bid updates (`BidObserver`).
    * *Decorator:* Extending bids natively (`PremiumBidDecorator`).
    * *Builder:* Streamlining object creations without complex constructors.

---

## 🚀 Run the Project: Step-by-Step Guide

### 1️⃣ Prerequisites
Make sure you have installed the following software on your machine:
- **Java JDK 11+**
- **Apache Tomcat 9.x**
- **Oracle Database XE 21c**
- **Maven** Plugin

### 2️⃣ Database Setup
1. Launch **Oracle XE** and connect via SQL Developer or SQL*Plus as SYSTEM.
2. Create the project workspace/user:
   ```sql
   CREATE USER auction_user IDENTIFIED BY auction_pass;
   GRANT CONNECT, RESOURCE, DBA TO auction_user;
   ```
3. Connect strictly as `auction_user`.
4. Locate the file `database_schema.sql` at the root folder and run it to construct tables (users, items, bids...).

### 3️⃣ Configure Environment Properties
The project loads configurations dynamically. Create a file named **`env.bat`** (at root directory) bridging your DB:
```bat
:: env.bat
set DB_URL=jdbc:oracle:thin:@localhost:1521:XE
set DB_USER=auction_user
set DB_PASS=auction_pass
```

### 4️⃣ Install Oracle JDBC manually
Because Oracle constraints its JDBC driver via direct maven downloads, do a one-time configuration via CLI:
```bat
mvn install:install-file -Dfile=ojdbc8.jar -DgroupId=com.oracle -DartifactId=ojdbc8 -Dversion=19.3 -Dpackaging=jar
```
*(Alternative: You may place the `ojdbc8.jar` file manually inside Apache Tomcat's `lib` directory.)*

### 5️⃣ Booting up Localhost (How to Run)
The application comes with the Windows utility script meant to simplify boot processes!

1. Open your terminal in the directory where `startApp.bat` and `pom.xml` reside.
2. Run the batch file natively:
   ```bat
   startApp.bat
   ```
   *(Note: This completely packages your MAVEN target, bridges `env.bat`, starts Tomcat, and deploys it automatically.)*

### 🌐 Checking Output (Localhost Port)
Once successfully compiled and booted, the Tomcat server operates exclusively on the following localhost port address in your browser:

👉 **URL:** [http://localhost:8080](http://localhost:8080)
- *Default Port check: `8080`*
- *RMI Port: `1099`*
- *Socket Notification Port: `9090`*

Explore, Register a brand-new user on localhost, publish Items, bid via chat seamlessly, and win the auctions!

<div align="center">
  <br/>
  <b>Made to demonstrate advanced Java Engineering</b>
</div>