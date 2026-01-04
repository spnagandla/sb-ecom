
-- Add unique rule: category_name should not repeat (basically should be unique)
-- So this is related to DB even we are making the rules in java where it will be unique with out this we can insert the category with same name .
-- this will prevent that

ALTER TABLE categories ADD CONSTRAINT uq_category_name UNIQUE (category_name);

-- Add index to speed up queries like WHERE category_id=?

CREATE INDEX ix_product_category_id ON product(category_id);