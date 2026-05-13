package com.barbersaas.availability.application.service;

import com.barbersaas.availability.application.port.in.GetBarberAvailabilityUseCase;
import com.barbersaas.availability.application.port.out.LoadBarberAvailabilityPort;
import com.barbersaas.availability.domain.model.BarberAvailability;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityApplicationService implements GetBarberAvailabilityUseCase {

  private final LoadBarberAvailabilityPort loadBarberAvailabilityPort;

  public AvailabilityApplicationService(LoadBarberAvailabilityPort loadBarberAvailabilityPort) {
    this.loadBarberAvailabilityPort = loadBarberAvailabilityPort;
  }

  @Override
  public BarberAvailability getAvailability(
      String shopId, String barberId, String date, String startTime) {
    return loadBarberAvailabilityPort
        .loadByBarberAndSlot(shopId, barberId, LocalDate.parse(date), LocalTime.parse(startTime))
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Availability not found for barber "
                        + barberId
                        + " at "
                        + date
                        + " "
                        + startTime));
  }
}
