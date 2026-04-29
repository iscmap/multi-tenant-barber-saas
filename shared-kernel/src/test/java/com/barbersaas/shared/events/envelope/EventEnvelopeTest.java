package com.barbersaas.shared.events.envelope;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.barbersaas.shared.events.contract.BookingCreatedEvent;
import org.junit.jupiter.api.Test;

public class EventEnvelopeTest {

  @Test
  void shouldBuildEventEnvelope() {
    BookingCreatedEvent payload =
        BookingCreatedEvent.builder()
            .eventId("evt-1")
            .eventType("BookingCreated")
            .occurredAt("2026-04-10T10:00:00Z")
            .correlationId("corr-1")
            .bookingId("booking-1")
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date("2026-04-10")
            .startTime("10:00:00")
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .status("PENDING")
            .build();

    EventEnvelope<BookingCreatedEvent> envelope =
        EventEnvelope.<BookingCreatedEvent>builder()
            .eventId("env-1")
            .eventType("BookingCreated")
            .occurredAt("2026-04-10T10:00:00Z")
            .correlationId("corr-1")
            .source("booking-service")
            .tenantId("shop-1")
            .payload(payload)
            .build();

    assertEquals("env-1", envelope.getEventId());
    assertEquals("booking-service", envelope.getSource());
    assertEquals("shop-1", envelope.getTenantId());
    assertEquals("booking-1", envelope.getPayload().getBookingId());
  }
}
