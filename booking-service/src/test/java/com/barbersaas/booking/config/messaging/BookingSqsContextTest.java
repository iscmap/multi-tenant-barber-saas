package com.barbersaas.booking.config.messaging;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.barbersaas.booking.adapters.in.messaging.sqs.AvailabilityDecidedSqsListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(
        properties = {
                "spring.cloud.aws.region.static=us-east-1",
                "spring.cloud.aws.credentials.access-key=test",
                "spring.cloud.aws.credentials.secret-key=test",
                "spring.cloud.aws.endpoint=${AWS_ENDPOINT:http://localhost:4566}",
                "barbersaas.messaging.booking-created-queue=booking-created-queue",
                "barbersaas.messaging.booking-events-topic=booking-events",
                "barbersaas.messaging.availability-events-topic=availability-events",
                "barbersaas.messaging.availability-decided-queue=availability-decided-queue",
                "barbersaas.messaging.booking-events-dlq=booking-events-dlq",
                "barbersaas.messaging.availability-events-dlq=availability-events-dlq"
        })
class BookingSqsContextTest {

  @Autowired private AvailabilityDecidedSqsListener availabilityDecidedSqsListener;

  @Test
  void shouldLoadSqsListenerBean() {
    assertNotNull(availabilityDecidedSqsListener);
  }
}