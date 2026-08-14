CREATE INDEX IF NOT EXISTS idx_barber_availability_shop_barber_date_status
    ON availability.barber_availability (
    shop_id,
    barber_id,
    availability_date,
    status
    );

CREATE INDEX IF NOT EXISTS idx_barber_schedule_shop_barber_date
    ON availability.barber_schedule (
    shop_id,
    barber_id,
    schedule_date
    );