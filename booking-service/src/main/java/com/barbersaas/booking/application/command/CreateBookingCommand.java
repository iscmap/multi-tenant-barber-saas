package com.barbersaas.booking.application.command;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateBookingCommand {

  String shopId;
  String barberId;
  String customerId;
  LocalDate date;
  LocalTime startTime;
  Integer durationMinutes;
  String serviceCode;
}
