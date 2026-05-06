package com.barbersaas.booking.application.factory;

import com.barbersaas.booking.domain.model.Booking;
import com.barbersaas.shared.events.contract.BookingCreatedEvent;
import com.barbersaas.shared.events.envelope.EventEnvelope;
import com.barbersaas.shared.logging.CorrelationIdHolder;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class BookingEventFactory {
  public EventEnvelope<BookingCreatedEvent> buildBookingCreatedEvent(Booking booking) {
    String eventId = UUID.randomUUID().toString();
    String occurredAt = OffsetDateTime.now(ZoneOffset.UTC).toString();
    String correlationId = CorrelationIdHolder.get();

    BookingCreatedEvent payload =
        BookingCreatedEvent.builder()
            .eventId(eventId)
            .eventType("BookingCreated")
            .occurredAt(occurredAt)
            .correlationId(correlationId)
            .bookingId(booking.getBookingId())
            .shopId(booking.getShopId())
            .barberId(booking.getBarberId())
            .customerId(booking.getCustomerId())
            .date(booking.getDate().toString())
            .startTime(booking.getStartTime().toString())
            .durationMinutes(booking.getDurationMinutes())
            .serviceCode(booking.getServiceCode())
            .status(booking.getStatus().name())
            .build();

    return EventEnvelope.<BookingCreatedEvent>builder()
        .eventId(eventId)
        .eventType("BookingCreated")
        .occurredAt(occurredAt)
        .correlationId(correlationId)
        .source("booking-service")
        .tenantId(booking.getShopId())
        .payload(payload)
        .build();
  }
}
