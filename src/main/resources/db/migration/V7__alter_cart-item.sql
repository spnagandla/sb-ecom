ALTER TABLE cart_item
ALTER COLUMN discount TYPE NUMERIC(10,2)
USING discount::NUMERIC;

ALTER TABLE cart_item
ALTER COLUMN product_price TYPE NUMERIC(10,2)
USING product_price::NUMERIC;

ALTER TABLE cart
ALTER COLUMN total_price TYPE NUMERIC(10,2)
USING total_price::NUMERIC;

ALTER TABLE cart
    ALTER COLUMN total_price SET DEFAULT 0.00;

-- Using will take the already recorded double values into the numeric