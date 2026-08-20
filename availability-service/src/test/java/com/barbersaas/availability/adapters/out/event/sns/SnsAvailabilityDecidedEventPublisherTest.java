package com.barbersaas.availability.adapters.out.event.sns;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.barbersaas.availability.application.port.out.messaging.PublishMessagePort;
import com.barbersaas.shared.events.contract.AvailabilityDecidedEvent;
import com.barbersaas.shared.events.envelope.EventEnvelope;
import com.barbersaas.shared.messaging.config.MessagingProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class SnsAvailabilityDecidedEventPublisherTest {

  private static final String AVAILABILITY_EVENTS_TOPIC_ARN =
      "arn:aws:sns:us-east-1:000000000000:availability-events";

  @Mock private PublishMessagePort publishMessagePort;

  private MessagingProperties messagingProperties;
  private SnsAvailabilityDecidedEventPublisher publisher;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    messagingProperties = new MessagingProperties();
    messagingProperties.setAvailabilityEventsTopicArn(AVAILABILITY_EVENTS_TOPIC_ARN);

    publisher = new SnsAvailabilityDecidedEventPublisher(publishMessagePort, messagingProperties);
  }

  @Test
  void shouldSerializeAndPublishAvailabilityDecidedEventEnvelope() {

    AvailabilityDecidedEvent event =
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
            .payload(event)
            .build();

    publisher.publish(envelope);

    String expectedPayload =
        "{\"eventId\":\"evt-2\","
            + "\"eventType\":\"AvailabilityDecided\","
            + "\"occurredAt\":\"2026-04-10T10:00:00Z\","
            + "\"correlationId\":\"corr-2\","
            + "\"source\":\"availability-service\","
            + "\"tenantId\":\"shop-1\","
            + "\"payload\":{"
            + "\"eventId\":\"evt-2\","
            + "\"eventType\":\"AvailabilityDecided\","
            + "\"occurredAt\":\"2026-04-10T10:00:00Z\","
            + "\"correlationId\":\"corr-2\","
            + "\"bookingId\":\"booking-2\","
            + "\"shopId\":\"shop-1\","
            + "\"barberId\":\"barber-1\","
            + "\"decision\":\"CONFIRMED\","
            + "\"reason\":\"SLOT_RESERVED\""
            + "}}";

    verify(publishMessagePort).publish(eq(AVAILABILITY_EVENTS_TOPIC_ARN), eq(expectedPayload));
  }
}
