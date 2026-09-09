CREATE TABLE IF NOT EXISTS availability.barber_schedule (
    shop_id VARCHAR(100) NOT NULL,
    barber_id VARCHAR(100) NOT NULL,
    schedule_date DATE NOT NULL,
    work_start_time TIME NOT NULL,
    work_end_time TIME NOT NULL,
    slot_duration_minutes INTEGER NOT NULL,

    PRIMARY KEY (
                    shop_id,
                    barber_id,
                    schedule_date
                )
    );

INSERT INTO availability.barber_schedule (
    shop_id,
    barber_id,
    schedule_date,
    work_start_time,
    work_end_time,
    slot_duration_minutes
)
VALUES (
           'shop-1',
           'barber-1',
           '2026-04-10',
           '10:00:00',
           '18:00:00',
           30
       )
    ON CONFLICT DO NOTHING;