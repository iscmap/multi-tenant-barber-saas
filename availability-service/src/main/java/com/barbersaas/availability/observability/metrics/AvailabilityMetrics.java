package com.barbersaas.availability.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityMetrics {

  private final Counter availabilityLookups;
  private final Counter slotValidationSuccess;
  private final Counter slotValidationRejected;

  public AvailabilityMetrics(MeterRegistry meterRegistry) {
    this.availabilityLookups =
        Counter.builder("barbersaas.availability.lookup")
            .description("Number of availability lookups")
            .register(meterRegistry);

    this.slotValidationSuccess =
        Counter.builder("barbersaas.availability.validation")
            .description("Number of slot validations")
            .tag("outcome", "success")
            .register(meterRegistry);

    this.slotValidationRejected =
        Counter.builder("barbersaas.availability.validation")
            .description("Number of slot validations")
            .tag("outcome", "rejected")
            .register(meterRegistry);
  }

  public void availabilityLookup() {
    availabilityLookups.increment();
  }

  public void slotValidationSucceeded() {
    slotValidationSuccess.increment();
  }

  public void slotValidationRejected() {
    slotValidationRejected.increment();
  }
}
