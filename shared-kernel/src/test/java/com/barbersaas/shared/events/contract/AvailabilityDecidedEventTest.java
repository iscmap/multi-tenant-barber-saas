package com.barbersaas.shared.events.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AvailabilityDecidedEventTest {

  @Test
  void shouldBuildAvailabilityDecidedEvent() {
    AvailabilityDecidedEvent event =
        AvailabilityDecidedEvent.builder()
            .eventId("evt-2")
            .eventType("AvailabilityDecided")
            .occurredAt("2026-04-10T10:00:05Z")
            .correlationId("corr-1")
            .bookingId("booking-1")
            .shopId("shop-1")
            .barberId("barber-1")
            .decision("CONFIRMED")
            .reason("SLOT_RESERVED")
            .build();

    assertEquals("evt-2", event.getEventId());
    assertEquals("AvailabilityDecided", event.getEventType());
    assertEquals("booking-1", event.getBookingId());
    assertEquals("CONFIRMED", event.getDecision());
  }
}
