package com.barbersaas.booking.application.service.timeout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.barbersaas.booking.application.command.timeout.RejectTimedOutBookingsCommand;
import com.barbersaas.booking.application.port.out.SaveBookingPort;
import com.barbersaas.booking.application.port.out.timeout.LoadPendingBookingsPort;
import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingTimeoutApplicationServiceTest {

  private LoadPendingBookingsPort loadPendingBookingsPort;
  private SaveBookingPort saveBookingPort;

  private BookingTimeoutApplicationService service;

  @BeforeEach
  void setUp() {
    loadPendingBookingsPort = mock(LoadPendingBookingsPort.class);

    saveBookingPort = mock(SaveBookingPort.class);

    service = new BookingTimeoutApplicationService(loadPendingBookingsPort, saveBookingPort);
  }

  @Test
  void shouldRejectTimedOutPendingBookings() {

    Booking oldPendingBooking =
        Booking.builder()
            .bookingId("booking-old")
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .status(BookingStatus.PENDING)
            .createdAt(LocalDateTime.of(2026, 4, 10, 10, 0))
            .build();

    Booking freshPendingBooking =
        Booking.builder()
            .bookingId("booking-fresh")
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 10))
            .durationMinutes(30)
            .serviceCode("BEARD")
            .status(BookingStatus.PENDING)
            .createdAt(LocalDateTime.of(2026, 4, 10, 10, 4))
            .build();

    when(loadPendingBookingsPort.loadPendingBookings())
        .thenReturn(List.of(oldPendingBooking, freshPendingBooking));

    when(saveBookingPort.save(any(Booking.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    RejectTimedOutBookingsCommand command =
        RejectTimedOutBookingsCommand.builder()
            .now(OffsetDateTime.of(2026, 4, 10, 10, 6, 0, 0, ZoneOffset.UTC))
            .build();

    int rejectedCount = service.rejectTimedOutBookings(command);

    assertEquals(1, rejectedCount);

    verify(saveBookingPort)
        .save(
            org.mockito.ArgumentMatchers.argThat(
                booking ->
                    booking.getBookingId().equals("booking-old")
                        && booking.getStatus() == BookingStatus.REJECTED));
  }
}
