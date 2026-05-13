package com.barbersaas.availability.api.contract;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GetBarberAvailabilityResponse {

  String shopId;
  String barberId;
  LocalDate date;
  LocalTime startTime;
  Integer durationMinutes;
  String status;
}
