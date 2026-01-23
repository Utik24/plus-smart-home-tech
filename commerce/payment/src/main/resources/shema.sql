CREATE TABLE IF NOT EXISTS payments (
    payment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    total_payment DOUBLE PRECISION,
    delivery_total DOUBLE PRECISION,
    fee_total DOUBLE PRECISION,
    product_total DOUBLE PRECISION,
    status VARCHAR
);
