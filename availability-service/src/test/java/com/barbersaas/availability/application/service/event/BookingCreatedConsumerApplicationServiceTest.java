package com.barbersaas.availability.application.service.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.barbersaas.availability.application.port.in.validation.ValidateSlotUseCase;
import org.junit.jupiter.api.Test;

class BookingCreatedConsumerApplicationServiceTest {

  private final ValidateSlotUseCase validateSlotUseCase = mock(ValidateSlotUseCase.class);
  private final BookingCreatedConsumerApplicationService service =
      new BookingCreatedConsumerApplicationService(validateSlotUseCase);

  @Test
  void shouldParseBookingCreatedAndValidateSlot() {
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

    service.consume(payload);

    verify(validateSlotUseCase).validateSlot("shop-1", "barber-1", "2026-04-10", "10:00", 30);
  }
}
