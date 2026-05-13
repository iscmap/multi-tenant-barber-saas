package com.barbersaas.availability.domain.rule;

import com.barbersaas.availability.domain.enums.AvailabilityStatus;
import com.barbersaas.availability.domain.exception.SlotValidationException;
import com.barbersaas.availability.domain.model.BarberAvailability;
import com.barbersaas.availability.domain.model.BarberSchedule;
import java.time.LocalTime;

public final class SlotValidationRules {

  private SlotValidationRules() {}

  public static void validateReservable(
      BarberSchedule schedule,
      BarberAvailability availability,
      LocalTime requestedStartTime,
      int requestedDurationMinutes) {

    if (schedule == null) {
      throw new SlotValidationException("Barber is not scheduled for the requested date");
    }

    if (requestedDurationMinutes != schedule.getSlotDurationMinutes()) {
      throw new SlotValidationException("Requested duration does not match barber slot duration");
    }

    LocalTime requestedEndTime = requestedStartTime.plusMinutes(requestedDurationMinutes);

    if (requestedStartTime.isBefore(schedule.getWorkStartTime())) {
      throw new SlotValidationException("Requested slot starts before barber working hours");
    }

    if (requestedEndTime.isAfter(schedule.getWorkEndTime())) {
      throw new SlotValidationException("Requested slot ends after barber working hours");
    }

    if (availability == null) {
      throw new SlotValidationException("Requested slot does not exist");
    }

    if (availability.getStatus() != AvailabilityStatus.AVAILABLE) {
      throw new SlotValidationException("Requested slot is not available");
    }
  }
}
