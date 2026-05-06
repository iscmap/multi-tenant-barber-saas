package com.barbersaas.booking.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class BookingStateTransitionsTest {

  @Test
  void shouldConfirmPendingBooking() {
    Booking booking =
        Booking.builder()
            .bookingId("booking-1")
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .status(BookingStatus.PENDING)
            .build();

    Booking confirmedBooking = BookingStateTransitions.confirm(booking);

    assertEquals(BookingStatus.CONFIRMED, confirmedBooking.getStatus());
    assertEquals("booking-1", confirmedBooking.getBookingId());
  }

  @Test
  void shouldRejectPendingBooking() {
    Booking booking =
        Booking.builder()
            .bookingId("booking-2")
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .status(BookingStatus.PENDING)
            .build();

    Booking rejectedBooking = BookingStateTransitions.reject(booking);

    assertEquals(BookingStatus.REJECTED, rejectedBooking.getStatus());
    assertEquals("booking-2", rejectedBooking.getBookingId());
  }

  @Test
  void shouldFailWhenConfirmingConfirmedBooking() {
    Booking booking =
        Booking.builder()
            .bookingId("booking-3")
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .status(BookingStatus.CONFIRMED)
            .build();

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> BookingStateTransitions.confirm(booking));

    assertEquals(
        "Invalid booking status transition from CONFIRMED to CONFIRMED", exception.getMessage());
  }

  @Test
  void shouldFailWhenRejectingRejectedBooking() {
    Booking booking =
        Booking.builder()
            .bookingId("booking-4")
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .status(BookingStatus.REJECTED)
            .build();

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> BookingStateTransitions.reject(booking));

    assertEquals(
        "Invalid booking status transition from REJECTED to REJECTED", exception.getMessage());
  }
}
