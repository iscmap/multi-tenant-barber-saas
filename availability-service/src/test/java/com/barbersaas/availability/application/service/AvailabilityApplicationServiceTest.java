package com.barbersaas.availability.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.barbersaas.availability.adapters.out.persistence.memory.InMemoryBarberAvailabilityRepository;
import com.barbersaas.availability.domain.enums.AvailabilityStatus;
import com.barbersaas.availability.domain.model.BarberAvailability;
import org.junit.jupiter.api.Test;

class AvailabilityApplicationServiceTest {

  private final InMemoryBarberAvailabilityRepository repository =
      new InMemoryBarberAvailabilityRepository();
  private final AvailabilityApplicationService service =
      new AvailabilityApplicationService(repository);

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
}
