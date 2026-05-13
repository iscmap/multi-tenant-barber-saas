package com.barbersaas.availability.adapters.out.persistence.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.barbersaas.availability.domain.enums.AvailabilityStatus;
import com.barbersaas.availability.domain.model.BarberAvailability;
import com.barbersaas.availability.domain.model.BarberSchedule;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class InMemoryBarberAvailabilityRepositoryTest {

  private final InMemoryBarberAvailabilityRepository repository =
      new InMemoryBarberAvailabilityRepository();

  @Test
  void shouldLoadSeededAvailability() {
    BarberAvailability availability =
        repository
            .loadByBarberAndSlot(
                "shop-1", "barber-1", LocalDate.of(2026, 4, 10), LocalTime.of(10, 0))
            .orElseThrow();

    assertEquals("shop-1", availability.getShopId());
    assertEquals(AvailabilityStatus.AVAILABLE, availability.getStatus());
  }

  @Test
  void shouldReturnEmptyWhenSlotDoesNotExist() {
    assertTrue(
        repository
            .loadByBarberAndSlot(
                "shop-1", "barber-x", LocalDate.of(2026, 4, 10), LocalTime.of(10, 0))
            .isEmpty());
  }

  @Test
  void shouldLoadSeededSchedule() {
    BarberSchedule schedule =
        repository
            .loadByBarberAndDate("shop-1", "barber-1", LocalDate.of(2026, 4, 10))
            .orElseThrow();

    assertEquals("shop-1", schedule.getShopId());
    assertEquals(LocalTime.of(10, 0), schedule.getWorkStartTime());
    assertEquals(LocalTime.of(18, 0), schedule.getWorkEndTime());
    assertEquals(30, schedule.getSlotDurationMinutes());
  }

  @Test
  void shouldReturnEmptyWhenScheduleDoesNotExist() {
    assertTrue(
        repository.loadByBarberAndDate("shop-1", "barber-x", LocalDate.of(2026, 4, 10)).isEmpty());
  }

  @Test
  void shouldReserveAvailabilitySlot() {
    BarberAvailability availability =
        repository
            .loadByBarberAndSlot(
                "shop-1", "barber-1", LocalDate.of(2026, 4, 10), LocalTime.of(10, 0))
            .orElseThrow();

    BarberAvailability reservedAvailability = repository.reserve(availability);

    assertEquals(AvailabilityStatus.RESERVED, reservedAvailability.getStatus());
  }
}
