package com.barbersaas.availability.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.barbersaas.availability.adapters.out.persistence.memory.InMemoryBarberAvailabilityRepository;
import com.barbersaas.availability.domain.enums.AvailabilityStatus;
import com.barbersaas.availability.domain.model.BarberAvailability;
import com.barbersaas.availability.domain.model.BarberSchedule;
import org.junit.jupiter.api.Test;

class AvailabilityApplicationServiceTest {

  private final InMemoryBarberAvailabilityRepository repository =
      new InMemoryBarberAvailabilityRepository();
  private final AvailabilityApplicationService service =
      new AvailabilityApplicationService(repository, repository);

  @Test
  void shouldReturnAvailabilityByBarberAndSlot() {
    BarberAvailability availability =
        service.getAvailability("shop-1", "barber-1", "2026-04-10", "10:00");

    assertEquals("shop-1", availability.getShopId());
    assertEquals("barber-1", availability.getBarberId());
    assertEquals(AvailabilityStatus.AVAILABLE, availability.getStatus());
  }

  @Test
  void shouldThrowWhenAvailabilityDoesNotExist() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getAvailability("shop-1", "barber-9", "2026-04-10", "10:00"));

    assertEquals(
        "Availability not found for barber barber-9 at 2026-04-10 10:00", exception.getMessage());
  }

  @Test
  void shouldReturnScheduleByBarberAndDate() {
    BarberSchedule schedule = service.getSchedule("shop-1", "barber-1", "2026-04-10");

    assertEquals("shop-1", schedule.getShopId());
    assertEquals("barber-1", schedule.getBarberId());
    assertEquals(30, schedule.getSlotDurationMinutes());
    assertEquals(java.time.LocalTime.of(10, 0), schedule.getWorkStartTime());
    assertEquals(java.time.LocalTime.of(18, 0), schedule.getWorkEndTime());
  }

  @Test
  void shouldThrowWhenScheduleDoesNotExist() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getSchedule("shop-1", "barber-9", "2026-04-10"));

    assertEquals("Schedule not found for barber barber-9 on 2026-04-10", exception.getMessage());
  }
}
