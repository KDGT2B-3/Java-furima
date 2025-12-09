-- ===============================
-- 依存順に DROP（開発用リセット）
-- ===============================
DROP TABLE IF EXISTS review CASCADE;
DROP TABLE IF EXISTS favorite_item CASCADE;
DROP TABLE IF EXISTS chat CASCADE;
DROP TABLE IF EXISTS app_order CASCADE;
DROP TABLE IF EXISTS item CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ===============================
-- users テーブル
-- ===============================
CREATE TABLE users (
    id SERIAL PRIMARY KEY,                  -- 主キー
    name VARCHAR(50) NOT NULL,              -- 表示名
    email VARCHAR(255) NOT NULL UNIQUE,     -- メール（一意）
    password VARCHAR(255) NOT NULL,         -- パスワード
    role VARCHAR(20) NOT NULL,              -- USER / ADMIN
    line_notify_token VARCHAR(255),         -- LINE Notify
    enabled BOOLEAN NOT NULL DEFAULT TRUE   -- 有効/無効
);

-- ===============================
-- category テーブル
-- ===============================
CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- ===============================
-- item テーブル
-- ===============================
CREATE TABLE item (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,                       -- 出品者ID
    name VARCHAR(255) NOT NULL,                 -- 商品名
    description TEXT,                           -- 説明
    price NUMERIC(10,2) NOT NULL,               -- 価格
    category_id INT,                            -- カテゴリ
    status VARCHAR(20) DEFAULT '出品中',        -- ステータス
    image_url TEXT,                             -- 画像URL
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES category(id)
);

-- ===============================
-- app_order（注文）テーブル
-- ===============================
CREATE TABLE app_order (
    id SERIAL PRIMARY KEY,
    item_id INT NOT NULL,                       -- 商品
    buyer_id INT NOT NULL,                      -- 購入者
    price NUMERIC(10,2) NOT NULL,               -- 価格 snapshot
    status VARCHAR(20) DEFAULT '購入済',        -- ステータス
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (item_id) REFERENCES item(id),
    FOREIGN KEY (buyer_id) REFERENCES users(id)
);

-- ===============================
-- chat テーブル
-- ===============================
CREATE TABLE chat (
    id SERIAL PRIMARY KEY,
    item_id INT NOT NULL,
    sender_id INT NOT NULL,
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (item_id) REFERENCES item(id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- ===============================
-- favorite_item（お気に入り）テーブル
-- ===============================
CREATE TABLE favorite_item (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    item_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, item_id),

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (item_id) REFERENCES item(id)
);

-- ===============================
-- review（評価）テーブル
-- ===============================
CREATE TABLE review (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL UNIQUE,               -- 1注文1レビュー
    reviewer_id INT NOT NULL,
    seller_id INT NOT NULL,
    item_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES app_order(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id),
    FOREIGN KEY (seller_id) REFERENCES users(id),
    FOREIGN KEY (item_id) REFERENCES item(id)
);

-- ===============================
-- インデックス（高速化）
-- ===============================
CREATE INDEX IF NOT EXISTS idx_item_user_id      ON item(user_id);
CREATE INDEX IF NOT EXISTS idx_item_category_id  ON item(category_id);

CREATE INDEX IF NOT EXISTS idx_order_item_id     ON app_order(item_id);
CREATE INDEX IF NOT EXISTS idx_order_buyer_id    ON app_order(buyer_id);

CREATE INDEX IF NOT EXISTS idx_chat_item_id      ON chat(item_id);
CREATE INDEX IF NOT EXISTS idx_chat_sender_id    ON chat(sender_id);

CREATE INDEX IF NOT EXISTS idx_fav_user_id       ON favorite_item(user_id);
CREATE INDEX IF NOT EXISTS idx_fav_item_id       ON favorite_item(item_id);

CREATE INDEX IF NOT EXISTS idx_review_order_id   ON review(order_id);
