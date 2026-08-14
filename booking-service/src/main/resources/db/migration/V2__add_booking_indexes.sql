CREATE INDEX IF NOT EXISTS idx_bookings_status_created_at
    ON booking.bookings (status, created_at);

CREATE INDEX IF NOT EXISTS idx_bookings_shop_barber_date
    ON booking.bookings (shop_id, barber_id, booking_date);