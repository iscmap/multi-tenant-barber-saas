package com.barbersaas.booking.config.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.barbersaas.shared.messaging.config.MessagingProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class BookingMessagingConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withUserConfiguration(BookingMessagingConfiguration.class)
          .withPropertyValues(
              "barbersaas.messaging.booking-events-topic=booking-events",
              "barbersaas.messaging.booking-created-queue=booking-created-queue",
              "barbersaas.messaging.availability-events-topic=availability-events",
              "barbersaas.messaging.availability-decided-queue=availability-decided-queue",
              "barbersaas.messaging.booking-events-dlq=booking-events-dlq",
              "barbersaas.messaging.availability-events-dlq=availability-events-dlq");

  @Test
  void shouldLoadMessagingPropertiesBean() {
    contextRunner.run(
        context -> {
          MessagingProperties properties = context.getBean(MessagingProperties.class);

          assertNotNull(properties);
          assertEquals("booking-events", properties.getBookingEventsTopic());
          assertEquals("availability-decided-queue", properties.getAvailabilityDecidedQueue());
        });
  }
}
