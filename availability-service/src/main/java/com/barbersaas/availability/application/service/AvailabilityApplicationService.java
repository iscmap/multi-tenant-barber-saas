package com.barbersaas.availability.application.service;

import com.barbersaas.availability.application.port.in.GetBarberAvailabilityUseCase;
import com.barbersaas.availability.application.port.in.schedule.GetBarberScheduleUseCase;
import com.barbersaas.availability.application.port.in.validation.ValidateSlotUseCase;
import com.barbersaas.availability.application.port.out.LoadBarberAvailabilityPort;
import com.barbersaas.availability.application.port.out.schedule.LoadBarberSchedulePort;
import com.barbersaas.availability.domain.model.BarberAvailability;
import com.barbersaas.availability.domain.model.BarberSchedule;
import com.barbersaas.availability.domain.rule.SlotValidationRules;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityApplicationService
    implements GetBarberAvailabilityUseCase, GetBarberScheduleUseCase, ValidateSlotUseCase {

  private final LoadBarberAvailabilityPort loadBarberAvailabilityPort;
  private final LoadBarberSchedulePort loadBarberSchedulePort;

  public AvailabilityApplicationService(
      LoadBarberAvailabilityPort loadBarberAvailabilityPort,
      LoadBarberSchedulePort loadBarberSchedulePort) {
    this.loadBarberAvailabilityPort = loadBarberAvailabilityPort;
    this.loadBarberSchedulePort = loadBarberSchedulePort;
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

  @Override
  public BarberSchedule getSchedule(String shopId, String barberId, String date) {
    return loadBarberSchedulePort
        .loadByBarberAndDate(shopId, barberId, LocalDate.parse(date))
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Schedule not found for barber " + barberId + " on " + date));
  }

  @Override
  public void validateSlot(
      String shopId, String barberId, String date, String startTime, int durationMinutes) {
    LocalDate localDate = LocalDate.parse(date);
    LocalTime localStartTime = LocalTime.parse(startTime);

    BarberSchedule schedule =
        loadBarberSchedulePort.loadByBarberAndDate(shopId, barberId, localDate).orElse(null);

    BarberAvailability availability =
        loadBarberAvailabilityPort
            .loadByBarberAndSlot(shopId, barberId, localDate, localStartTime)
            .orElse(null);

    SlotValidationRules.validateReservable(schedule, availability, localStartTime, durationMinutes);
  }
}
