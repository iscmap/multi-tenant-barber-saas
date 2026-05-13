package com.barbersaas.availability.domain.rule;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.barbersaas.availability.domain.enums.AvailabilityStatus;
import com.barbersaas.availability.domain.exception.SlotValidationException;
import com.barbersaas.availability.domain.model.BarberAvailability;
import com.barbersaas.availability.domain.model.BarberSchedule;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class SlotValidationRulesTest {

  @Test
  void shouldValidateReservableSlot() {
    BarberSchedule schedule =
        BarberSchedule.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .date(LocalDate.of(2026, 4, 10))
            .workStartTime(LocalTime.of(10, 0))
            .workEndTime(LocalTime.of(18, 0))
            .slotDurationMinutes(30)
            .build();

    BarberAvailability availability =
        BarberAvailability.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .status(AvailabilityStatus.AVAILABLE)
            .build();

    assertDoesNotThrow(
        () ->
            SlotValidationRules.validateReservable(
                schedule, availability, LocalTime.of(10, 0), 30));
  }

  @Test
  void shouldFailWhenScheduleIsMissing() {
    BarberAvailability availability =
        BarberAvailability.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .status(AvailabilityStatus.AVAILABLE)
            .build();

    SlotValidationException exception =
        assertThrows(
            SlotValidationException.class,
            () ->
                SlotValidationRules.validateReservable(
                    null, availability, LocalTime.of(10, 0), 30));

    assertEquals("Barber is not scheduled for the requested date", exception.getMessage());
  }

  @Test
  void shouldFailWhenRequestedSlotEndsAfterWorkingHours() {
    BarberSchedule schedule =
        BarberSchedule.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .date(LocalDate.of(2026, 4, 10))
            .workStartTime(LocalTime.of(10, 0))
            .workEndTime(LocalTime.of(18, 0))
            .slotDurationMinutes(30)
            .build();

    BarberAvailability availability =
        BarberAvailability.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(17, 45))
            .durationMinutes(30)
            .status(AvailabilityStatus.AVAILABLE)
            .build();

    SlotValidationException exception =
        assertThrows(
            SlotValidationException.class,
            () ->
                SlotValidationRules.validateReservable(
                    schedule, availability, LocalTime.of(17, 45), 30));

    assertEquals("Requested slot ends after barber working hours", exception.getMessage());
  }

  @Test
  void shouldFailWhenSlotIsReserved() {
    BarberSchedule schedule =
        BarberSchedule.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .date(LocalDate.of(2026, 4, 10))
            .workStartTime(LocalTime.of(10, 0))
            .workEndTime(LocalTime.of(18, 0))
            .slotDurationMinutes(30)
            .build();

    BarberAvailability availability =
        BarberAvailability.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .status(AvailabilityStatus.RESERVED)
            .build();

    SlotValidationException exception =
        assertThrows(
            SlotValidationException.class,
            () ->
                SlotValidationRules.validateReservable(
                    schedule, availability, LocalTime.of(10, 0), 30));

    assertEquals("Requested slot is not available", exception.getMessage());
  }
}
