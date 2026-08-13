CREATE TABLE IF NOT EXISTS availability.barber_availability (
                                                                shop_id VARCHAR(100) NOT NULL,
    barber_id VARCHAR(100) NOT NULL,
    availability_date DATE NOT NULL,
    start_time TIME NOT NULL,
    duration_minutes INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL,

    PRIMARY KEY (
                    shop_id,
                    barber_id,
                    availability_date,
                    start_time
                )
    );

INSERT INTO availability.barber_availability (
    shop_id,
    barber_id,
    availability_date,
    start_time,
    duration_minutes,
    status
)
VALUES (
           'shop-1',
           'barber-1',
           '2026-04-10',
           '10:00:00',
           30,
           'AVAILABLE'
       )
    ON CONFLICT DO NOTHING;