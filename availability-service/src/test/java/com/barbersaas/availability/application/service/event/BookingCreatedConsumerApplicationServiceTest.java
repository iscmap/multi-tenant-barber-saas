package com.barbersaas.availability.application.service.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.barbersaas.availability.adapters.out.deduplication.memory.InMemoryProcessedBookingEventRepository;
import com.barbersaas.availability.adapters.out.persistence.memory.InMemoryBarberAvailabilityRepository;
import com.barbersaas.availability.application.factory.AvailabilityEventFactory;
import com.barbersaas.availability.application.port.out.event.PublishAvailabilityDecidedEventPort;
import com.barbersaas.availability.application.service.AvailabilityApplicationService;
import com.barbersaas.availability.domain.enums.AvailabilityStatus;
import com.barbersaas.availability.domain.model.BarberAvailability;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class BookingCreatedConsumerApplicationServiceTest {

  private final InMemoryBarberAvailabilityRepository repository =
      new InMemoryBarberAvailabilityRepository();
  private final InMemoryProcessedBookingEventRepository processedEventRepository =
      new InMemoryProcessedBookingEventRepository();
  private final AvailabilityApplicationService validateSlotUseCase =
      new AvailabilityApplicationService(repository, repository);
  private final PublishAvailabilityDecidedEventPort publishAvailabilityDecidedEventPort =
      mock(PublishAvailabilityDecidedEventPort.class);
  private final AvailabilityEventFactory availabilityEventFactory = new AvailabilityEventFactory();
  private final BookingCreatedConsumerApplicationService service =
      new BookingCreatedConsumerApplicationService(
          validateSlotUseCase,
          repository,
          repository,
          publishAvailabilityDecidedEventPort,
          availabilityEventFactory,
          processedEventRepository,
          processedEventRepository);

  @Test
  void shouldParseBookingCreatedValidateReserveAndPublishConfirmed() {
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

    BarberAvailability reservedSlot =
        repository
            .loadByBarberAndSlot(
                "shop-1", "barber-1", LocalDate.of(2026, 4, 10), LocalTime.of(10, 0))
            .orElseThrow();

    assertEquals(AvailabilityStatus.RESERVED, reservedSlot.getStatus());

    verify(publishAvailabilityDecidedEventPort)
        .publish(
            argThat(
                event ->
                    event.getPayload().getDecision().equals("CONFIRMED")
                        && event.getPayload().getReason().equals("SLOT_RESERVED")
                        && event.getPayload().getBookingId().equals("booking-1")));
  }

  @Test
  void shouldIgnoreDuplicateBookingCreatedEvent() {
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
    service.consume(payload);

    BarberAvailability reservedSlot =
        repository
            .loadByBarberAndSlot(
                "shop-1", "barber-1", LocalDate.of(2026, 4, 10), LocalTime.of(10, 0))
            .orElseThrow();

    assertEquals(AvailabilityStatus.RESERVED, reservedSlot.getStatus());

    verify(publishAvailabilityDecidedEventPort, times(1))
        .publish(
            argThat(
                event ->
                    event.getPayload().getDecision().equals("CONFIRMED")
                        && event.getPayload().getBookingId().equals("booking-1")));
  }
}
