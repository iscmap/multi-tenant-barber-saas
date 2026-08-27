package com.barbersaas.booking.observability.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class BookingMetricsTest {

  @Test
  void shouldIncrementBookingCreatedCounter() {
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    BookingMetrics metrics = new BookingMetrics(meterRegistry);

    metrics.bookingCreated();

    double count = meterRegistry.get("barbersaas.booking.created").counter().count();

    assertEquals(1.0, count);
  }

  @Test
  void shouldIncrementIdempotencyReplayCounter() {
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    BookingMetrics metrics = new BookingMetrics(meterRegistry);

    metrics.idempotencyReplay();

    double count = meterRegistry.get("barbersaas.booking.idempotency.replay").counter().count();

    assertEquals(1.0, count);
  }
}
