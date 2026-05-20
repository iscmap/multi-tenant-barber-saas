package com.barbersaas.booking.adapters.out.event.sns;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.barbersaas.booking.application.port.out.messaging.PublishMessagePort;
import com.barbersaas.shared.events.contract.BookingCreatedEvent;
import com.barbersaas.shared.events.envelope.EventEnvelope;
import com.barbersaas.shared.messaging.config.MessagingProperties;
import org.junit.jupiter.api.Test;

class SnsBookingCreatedEventPublisherTest {

  private final PublishMessagePort publishMessagePort = mock(PublishMessagePort.class);

  @Test
  void shouldSerializeAndPublishBookingCreatedEventEnvelope() {
    MessagingProperties properties = new MessagingProperties();
    properties.setBookingEventsTopic("booking-events");

    SnsBookingCreatedEventPublisher publisher =
        new SnsBookingCreatedEventPublisher(publishMessagePort, properties);

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
            .eventId("evt-1")
            .eventType("BookingCreated")
            .occurredAt("2026-04-10T10:00:00Z")
            .correlationId("corr-1")
            .source("booking-service")
            .tenantId("shop-1")
            .payload(payload)
            .build();

    publisher.publish(envelope);

    verify(publishMessagePort)
        .publish(
            eq("booking-events"),
            eq(
"""
{"eventId":"evt-1","eventType":"BookingCreated","occurredAt":"2026-04-10T10:00:00Z","correlationId":"corr-1","source":"booking-service","tenantId":"shop-1","payload":{"eventId":"evt-1","eventType":"BookingCreated","occurredAt":"2026-04-10T10:00:00Z","correlationId":"corr-1","bookingId":"booking-1","shopId":"shop-1","barberId":"barber-1","customerId":"customer-1","date":"2026-04-10","startTime":"10:00:00","durationMinutes":30,"serviceCode":"HAIRCUT","status":"PENDING"}}"""));
  }
}
