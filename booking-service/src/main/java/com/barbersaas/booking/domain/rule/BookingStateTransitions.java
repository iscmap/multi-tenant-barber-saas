package com.barbersaas.booking.domain.rule;

import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;

public class BookingStateTransitions {

  private BookingStateTransitions() {}

  public static Booking confirm(Booking booking) {
    validateTransition(booking.getStatus(), BookingStatus.CONFIRMED);

    return Booking.builder()
        .bookingId(booking.getBookingId())
        .shopId(booking.getShopId())
        .barberId(booking.getBarberId())
        .customerId(booking.getCustomerId())
        .date(booking.getDate())
        .startTime(booking.getStartTime())
        .durationMinutes(booking.getDurationMinutes())
        .serviceCode(booking.getServiceCode())
        .status(BookingStatus.CONFIRMED)
        .createdAt(booking.getCreatedAt())
        .build();
  }

  public static Booking reject(Booking booking) {
    validateTransition(booking.getStatus(), BookingStatus.REJECTED);

    return Booking.builder()
        .bookingId(booking.getBookingId())
        .shopId(booking.getShopId())
        .barberId(booking.getBarberId())
        .customerId(booking.getCustomerId())
        .date(booking.getDate())
        .startTime(booking.getStartTime())
        .durationMinutes(booking.getDurationMinutes())
        .serviceCode(booking.getServiceCode())
        .status(BookingStatus.REJECTED)
        .createdAt(booking.getCreatedAt())
        .build();
  }

  private static void validateTransition(BookingStatus currentStatus, BookingStatus targetStatus) {
    if (currentStatus == null) {
      throw new IllegalArgumentException("Booking status is required");
    }

    if (currentStatus != BookingStatus.PENDING) {
      throw new IllegalStateException(
          "Invalid booking status transition from " + currentStatus + " to " + targetStatus);
    }
  }
}
