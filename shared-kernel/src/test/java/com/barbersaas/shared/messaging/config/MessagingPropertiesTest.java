package com.barbersaas.shared.messaging.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MessagingPropertiesTest {

  @Test
  void shouldSetAndGetMessagingProperties() {
    MessagingProperties properties = new MessagingProperties();
    properties.setBookingEventsTopic("booking-events");
    properties.setBookingCreatedQueue("booking-created-queue");
    properties.setAvailabilityEventsTopic("availability-events");
    properties.setAvailabilityDecidedQueue("availability-decided-queue");
    properties.setBookingEventsDlq("booking-events-dlq");
    properties.setAvailabilityEventsDlq("availability-events-dlq");
    properties.setBookingCreatedKafkaTopic("booking-created-kafka");
    properties.setAvailabilityKafkaConsumerGroup("availability-booking-created-group");
    properties.setKafkaBootstrapServers("localhost:9092");
    properties.setKafkaListenerAutoStartup(true);

    assertEquals("booking-events", properties.getBookingEventsTopic());
    assertEquals("booking-created-queue", properties.getBookingCreatedQueue());
    assertEquals("availability-events", properties.getAvailabilityEventsTopic());
    assertEquals("availability-decided-queue", properties.getAvailabilityDecidedQueue());
    assertEquals("booking-events-dlq", properties.getBookingEventsDlq());
    assertEquals("availability-events-dlq", properties.getAvailabilityEventsDlq());
    assertEquals("booking-created-kafka", properties.getBookingCreatedKafkaTopic());
    assertEquals(
        "availability-booking-created-group", properties.getAvailabilityKafkaConsumerGroup());
    assertEquals("localhost:9092", properties.getKafkaBootstrapServers());
    assertTrue(properties.isKafkaListenerAutoStartup());
  }
}
