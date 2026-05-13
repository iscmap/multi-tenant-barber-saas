package com.barbersaas.booking.domain.policy;

import com.barbersaas.booking.domain.model.Booking;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class BookingTimeoutPolicy {
  public static final long TIMEOUT_MINUTES = 5L;

  private BookingTimeoutPolicy() {}

  public static boolean isTimedOut(Booking booking, OffsetDateTime now) {
    OffsetDateTime bookingCreatedAt = booking.getCreatedAt().atOffset(ZoneOffset.UTC);
    return bookingCreatedAt.plusMinutes(TIMEOUT_MINUTES).isBefore(now)
        || bookingCreatedAt.plusMinutes(TIMEOUT_MINUTES).isEqual(now);
  }
}
