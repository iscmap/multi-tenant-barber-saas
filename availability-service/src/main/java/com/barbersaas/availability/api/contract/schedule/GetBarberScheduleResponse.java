package com.barbersaas.availability.api.contract.schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GetBarberScheduleResponse {

  String shopId;
  String barberId;
  LocalDate date;
  LocalTime workStartTime;
  LocalTime workEndTime;
  Integer slotDurationMinutes;
}
