package com.barbersaas.availability.application.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.barbersaas.shared.events.contract.AvailabilityDecidedEvent;
import com.barbersaas.shared.events.contract.BookingCreatedEvent;
import com.barbersaas.shared.events.envelope.EventEnvelope;
import org.junit.jupiter.api.Test;

class AvailabilityEventFactoryTest {

  private final AvailabilityEventFactory factory = new AvailabilityEventFactory();

  @Test
  void shouldBuildConfirmedEventEnvelope() {
    BookingCreatedEvent bookingCreatedEvent =
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
            .startTime("10:00")
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .status("PENDING")
            .build();

    EventEnvelope<AvailabilityDecidedEvent> envelope =
        factory.buildConfirmedEvent(bookingCreatedEvent);

    assertNotNull(envelope.getEventId());
    assertEquals("AvailabilityDecided", envelope.getEventType());
    assertEquals("availability-service", envelope.getSource());
    assertEquals("shop-1", envelope.getTenantId());
    assertEquals("CONFIRMED", envelope.getPayload().getDecision());
    assertEquals("SLOT_RESERVED", envelope.getPayload().getReason());
  }

  @Test
  void shouldBuildRejectedEventEnvelope() {
    BookingCreatedEvent bookingCreatedEvent =
        BookingCreatedEvent.builder()
            .eventId("evt-2")
            .eventType("BookingCreated")
            .occurredAt("2026-04-10T10:00:00Z")
            .correlationId("corr-2")
            .bookingId("booking-2")
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date("2026-04-10")
            .startTime("10:00")
            .durationMinutes(45)
            .serviceCode("HAIRCUT")
            .status("PENDING")
            .build();

    EventEnvelope<AvailabilityDecidedEvent> envelope =
        factory.buildRejectedEvent(
            bookingCreatedEvent, "Requested duration does not match barber slot duration");

    assertNotNull(envelope.getEventId());
    assertEquals("AvailabilityDecided", envelope.getEventType());
    assertEquals("REJECTED", envelope.getPayload().getDecision());
    assertEquals(
        "Requested duration does not match barber slot duration",
        envelope.getPayload().getReason());
  }
}
