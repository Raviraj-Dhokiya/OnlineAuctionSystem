    -- ============================================================
-- Online Auction Management System - Oracle Database Schema
-- Run this script in Oracle SQL Developer or SQL*Plus
-- ============================================================

-- 1. USERS TABLE
CREATE TABLE users (
    user_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username    VARCHAR2(50)  NOT NULL UNIQUE,
    email       VARCHAR2(100) NOT NULL UNIQUE,
    password    VARCHAR2(255) NOT NULL,   -- BCrypt hashed
    full_name   VARCHAR2(100) NOT NULL,
    phone       VARCHAR2(15),
    role        VARCHAR2(10)  DEFAULT 'BIDDER' CHECK (role IN ('BIDDER','ADMIN')),
    is_active   NUMBER(1)     DEFAULT 1,
    created_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- 2. AUCTION ITEMS TABLE
CREATE TABLE auction_items (
    item_id         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title           VARCHAR2(200) NOT NULL,
    description     CLOB,
    category        VARCHAR2(50)  NOT NULL,
    starting_price  NUMBER(12,2)  NOT NULL,
    current_price   NUMBER(12,2)  NOT NULL,
    reserve_price   NUMBER(12,2),
    image_data      BLOB,
    image_name      VARCHAR2(255),
    seller_id       NUMBER NOT NULL,
    status          VARCHAR2(20) DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE','CLOSED','CANCELLED','PENDING','NO_BIDS','RESERVE_NOT_MET')),
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_seller FOREIGN KEY (seller_id) REFERENCES users(user_id)
);

-- 3. BIDS TABLE
CREATE TABLE bids (
    bid_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_id     NUMBER       NOT NULL,
    bidder_id   NUMBER       NOT NULL,
    bid_amount  NUMBER(12,2) NOT NULL,
    bid_time    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_winning  NUMBER(1)    DEFAULT 0,
    CONSTRAINT fk_bid_item   FOREIGN KEY (item_id)   REFERENCES auction_items(item_id),
    CONSTRAINT fk_bid_bidder FOREIGN KEY (bidder_id) REFERENCES users(user_id)
);

-- 4. WINNERS TABLE
CREATE TABLE winners (
    winner_id       NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_id         NUMBER       NOT NULL UNIQUE,
    user_id         NUMBER       NOT NULL,
    winning_amount  NUMBER(12,2) NOT NULL,
    payment_status  VARCHAR2(15) DEFAULT 'PENDING'
                    CHECK (payment_status IN ('PENDING','PAID','FAILED')),
    awarded_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_win_item FOREIGN KEY (item_id) REFERENCES auction_items(item_id),
    CONSTRAINT fk_win_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 5. WATCHLIST TABLE
CREATE TABLE watchlist (
    watch_id    NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     NUMBER NOT NULL,
    item_id     NUMBER NOT NULL,
    added_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_watch_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_watch_item FOREIGN KEY (item_id) REFERENCES auction_items(item_id),
    CONSTRAINT uq_watchlist  UNIQUE(user_id, item_id)
);

-- 6. MESSAGES TABLE (for Auction Chat - Unit 3)
CREATE TABLE messages (
    msg_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_id     NUMBER        NOT NULL,
    sender_id   NUMBER        NOT NULL,
    content     VARCHAR2(500) NOT NULL,
    sent_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_msg_item   FOREIGN KEY (item_id)   REFERENCES auction_items(item_id),
    CONSTRAINT fk_msg_sender FOREIGN KEY (sender_id) REFERENCES users(user_id)
);

-- ============================================================
-- INDEXES for performance
-- ============================================================
CREATE INDEX idx_bids_item    ON bids(item_id);
CREATE INDEX idx_bids_bidder  ON bids(bidder_id);
CREATE INDEX idx_items_status ON auction_items(status);
CREATE INDEX idx_items_end    ON auction_items(end_time);
CREATE INDEX idx_items_seller ON auction_items(seller_id);

-- Function-based indexes for case-insensitive search (searchItems() uses UPPER())
-- Without these, UPPER(title) LIKE ... cannot use idx_items_status and causes full table scan
CREATE INDEX idx_items_title_upper ON auction_items(UPPER(title));
CREATE INDEX idx_items_cat_upper   ON auction_items(UPPER(category));

-- ============================================================
-- STORED PROCEDURE: Determine Auction Winner (Unit 4)
-- ============================================================
CREATE OR REPLACE PROCEDURE determine_winner(p_item_id IN NUMBER) AS
    v_bidder_id    NUMBER;
    v_bid_amount   NUMBER(12,2);
    v_reserve      NUMBER(12,2);
BEGIN
    -- Get the highest bid for the item
    SELECT bidder_id, bid_amount
    INTO v_bidder_id, v_bid_amount
    FROM (
        SELECT bidder_id, bid_amount
        FROM bids
        WHERE item_id = p_item_id
        ORDER BY bid_amount DESC
    )
    WHERE ROWNUM = 1;

    -- Check reserve price
    SELECT reserve_price INTO v_reserve
    FROM auction_items WHERE item_id = p_item_id;

    IF v_reserve IS NULL OR v_bid_amount >= v_reserve THEN
        -- Insert or update winner
        MERGE INTO winners w
        USING (SELECT p_item_id AS item_id FROM dual) s
        ON (w.item_id = s.item_id)
        WHEN MATCHED THEN
            UPDATE SET w.user_id = v_bidder_id, w.winning_amount = v_bid_amount
        WHEN NOT MATCHED THEN
            INSERT (item_id, user_id, winning_amount)
            VALUES (p_item_id, v_bidder_id, v_bid_amount);

        -- Mark winning bid
        UPDATE bids SET is_winning = 1
        WHERE item_id = p_item_id AND bidder_id = v_bidder_id AND bid_amount = v_bid_amount;

        -- Close the auction item
        UPDATE auction_items SET status = 'CLOSED' WHERE item_id = p_item_id;

        COMMIT;
    ELSE
        -- Reserve not met — use distinct status (not same as CANCELLED)
        -- CANCELLED = seller withdrew; RESERVE_NOT_MET = auction ended below reserve
        UPDATE auction_items SET status = 'RESERVE_NOT_MET' WHERE item_id = p_item_id;
        COMMIT;
    END IF;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        -- No bids at all — distinct from CANCELLED
        UPDATE auction_items SET status = 'NO_BIDS' WHERE item_id = p_item_id;
        COMMIT;
END determine_winner;
/

-- ============================================================
-- SAMPLE DATA
-- ============================================================

-- Admin user (password: Admin@123)
INSERT INTO users (username, email, password, full_name, role)
VALUES ('admin', 'admin@auction.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh8.',
        'System Admin', 'ADMIN');

-- Sample bidder (password: Test@123)
INSERT INTO users (username, email, password, full_name, role)
VALUES ('john_doe', 'john@example.com',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
        'John Doe', 'BIDDER');

-- Sample auction item
INSERT INTO auction_items (title, description, category, starting_price, current_price, seller_id, start_time, end_time)
VALUES ('Vintage Guitar 1965', 'Rare vintage Fender Stratocaster in excellent condition',
        'Music', 500.00, 500.00, 1,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '7' DAY);

COMMIT;

PROMPT Schema created successfully!
