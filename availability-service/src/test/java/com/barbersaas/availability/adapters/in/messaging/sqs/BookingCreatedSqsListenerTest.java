package com.barbersaas.availability.adapters.in.messaging.sqs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.barbersaas.availability.application.classifier.MessageFailureClassifier;
import com.barbersaas.availability.application.port.in.event.ConsumeBookingCreatedUseCase;
import com.barbersaas.availability.domain.exception.SlotValidationException;
import com.barbersaas.availability.domain.exception.messaging.RetryableMessageException;
import org.junit.jupiter.api.Test;

class BookingCreatedSqsListenerTest {

  private final ConsumeBookingCreatedUseCase consumeBookingCreatedUseCase =
      mock(ConsumeBookingCreatedUseCase.class);
  private final MessageFailureClassifier messageFailureClassifier = new MessageFailureClassifier();

  private final BookingCreatedSqsListener listener =
      new BookingCreatedSqsListener(consumeBookingCreatedUseCase, messageFailureClassifier);

  @Test
  void shouldDelegatePayloadToUseCase() {
    String payload =
        """
                {
                  "eventId": "evt-1",
                  "eventType": "BookingCreated",
                  "occurredAt": "2026-04-10T10:00:00Z",
                  "correlationId": "corr-1",
                  "bookingId": "booking-1",
                  "shopId": "shop-1",
                  "barberId": "barber-1",
                  "customerId": "customer-1",
                  "date": "2026-04-10",
                  "startTime": "10:00",
                  "durationMinutes": 30,
                  "serviceCode": "HAIRCUT",
                  "status": "PENDING"
                }
                """;

    listener.listen(payload);

    verify(consumeBookingCreatedUseCase).consume(payload);
  }

  @Test
  void shouldSwallowNonRetryableFailure() {
    String payload = "{\"payload\":\"value\"}";

    doThrow(new SlotValidationException("Requested slot is not available"))
        .when(consumeBookingCreatedUseCase)
        .consume(payload);

    assertDoesNotThrow(() -> listener.listen(payload));
  }

  @Test
  void shouldRethrowRetryableFailure() {
    String payload = "{\"payload\":\"value\"}";

    doThrow(new RuntimeException("Temporary failure"))
        .when(consumeBookingCreatedUseCase)
        .consume(payload);

    assertThrows(RetryableMessageException.class, () -> listener.listen(payload));
  }
}
