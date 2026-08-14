\echo '======================================'
\echo 'Barber SaaS persistence validation'
\echo '======================================'

\echo ''
\echo '1. Checking schemas...'

SELECT schema_name
FROM information_schema.schemata
WHERE schema_name IN ('booking', 'availability')
ORDER BY schema_name;

\echo ''
\echo '2. Checking booking table...'

SELECT table_schema, table_name
FROM information_schema.tables
WHERE table_schema = 'booking'
  AND table_name = 'bookings';

\echo ''
\echo '3. Checking availability tables...'

SELECT table_schema, table_name
FROM information_schema.tables
WHERE table_schema = 'availability'
  AND table_name IN (
                     'barber_availability',
                     'barber_schedule'
    )
ORDER BY table_name;

\echo ''
\echo '4. Checking booking indexes...'

SELECT indexname
FROM pg_indexes
WHERE schemaname = 'booking'
  AND tablename = 'bookings'
ORDER BY indexname;

\echo ''
\echo '5. Checking availability indexes...'

SELECT tablename, indexname
FROM pg_indexes
WHERE schemaname = 'availability'
  AND tablename IN (
                    'barber_availability',
                    'barber_schedule'
    )
ORDER BY tablename, indexname;

\echo ''
\echo '6. Booking status counts...'

SELECT status, COUNT(*)
FROM booking.bookings
GROUP BY status
ORDER BY status;

\echo ''
\echo '7. Availability status counts...'

SELECT status, COUNT(*)
FROM availability.barber_availability
GROUP BY status
ORDER BY status;

\echo ''
\echo '8. Barber schedules...'

SELECT
    shop_id,
    barber_id,
    schedule_date,
    work_start_time,
    work_end_time,
    slot_duration_minutes
FROM availability.barber_schedule
ORDER BY schedule_date, shop_id, barber_id;

\echo ''
\echo '======================================'
\echo 'Validation completed'
\echo '======================================'