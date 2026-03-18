-- ============================================================
-- AUTO BID TABLE - Oracle SQL
-- Ise Oracle SQL Developer mein run karein
-- ============================================================

-- AUTO_BIDS table create karo
CREATE TABLE auto_bids (
    auto_bid_id  NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_id      NUMBER        NOT NULL,
    user_id      NUMBER        NOT NULL,
    max_target   NUMBER(12,2)  NOT NULL,
    is_active    NUMBER(1)     DEFAULT 1,
    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ab_item   FOREIGN KEY (item_id) REFERENCES auction_items(item_id),
    CONSTRAINT fk_ab_user   FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT uq_auto_bid  UNIQUE(item_id, user_id)
);

-- Index for performance
CREATE INDEX idx_auto_bids_item   ON auto_bids(item_id);
CREATE INDEX idx_auto_bids_user   ON auto_bids(user_id);
CREATE INDEX idx_auto_bids_active ON auto_bids(is_active);

COMMIT;
PROMPT Auto bids table created successfully!
