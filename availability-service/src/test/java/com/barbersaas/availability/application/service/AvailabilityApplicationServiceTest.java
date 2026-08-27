package com.barbersaas.availability.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.barbersaas.availability.application.port.out.LoadBarberAvailabilityPort;
import com.barbersaas.availability.application.port.out.schedule.LoadBarberSchedulePort;
import com.barbersaas.availability.domain.enums.AvailabilityStatus;
import com.barbersaas.availability.domain.exception.SlotValidationException;
import com.barbersaas.availability.domain.model.BarberAvailability;
import com.barbersaas.availability.domain.model.BarberSchedule;
import com.barbersaas.availability.observability.metrics.AvailabilityMetrics;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AvailabilityApplicationServiceTest {

  private LoadBarberAvailabilityPort loadBarberAvailabilityPort;
  private LoadBarberSchedulePort loadBarberSchedulePort;

  private AvailabilityApplicationService service;
  private AvailabilityMetrics availabilityMetrics;

  @BeforeEach
  void setUp() {
    loadBarberAvailabilityPort = mock(LoadBarberAvailabilityPort.class);

    loadBarberSchedulePort = mock(LoadBarberSchedulePort.class);

    availabilityMetrics = mock(AvailabilityMetrics.class);

    service =
        new AvailabilityApplicationService(
            loadBarberAvailabilityPort, loadBarberSchedulePort, availabilityMetrics);
  }

  @Test
  void shouldReturnAvailabilityByBarberAndSlot() {

    BarberAvailability availability = availableSlot();

    when(loadBarberAvailabilityPort.loadByBarberAndSlot(
            "shop-1", "barber-1", LocalDate.of(2026, 4, 10), LocalTime.of(10, 0)))
        .thenReturn(Optional.of(availability));

    BarberAvailability result =
        service.getAvailability("shop-1", "barber-1", "2026-04-10", "10:00");

    assertEquals("shop-1", result.getShopId());

    assertEquals("barber-1", result.getBarberId());

    assertEquals(AvailabilityStatus.AVAILABLE, result.getStatus());
  }

  @Test
  void shouldThrowWhenAvailabilityDoesNotExist() {

    when(loadBarberAvailabilityPort.loadByBarberAndSlot(
            "shop-1", "barber-9", LocalDate.of(2026, 4, 10), LocalTime.of(10, 0)))
        .thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getAvailability("shop-1", "barber-9", "2026-04-10", "10:00"));

    assertEquals(
        "Availability not found for barber barber-9 at 2026-04-10 10:00", exception.getMessage());
  }

  @Test
  void shouldReturnScheduleByBarberAndDate() {

    BarberSchedule schedule = barberSchedule();

    when(loadBarberSchedulePort.loadByBarberAndDate(
            "shop-1", "barber-1", LocalDate.of(2026, 4, 10)))
        .thenReturn(Optional.of(schedule));

    BarberSchedule result = service.getSchedule("shop-1", "barber-1", "2026-04-10");

    assertEquals("shop-1", result.getShopId());

    assertEquals("barber-1", result.getBarberId());

    assertEquals(30, result.getSlotDurationMinutes());

    assertEquals(LocalTime.of(10, 0), result.getWorkStartTime());

    assertEquals(LocalTime.of(18, 0), result.getWorkEndTime());
  }

  @Test
  void shouldThrowWhenScheduleDoesNotExist() {

    when(loadBarberSchedulePort.loadByBarberAndDate(
            "shop-1", "barber-9", LocalDate.of(2026, 4, 10)))
        .thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getSchedule("shop-1", "barber-9", "2026-04-10"));

    assertEquals("Schedule not found for barber barber-9 on 2026-04-10", exception.getMessage());
  }

  @Test
  void shouldValidateReservableSlot() {

    when(loadBarberSchedulePort.loadByBarberAndDate(
            "shop-1", "barber-1", LocalDate.of(2026, 4, 10)))
        .thenReturn(Optional.of(barberSchedule()));

    when(loadBarberAvailabilityPort.loadByBarberAndSlot(
            "shop-1", "barber-1", LocalDate.of(2026, 4, 10), LocalTime.of(10, 0)))
        .thenReturn(Optional.of(availableSlot()));

    assertDoesNotThrow(() -> service.validateSlot("shop-1", "barber-1", "2026-04-10", "10:00", 30));
  }

  @Test
  void shouldFailWhenDurationDoesNotMatchSchedule() {

    when(loadBarberSchedulePort.loadByBarberAndDate(
            "shop-1", "barber-1", LocalDate.of(2026, 4, 10)))
        .thenReturn(Optional.of(barberSchedule()));

    SlotValidationException exception =
        assertThrows(
            SlotValidationException.class,
            () -> service.validateSlot("shop-1", "barber-1", "2026-04-10", "10:00", 45));

    assertEquals("Requested duration does not match barber slot duration", exception.getMessage());
  }

  @Test
  void shouldFailWhenSlotStartsBeforeWorkingHours() {

    when(loadBarberSchedulePort.loadByBarberAndDate(
            "shop-1", "barber-1", LocalDate.of(2026, 4, 10)))
        .thenReturn(Optional.of(barberSchedule()));

    SlotValidationException exception =
        assertThrows(
            SlotValidationException.class,
            () -> service.validateSlot("shop-1", "barber-1", "2026-04-10", "09:30", 30));

    assertEquals("Requested slot starts before barber working hours", exception.getMessage());
  }

  @Test
  void shouldFailWhenSlotDoesNotExist() {

    when(loadBarberSchedulePort.loadByBarberAndDate(
            "shop-1", "barber-1", LocalDate.of(2026, 4, 10)))
        .thenReturn(Optional.of(barberSchedule()));

    when(loadBarberAvailabilityPort.loadByBarberAndSlot(
            "shop-1", "barber-1", LocalDate.of(2026, 4, 10), LocalTime.of(11, 0)))
        .thenReturn(Optional.empty());

    SlotValidationException exception =
        assertThrows(
            SlotValidationException.class,
            () -> service.validateSlot("shop-1", "barber-1", "2026-04-10", "11:00", 30));

    assertEquals("Requested slot does not exist", exception.getMessage());
  }

  private BarberSchedule barberSchedule() {

    return BarberSchedule.builder()
        .shopId("shop-1")
        .barberId("barber-1")
        .date(LocalDate.of(2026, 4, 10))
        .workStartTime(LocalTime.of(10, 0))
        .workEndTime(LocalTime.of(18, 0))
        .slotDurationMinutes(30)
        .build();
  }

  private BarberAvailability availableSlot() {

    return BarberAvailability.builder()
        .shopId("shop-1")
        .barberId("barber-1")
        .date(LocalDate.of(2026, 4, 10))
        .startTime(LocalTime.of(10, 0))
        .durationMinutes(30)
        .status(AvailabilityStatus.AVAILABLE)
        .build();
  }
}
