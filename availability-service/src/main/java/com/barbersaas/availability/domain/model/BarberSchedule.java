package com.barbersaas.availability.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BarberSchedule {

  String shopId;
  String barberId;
  LocalDate date;
  LocalTime workStartTime;
  LocalTime workEndTime;
  Integer slotDurationMinutes;
}
