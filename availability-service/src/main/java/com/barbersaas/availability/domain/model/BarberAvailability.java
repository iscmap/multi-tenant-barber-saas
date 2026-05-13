package com.barbersaas.availability.domain.model;

import com.barbersaas.availability.domain.enums.AvailabilityStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BarberAvailability {
  String shopId;
  String barberId;
  LocalDate date;
  LocalTime startTime;
  Integer durationMinutes;
  AvailabilityStatus status;
}
