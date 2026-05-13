package com.barbersaas.booking.domain.model;

import com.barbersaas.booking.domain.enums.BookingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Booking {
  String bookingId;
  String shopId;
  String barberId;
  String customerId;
  LocalDate date;
  LocalTime startTime;
  Integer durationMinutes;
  String serviceCode;
  BookingStatus status;
  LocalDateTime createdAt;
}
