CREATE SCHEMA IF NOT EXISTS booking;

CREATE TABLE IF NOT EXISTS booking.bookings (
                                                booking_id VARCHAR(100) PRIMARY KEY,
    shop_id VARCHAR(100) NOT NULL,
    barber_id VARCHAR(100) NOT NULL,
    customer_id VARCHAR(100) NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    duration_minutes INTEGER NOT NULL,
    service_code VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL
    );