package com.barbersaas.availability.adapters.out.event.sns;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.barbersaas.availability.application.port.out.messaging.PublishMessagePort;
import com.barbersaas.shared.events.contract.AvailabilityDecidedEvent;
import com.barbersaas.shared.events.envelope.EventEnvelope;
import com.barbersaas.shared.messaging.config.MessagingProperties;
import org.junit.jupiter.api.Test;

class SnsAvailabilityDecidedEventPublisherTest {

  private final PublishMessagePort publishMessagePort = mock(PublishMessagePort.class);

  @Test
  void shouldSerializeAndPublishAvailabilityDecidedEventEnvelope() {
    MessagingProperties properties = new MessagingProperties();
    properties.setAvailabilityEventsTopic("availability-events");

    SnsAvailabilityDecidedEventPublisher publisher =
        new SnsAvailabilityDecidedEventPublisher(publishMessagePort, properties);

    AvailabilityDecidedEvent payload =
        AvailabilityDecidedEvent.builder()
            .eventId("evt-2")
            .eventType("AvailabilityDecided")
            .occurredAt("2026-04-10T10:00:00Z")
            .correlationId("corr-2")
            .bookingId("booking-2")
            .shopId("shop-1")
            .barberId("barber-1")
            .decision("CONFIRMED")
            .reason("SLOT_RESERVED")
            .build();

    EventEnvelope<AvailabilityDecidedEvent> envelope =
        EventEnvelope.<AvailabilityDecidedEvent>builder()
            .eventId("evt-2")
            .eventType("AvailabilityDecided")
            .occurredAt("2026-04-10T10:00:00Z")
            .correlationId("corr-2")
            .source("availability-service")
            .tenantId("shop-1")
            .payload(payload)
            .build();

    publisher.publish(envelope);

    verify(publishMessagePort)
        .publish(
            eq("availability-events"),
            eq(
"""
{"eventId":"evt-2","eventType":"AvailabilityDecided","occurredAt":"2026-04-10T10:00:00Z","correlationId":"corr-2","source":"availability-service","tenantId":"shop-1","payload":{"eventId":"evt-2","eventType":"AvailabilityDecided","occurredAt":"2026-04-10T10:00:00Z","correlationId":"corr-2","bookingId":"booking-2","shopId":"shop-1","barberId":"barber-1","decision":"CONFIRMED","reason":"SLOT_RESERVED"}}"""));
  }
}
