package com.barbersaas.booking.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BookingMetrics {

  private final Counter bookingsCreated;
  private final Counter idempotencyReplays;

  public BookingMetrics(MeterRegistry meterRegistry) {
    this.bookingsCreated =
        Counter.builder("barbersaas.booking.created")
            .description("Number of new bookings created")
            .register(meterRegistry);

    this.idempotencyReplays =
        Counter.builder("barbersaas.booking.idempotency.replay")
            .description("Number of booking requests resolved through idempotency")
            .register(meterRegistry);
  }

  public void bookingCreated() {
    bookingsCreated.increment();
  }

  public void idempotencyReplay() {
    idempotencyReplays.increment();
  }
}
