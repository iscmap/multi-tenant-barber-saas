package com.barbersaas.booking.application.service.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.barbersaas.booking.application.port.out.LoadBookingPort;
import com.barbersaas.booking.application.port.out.SaveBookingPort;
import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AvailabilityDecidedConsumerApplicationServiceTest {

  private LoadBookingPort loadBookingPort;
  private SaveBookingPort saveBookingPort;
  private AvailabilityDecidedConsumerApplicationService service;

  @BeforeEach
  void setUp() {
    loadBookingPort = mock(LoadBookingPort.class);
    saveBookingPort = mock(SaveBookingPort.class);

    service = new AvailabilityDecidedConsumerApplicationService(loadBookingPort, saveBookingPort);
  }

  @Test
  void shouldConfirmBookingWhenDecisionIsConfirmed() {

    Booking booking = booking("booking-1", BookingStatus.PENDING);

    when(loadBookingPort.loadById("booking-1")).thenReturn(Optional.of(booking));

    when(saveBookingPort.save(any(Booking.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

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

    service.consume(payload);

    verify(saveBookingPort)
        .save(
            org.mockito.ArgumentMatchers.argThat(
                savedBooking ->
                    savedBooking.getBookingId().equals("booking-1")
                        && savedBooking.getStatus() == BookingStatus.CONFIRMED));
  }

  @Test
  void shouldRejectBookingWhenDecisionIsRejected() {

    Booking booking = booking("booking-2", BookingStatus.PENDING);

    when(loadBookingPort.loadById("booking-2")).thenReturn(Optional.of(booking));

    when(saveBookingPort.save(any(Booking.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    String payload =
        """
            {
              "eventId": "evt-2",
              "eventType": "AvailabilityDecided",
              "occurredAt": "2026-04-10T10:00:00Z",
              "correlationId": "corr-2",
              "source": "availability-service",
              "tenantId": "shop-1",
              "payload": {
                "eventId": "evt-2",
                "eventType": "AvailabilityDecided",
                "occurredAt": "2026-04-10T10:00:00Z",
                "correlationId": "corr-2",
                "bookingId": "booking-2",
                "shopId": "shop-1",
                "barberId": "barber-1",
                "decision": "REJECTED",
                "reason": "Requested slot is not available"
              }
            }
            """;

    service.consume(payload);

    verify(saveBookingPort)
        .save(
            org.mockito.ArgumentMatchers.argThat(
                savedBooking ->
                    savedBooking.getBookingId().equals("booking-2")
                        && savedBooking.getStatus() == BookingStatus.REJECTED));
  }

  @Test
  void shouldThrowWhenDecisionIsUnsupported() {

    Booking booking = booking("booking-3", BookingStatus.PENDING);

    when(loadBookingPort.loadById("booking-3")).thenReturn(Optional.of(booking));

    String payload =
        """
            {
              "eventId": "evt-3",
              "eventType": "AvailabilityDecided",
              "occurredAt": "2026-04-10T10:00:00Z",
              "correlationId": "corr-3",
              "source": "availability-service",
              "tenantId": "shop-1",
              "payload": {
                "eventId": "evt-3",
                "eventType": "AvailabilityDecided",
                "occurredAt": "2026-04-10T10:00:00Z",
                "correlationId": "corr-3",
                "bookingId": "booking-3",
                "shopId": "shop-1",
                "barberId": "barber-1",
                "decision": "MAYBE",
                "reason": "UNKNOWN"
              }
            }
            """;

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> service.consume(payload));

    assertEquals("Unsupported availability decision: MAYBE", exception.getMessage());
  }

  private Booking booking(String bookingId, BookingStatus status) {

    return Booking.builder()
        .bookingId(bookingId)
        .shopId("shop-1")
        .barberId("barber-1")
        .customerId("customer-1")
        .date(LocalDate.of(2026, 4, 10))
        .startTime(LocalTime.of(10, 0))
        .durationMinutes(30)
        .serviceCode("HAIRCUT")
        .status(status)
        .createdAt(LocalDateTime.of(2026, 4, 10, 10, 0))
        .build();
  }
}
