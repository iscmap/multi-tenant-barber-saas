package com.barbersaas.booking.adapters.out.event.kafka;

import com.barbersaas.shared.events.contract.BookingCreatedEvent;
import com.barbersaas.shared.events.envelope.EventEnvelope;
import com.barbersaas.shared.events.parser.EventJsonParser;
import com.barbersaas.shared.events.parser.JacksonEventJsonParser;
import com.barbersaas.shared.messaging.config.MessagingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaBookingCreatedEventPublisher {
  public static final Logger LOGGER =
      LoggerFactory.getLogger(KafkaBookingCreatedEventPublisher.class);

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final MessagingProperties messagingProperties;
  private final EventJsonParser eventJsonParser;

  public KafkaBookingCreatedEventPublisher(
      KafkaTemplate<String, String> kafkaTemplate, MessagingProperties messagingProperties) {
    this.kafkaTemplate = kafkaTemplate;
    this.messagingProperties = messagingProperties;
    this.eventJsonParser = new JacksonEventJsonParser();
  }

  public void publish(EventEnvelope<BookingCreatedEvent> eventEnvelope) {
    String payload = eventJsonParser.toJson(eventEnvelope);
    String topic = messagingProperties.getBookingCreatedKafkaTopic();
    String key = eventEnvelope.getTenantId() + ":" + eventEnvelope.getPayload().getBookingId();

    kafkaTemplate.send(topic, key, payload);

    LOGGER.info(
        "kafka_booking_created_published topic={} key={} bookingId={} tenantId={}",
        topic,
        key,
        eventEnvelope.getPayload().getBookingId(),
        eventEnvelope.getTenantId());
  }
}
