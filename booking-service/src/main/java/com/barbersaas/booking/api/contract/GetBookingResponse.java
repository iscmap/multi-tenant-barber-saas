package com.barbersaas.booking.api.contract;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GetBookingResponse {

  String bookingId;
  String shopId;
  String barberId;
  String customerId;
  LocalDate date;
  LocalTime startTime;
  Integer durationMinutes;
  String serviceCode;
  String status;
}
