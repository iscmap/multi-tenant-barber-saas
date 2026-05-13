package com.barbersaas.availability.application.port.out;

import com.barbersaas.availability.domain.model.BarberAvailability;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public interface LoadBarberAvailabilityPort {

  Optional<BarberAvailability> loadByBarberAndSlot(
      String shopId, String barberId, LocalDate date, LocalTime startTime);
}
