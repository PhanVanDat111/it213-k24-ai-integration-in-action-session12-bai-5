CREATE TABLE IF NOT EXISTS post_offices (
    id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    region VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS deliveries (
    id VARCHAR(20) PRIMARY KEY,
    tracking_code VARCHAR(20) UNIQUE NOT NULL,
    post_office_id VARCHAR(10) REFERENCES post_offices(id),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    delivered_at TIMESTAMP,
    shipping_fee DECIMAL(10,2) NOT NULL
);