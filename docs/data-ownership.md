# Data Ownership

## Rule

Each service owns its own data and database.

No service reads or writes another service's database directly.

Integration between services happens through API contracts and event contracts.

---

## booking-service owns

The booking-service is the source of truth for:

- bookingId
- shopId
- barberId
- customerId
- booking date
- booking start time
- booking durationMinutes
- serviceCode
- booking status
- idempotency key result for booking requests
- booking lifecycle transitions

### booking status examples

- PENDING
- CONFIRMED
- REJECTED

---

## availability-service owns

The availability-service is the source of truth for:

- barber working schedule
- available slots
- reserved slots
- slot reservation decisions
- availability rejection reasons

### decision examples

- CONFIRMED
- REJECTED

### rejection reason examples

- BARBER_NOT_WORKING
- SLOT_ALREADY_RESERVED
- INVALID_DURATION

---

## shared-kernel owns

The shared-kernel does not own runtime business data.

It only contains shared contracts and common technical models such as:

- API DTOs if explicitly shared
- event contracts
- ownership reference models
- later shared event envelope structures

---

## Why this matters

This avoids tight coupling.

If booking-service needs an availability decision, it should receive it through an event or API response, not by querying the availability database directly.

If availability-service needs booking input, it should consume the booking event contract, not read booking tables directly.

---

## Interview explanation

This project uses service-level data ownership.

That means:

- each microservice owns its own persistence
- cross-service communication happens through contracts
- this supports loose coupling
- this supports eventual consistency
- this prevents shared-database anti-patterns