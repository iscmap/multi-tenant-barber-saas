package com.barbersaas.shared.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MessagingChannelsTest {

  @Test
  void shouldExposeMessagingChannelConstants() {
    assertEquals("booking-events", MessagingChannels.BOOKING_EVENTS_TOPIC);
    assertEquals("booking-created-queue", MessagingChannels.BOOKING_CREATED_QUEUE);
    assertEquals("availability-events", MessagingChannels.AVAILABILITY_EVENTS_TOPIC);
    assertEquals("availability-decided-queue", MessagingChannels.AVAILABILITY_DECIDED_QUEUE);
    assertEquals("booking-events-dlq", MessagingChannels.BOOKING_EVENTS_DLQ);
    assertEquals("availability-events-dlq", MessagingChannels.AVAILABILITY_EVENTS_DLQ);
    assertEquals("BookingCreated", MessagingChannels.BOOKING_CREATED_EVENT_TYPE);
    assertEquals("AvailabilityDecided", MessagingChannels.AVAILABILITY_DECIDED_EVENT_TYPE);
  }
}
