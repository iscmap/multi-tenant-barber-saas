package com.barbersaas.booking.adapters.in.messaging.sqs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.barbersaas.booking.application.classifier.MessageFailureClassifier;
import com.barbersaas.booking.application.port.in.event.ConsumeAvailabilityDecidedUseCase;
import com.barbersaas.booking.domain.exception.messaging.RetryableMessageException;
import org.junit.jupiter.api.Test;

class AvailabilityDecidedSqsListenerTest {

  private final ConsumeAvailabilityDecidedUseCase consumeAvailabilityDecidedUseCase =
      mock(ConsumeAvailabilityDecidedUseCase.class);
  private final MessageFailureClassifier messageFailureClassifier = new MessageFailureClassifier();

  private final AvailabilityDecidedSqsListener listener =
      new AvailabilityDecidedSqsListener(
          consumeAvailabilityDecidedUseCase, messageFailureClassifier);

  @Test
  void shouldDelegatePayloadToUseCase() {
    String payload =
        """
                {
                  "eventId": "evt-1",
                  "eventType": "AvailabilityDecided",
                  "occurredAt": "2026-04-10T10:00:00Z",
                  "correlationId": "corr-1",
                  "source": "availability-service",
                  "tenantId": "shop-1",
                  "payload": {
                    "eventId": "evt-1",
                    "eventType": "AvailabilityDecided",
                    "occurredAt": "2026-04-10T10:00:00Z",
                    "correlationId": "corr-1",
                    "bookingId": "booking-1",
                    "shopId": "shop-1",
                    "barberId": "barber-1",
                    "decision": "CONFIRMED",
                    "reason": "SLOT_RESERVED"
                  }
                }
                """;

    listener.listen(payload);

    verify(consumeAvailabilityDecidedUseCase).consume(payload);
  }

  @Test
  void shouldSwallowNonRetryableFailure() {
    String payload = "{\"payload\":\"value\"}";

    doThrow(new IllegalArgumentException("Booking not found"))
        .when(consumeAvailabilityDecidedUseCase)
        .consume(payload);

    assertDoesNotThrow(() -> listener.listen(payload));
  }

  @Test
  void shouldRethrowRetryableFailure() {
    String payload = "{\"payload\":\"value\"}";

    doThrow(new RuntimeException("Temporary failure"))
        .when(consumeAvailabilityDecidedUseCase)
        .consume(payload);

    assertThrows(RetryableMessageException.class, () -> listener.listen(payload));
  }
}
