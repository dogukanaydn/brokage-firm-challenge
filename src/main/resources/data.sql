-- === Assets ===

-- Customer 1: has TRY and AAPL
INSERT INTO assets (customer_id, asset_name, size, usable_size) VALUES (1, 'TRY', 10000, 8000);
INSERT INTO assets (customer_id, asset_name, size, usable_size) VALUES (1, 'AAPL', 20, 20);

-- Customer 2: has TRY and TSLA
INSERT INTO assets (customer_id, asset_name, size, usable_size) VALUES (2, 'TRY', 5000, 5000);
INSERT INTO assets (customer_id, asset_name, size, usable_size) VALUES (2, 'TSLA', 10, 10);

-- === Orders ===

-- Customer 1: PENDING BUY order (locked 2000 TRY)
INSERT INTO orders (customer_id, asset_name, side, size, price, status, create_date)
VALUES (1, 'AAPL', 'BUY', 10, 200, 'PENDING', CURRENT_TIMESTAMP);

-- Customer 1: CANCELED SELL order (restored 5 AAPL)
INSERT INTO orders (customer_id, asset_name, side, size, price, status, create_date)
VALUES (1, 'AAPL', 'SELL', 5, 210, 'CANCELED', CURRENT_TIMESTAMP);

-- Customer 2: MATCHED SELL order (received 1000 TRY)
INSERT INTO orders (customer_id, asset_name, side, size, price, status, create_date)
VALUES (2, 'TSLA', 'SELL', 5, 200, 'MATCHED', CURRENT_TIMESTAMP);
