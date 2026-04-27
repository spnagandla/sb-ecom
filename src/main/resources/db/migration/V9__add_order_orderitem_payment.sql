-- 1. CREATE payments FIRST (no dependencies)
CREATE TABLE payments (
                          payment_id BIGSERIAL PRIMARY KEY,
                          payment_method VARCHAR(255) NOT NULL,
                          pg_payment_id VARCHAR(255),
                          pg_status VARCHAR(100),
                          pg_response_message VARCHAR(500),
                          pg_name VARCHAR(255)
);

-- 2. CREATE orders SECOND (depends on payments & address)
CREATE TABLE orders (
                        order_id BIGSERIAL PRIMARY KEY,
                        email VARCHAR(255) NOT NULL,
                        order_date DATE,
                        payment_id BIGINT UNIQUE,
                        total_amount NUMERIC(10,2),
                        order_status VARCHAR(100),
                        address_id BIGINT,

                        CONSTRAINT fk_orders_payment
                            FOREIGN KEY (payment_id) REFERENCES payments(payment_id),

                        CONSTRAINT fk_orders_address
                            FOREIGN KEY (address_id) REFERENCES addresses(address_id)
);

-- 3. CREATE order_items LAST (depends on orders & product)
CREATE TABLE order_items (
                             order_item_id BIGSERIAL PRIMARY KEY,
                             product_id BIGINT NOT NULL,
                             order_id BIGINT NOT NULL,
                             quantity INTEGER NOT NULL,
                             discount NUMERIC(10,2),
                             ordered_product_price NUMERIC(10,2),

                             CONSTRAINT fk_order_items_product
                                 FOREIGN KEY (product_id) REFERENCES product(product_id),

                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id) REFERENCES orders(order_id)
);