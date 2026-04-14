package com.barbersaas.shared.events.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BookingCreatedEventTest {

  @Test
  void shouldBuildBookingCreatedEvent() {
    BookingCreatedEvent event =
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

    assertEquals("evt-1", event.getEventId());
    assertEquals("BookingCreated", event.getEventType());
    assertEquals("booking-1", event.getBookingId());
    assertEquals("PENDING", event.getStatus());
  }
}
