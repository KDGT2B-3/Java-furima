-- 1. 既存のテーブルをすべて削除（リセット）
DROP TABLE IF EXISTS review CASCADE;
DROP TABLE IF EXISTS favorite_item CASCADE;
DROP TABLE IF EXISTS chat CASCADE;
DROP TABLE IF EXISTS app_order CASCADE;
DROP TABLE IF EXISTS item CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS user_complaint CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 2. usersテーブルを作成（ban_reason を確実に含める）
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    line_notify_token VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    banned BOOLEAN NOT NULL DEFAULT FALSE,     
    ban_reason TEXT,                            -- ← これが必要！
    banned_at TIMESTAMP,                        
    banned_by_admin_id INT
);

-- 3. カテゴリテーブル作成
CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 4. 商品テーブル作成
CREATE TABLE item (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL,
    original_price NUMERIC(10,2) NOT NULL,
    category_id INT,
    status VARCHAR(20) DEFAULT '出品中',
    image_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES category(id)
);

-- 5. 初期データ投入（{noop} 形式）
INSERT INTO users (name, email, password, role, enabled, banned) VALUES
('出品者 A', 'sellerA@example.com', '{noop}password',  'USER',  TRUE, FALSE),
('購入者 B', 'xyz@example.com',      '{noop}password',  'USER',  TRUE, FALSE),
('運営者 C', 'adminC@example.com',   '{noop}adminpass', 'ADMIN', TRUE, FALSE);

INSERT INTO category (name) VALUES
('本'), ('家電'), ('ファッション'), ('おもちゃ'), ('文房具');

INSERT INTO item (user_id, name, description, price, category_id, status) VALUES
((SELECT id FROM users WHERE email='sellerA@example.com'), 'Java入門', '初心者向け', 1500, 1000, (SELECT id FROM category WHERE name='本'), '出品中');