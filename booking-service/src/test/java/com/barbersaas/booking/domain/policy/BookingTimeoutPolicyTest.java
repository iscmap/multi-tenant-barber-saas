package com.barbersaas.booking.domain.policy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class BookingTimeoutPolicyTest {

  @Test
  void shouldReturnTrueWhenBookingIsTimedOut() {
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
            .createdAt(LocalDateTime.of(2026, 4, 10, 10, 0))
            .build();

    OffsetDateTime now = OffsetDateTime.of(2026, 4, 10, 10, 5, 0, 0, ZoneOffset.UTC);

    assertTrue(BookingTimeoutPolicy.isTimedOut(booking, now));
  }

  @Test
  void shouldReturnFalseWhenBookingIsNotTimedOut() {
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
            .createdAt(LocalDateTime.of(2026, 4, 10, 10, 0))
            .build();

    OffsetDateTime now = OffsetDateTime.of(2026, 4, 10, 10, 4, 59, 0, ZoneOffset.UTC);

    assertFalse(BookingTimeoutPolicy.isTimedOut(booking, now));
  }
}
