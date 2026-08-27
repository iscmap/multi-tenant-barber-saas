package com.barbersaas.availability.observability.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class AvailabilityMetricsTest {

  @Test
  void shouldIncrementAvailabilityLookupCounter() {
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    AvailabilityMetrics metrics = new AvailabilityMetrics(meterRegistry);

    metrics.availabilityLookup();

    double count = meterRegistry.get("barbersaas.availability.lookup").counter().count();

    assertEquals(1.0, count);
  }

  @Test
  void shouldIncrementSuccessfulValidationCounter() {
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    AvailabilityMetrics metrics = new AvailabilityMetrics(meterRegistry);

    metrics.slotValidationSucceeded();

    double count =
        meterRegistry
            .get("barbersaas.availability.validation")
            .tag("outcome", "success")
            .counter()
            .count();

    assertEquals(1.0, count);
  }

  @Test
  void shouldIncrementRejectedValidationCounter() {
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    AvailabilityMetrics metrics = new AvailabilityMetrics(meterRegistry);

    metrics.slotValidationRejected();

    double count =
        meterRegistry
            .get("barbersaas.availability.validation")
            .tag("outcome", "rejected")
            .counter()
            .count();

    assertEquals(1.0, count);
  }
}
