package com.barbersaas.availability.application.service.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.barbersaas.availability.application.factory.AvailabilityEventFactory;
import com.barbersaas.availability.application.port.in.validation.ValidateSlotUseCase;
import com.barbersaas.availability.application.port.out.LoadBarberAvailabilityPort;
import com.barbersaas.availability.application.port.out.deduplication.LoadProcessedBookingEventPort;
import com.barbersaas.availability.application.port.out.deduplication.SaveProcessedBookingEventPort;
import com.barbersaas.availability.application.port.out.event.PublishAvailabilityDecidedEventPort;
import com.barbersaas.availability.application.port.out.reservation.ReserveBarberAvailabilityPort;
import com.barbersaas.availability.domain.enums.AvailabilityStatus;
import com.barbersaas.availability.domain.model.BarberAvailability;
import com.barbersaas.availability.domain.model.event.ProcessedBookingEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingCreatedConsumerApplicationServiceTest {

  private ValidateSlotUseCase validateSlotUseCase;
  private LoadBarberAvailabilityPort loadBarberAvailabilityPort;
  private ReserveBarberAvailabilityPort reserveBarberAvailabilityPort;
  private LoadProcessedBookingEventPort loadProcessedBookingEventPort;
  private SaveProcessedBookingEventPort saveProcessedBookingEventPort;
  private PublishAvailabilityDecidedEventPort publishAvailabilityDecidedEventPort;

  private BookingCreatedConsumerApplicationService service;

  @BeforeEach
  void setUp() {

    validateSlotUseCase = mock(ValidateSlotUseCase.class);

    loadBarberAvailabilityPort = mock(LoadBarberAvailabilityPort.class);

    reserveBarberAvailabilityPort = mock(ReserveBarberAvailabilityPort.class);

    loadProcessedBookingEventPort = mock(LoadProcessedBookingEventPort.class);

    saveProcessedBookingEventPort = mock(SaveProcessedBookingEventPort.class);

    publishAvailabilityDecidedEventPort = mock(PublishAvailabilityDecidedEventPort.class);

    service =
        new BookingCreatedConsumerApplicationService(
            validateSlotUseCase,
            loadBarberAvailabilityPort,
            reserveBarberAvailabilityPort,
            publishAvailabilityDecidedEventPort,
            new AvailabilityEventFactory(),
            loadProcessedBookingEventPort,
            saveProcessedBookingEventPort);
  }

  @Test
  void shouldParseBookingCreatedValidateReserveAndPublishConfirmed() {

    BarberAvailability availableSlot =
        BarberAvailability.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .status(AvailabilityStatus.AVAILABLE)
            .build();

    BarberAvailability reservedSlot =
        BarberAvailability.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .status(AvailabilityStatus.RESERVED)
            .build();

    when(loadProcessedBookingEventPort.loadByBookingIdAndEventType("booking-1", "BookingCreated"))
        .thenReturn(Optional.empty());

    when(loadBarberAvailabilityPort.loadByBarberAndSlot(
            "shop-1", "barber-1", LocalDate.of(2026, 4, 10), LocalTime.of(10, 0)))
        .thenReturn(Optional.of(availableSlot));

    when(reserveBarberAvailabilityPort.reserve(availableSlot)).thenReturn(reservedSlot);

    when(saveProcessedBookingEventPort.save(any(ProcessedBookingEvent.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    String payload =
        """
            {
              "eventId": "evt-1",
              "eventType": "BookingCreated",
              "occurredAt": "2026-04-10T10:00:00Z",
              "correlationId": "corr-1",
              "source": "booking-service",
              "tenantId": "shop-1",
              "payload": {
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
            }
            """;

    service.consume(payload);

    verify(validateSlotUseCase).validateSlot("shop-1", "barber-1", "2026-04-10", "10:00", 30);

    verify(reserveBarberAvailabilityPort).reserve(availableSlot);

    verify(publishAvailabilityDecidedEventPort)
        .publish(
            argThat(
                event ->
                    event.getPayload().getDecision().equals("CONFIRMED")
                        && event.getPayload().getReason().equals("SLOT_RESERVED")
                        && event.getPayload().getBookingId().equals("booking-1")));

    verify(saveProcessedBookingEventPort)
        .save(
            argThat(
                processedEvent ->
                    processedEvent.getBookingId().equals("booking-1")
                        && processedEvent.getEventType().equals("BookingCreated")));
  }

  @Test
  void shouldIgnoreDuplicateBookingCreatedEvent() {

    ProcessedBookingEvent processedEvent =
        ProcessedBookingEvent.builder()
            .bookingId("booking-1")
            .eventType("BookingCreated")
            .processedAt(java.time.LocalDateTime.now())
            .build();

    when(loadProcessedBookingEventPort.loadByBookingIdAndEventType("booking-1", "BookingCreated"))
        .thenReturn(Optional.of(processedEvent));

    String payload =
        """
            {
              "eventId": "evt-1",
              "eventType": "BookingCreated",
              "occurredAt": "2026-04-10T10:00:00Z",
              "correlationId": "corr-1",
              "source": "booking-service",
              "tenantId": "shop-1",
              "payload": {
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
            }
            """;

    service.consume(payload);
    service.consume(payload);

    verify(publishAvailabilityDecidedEventPort, times(0)).publish(any());

    verify(reserveBarberAvailabilityPort, times(0)).reserve(any());

    verify(saveProcessedBookingEventPort, times(0)).save(any());
  }
}
