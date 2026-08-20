CREATE SCHEMA IF NOT EXISTS availability;

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