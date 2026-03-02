-- ============================================================
-- 쇼핑몰 DB 스키마
-- DB: test_db
-- ============================================================

-- 회원
CREATE TABLE IF NOT EXISTS member (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    name          VARCHAR(50)  NOT NULL,
    point_balance INT          NOT NULL DEFAULT 0,
    created_at    DATETIME     NOT NULL DEFAULT NOW()
);

-- 이메일 인증 코드 (OTP)
CREATE TABLE IF NOT EXISTS email_auth (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(100) NOT NULL,
    auth_code  VARCHAR(6)   NOT NULL,
    expired_at DATETIME     NOT NULL,
    verified   TINYINT(1)   NOT NULL DEFAULT 0
);

-- 상품
CREATE TABLE IF NOT EXISTS product (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    price       INT          NOT NULL,
    stock       INT          NOT NULL DEFAULT 0,
    description TEXT,
    created_at  DATETIME     NOT NULL DEFAULT NOW()
);

-- 주문 헤더
CREATE TABLE IF NOT EXISTS orders (
    id             BIGINT      AUTO_INCREMENT PRIMARY KEY,
    member_id      BIGINT      NOT NULL,
    total_amount   INT         NOT NULL,
    point_used     INT         NOT NULL DEFAULT 0,
    payment_amount INT         NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'ORDERED',
    ordered_at     DATETIME    NOT NULL DEFAULT NOW(),
    FOREIGN KEY (member_id) REFERENCES member(id)
);

-- 주문 상품
CREATE TABLE IF NOT EXISTS order_item (
    id         BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity   INT    NOT NULL,
    price      INT    NOT NULL,
    FOREIGN KEY (order_id)   REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
);

-- 포인트 내역
CREATE TABLE IF NOT EXISTS point_history (
    id         BIGINT      AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT      NOT NULL,
    order_id   BIGINT,
    amount     INT         NOT NULL,
    type       VARCHAR(10) NOT NULL,
    created_at DATETIME    NOT NULL DEFAULT NOW(),
    FOREIGN KEY (member_id) REFERENCES member(id),
    FOREIGN KEY (order_id)  REFERENCES orders(id)
);

-- 샘플 상품 데이터
INSERT INTO product (name, price, stock, description) VALUES
('스프링 부트 강의 교재', 25000, 100, '스프링 부트 3.x 완벽 가이드'),
('마우스 패드 XL', 15000, 50,  '대형 마우스 패드 (90x40cm)'),
('USB-C 허브 7포트',  45000, 30,  'USB-C to 7-in-1 멀티허브');
