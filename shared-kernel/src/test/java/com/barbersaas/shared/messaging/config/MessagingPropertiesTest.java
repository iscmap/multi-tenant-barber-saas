package com.barbersaas.shared.messaging.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    assertEquals("booking-events", properties.getBookingEventsTopic());
    assertEquals("booking-created-queue", properties.getBookingCreatedQueue());
    assertEquals("availability-events", properties.getAvailabilityEventsTopic());
    assertEquals("availability-decided-queue", properties.getAvailabilityDecidedQueue());
    assertEquals("booking-events-dlq", properties.getBookingEventsDlq());
    assertEquals("availability-events-dlq", properties.getAvailabilityEventsDlq());
  }
}
