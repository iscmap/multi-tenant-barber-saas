package com.barbersaas.availability.application.factory;

import com.barbersaas.shared.events.contract.AvailabilityDecidedEvent;
import com.barbersaas.shared.events.contract.BookingCreatedEvent;
import com.barbersaas.shared.events.envelope.EventEnvelope;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityEventFactory {

  public EventEnvelope<AvailabilityDecidedEvent> buildConfirmedEvent(
      BookingCreatedEvent bookingCreatedEvent) {
    return buildEventEnvelope(bookingCreatedEvent, "CONFIRMED", "SLOT_RESERVED");
  }

  public EventEnvelope<AvailabilityDecidedEvent> buildRejectedEvent(
      BookingCreatedEvent bookingCreatedEvent, String reason) {
    return buildEventEnvelope(bookingCreatedEvent, "REJECTED", reason);
  }

  private EventEnvelope<AvailabilityDecidedEvent> buildEventEnvelope(
      BookingCreatedEvent bookingCreatedEvent, String decision, String reason) {
    String eventId = UUID.randomUUID().toString();
    String occurredAt = OffsetDateTime.now(ZoneOffset.UTC).toString();

    AvailabilityDecidedEvent payload =
        AvailabilityDecidedEvent.builder()
            .eventId(eventId)
            .eventType("AvailabilityDecided")
            .occurredAt(occurredAt)
            .correlationId(bookingCreatedEvent.getCorrelationId())
            .bookingId(bookingCreatedEvent.getBookingId())
            .shopId(bookingCreatedEvent.getShopId())
            .barberId(bookingCreatedEvent.getBarberId())
            .decision(decision)
            .reason(reason)
            .build();

    return EventEnvelope.<AvailabilityDecidedEvent>builder()
        .eventId(eventId)
        .eventType("AvailabilityDecided")
        .occurredAt(occurredAt)
        .correlationId(bookingCreatedEvent.getCorrelationId())
        .source("availability-service")
        .tenantId(bookingCreatedEvent.getShopId())
        .payload(payload)
        .build();
  }
}
