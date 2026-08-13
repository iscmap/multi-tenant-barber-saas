package com.barbersaas.availability.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.barbersaas.availability.domain.enums.AvailabilityStatus;
import com.barbersaas.availability.domain.model.BarberAvailability;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JpaBarberAvailabilityRepositoryAdapterTest {

  @Mock private SpringDataBarberAvailabilityRepository repository;

  private JpaBarberAvailabilityRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    adapter = new JpaBarberAvailabilityRepositoryAdapter(repository);
  }

  @Test
  void shouldLoadAvailabilityFromRepository() {

    BarberAvailabilityJpaEntity entity =
        new BarberAvailabilityJpaEntity(
            "shop-1", "barber-1", LocalDate.of(2026, 4, 10), LocalTime.of(10, 0), 30, "AVAILABLE");

    when(repository.findByShopIdAndBarberIdAndDateAndStartTime(
            "shop-1", "barber-1", LocalDate.of(2026, 4, 10), LocalTime.of(10, 0)))
        .thenReturn(Optional.of(entity));

    BarberAvailability result =
        adapter
            .loadByBarberAndSlot(
                "shop-1", "barber-1", LocalDate.of(2026, 4, 10), LocalTime.of(10, 0))
            .orElseThrow();

    assertEquals("shop-1", result.getShopId());
    assertEquals("barber-1", result.getBarberId());
    assertEquals(LocalDate.of(2026, 4, 10), result.getDate());
    assertEquals(LocalTime.of(10, 0), result.getStartTime());
    assertEquals(30, result.getDurationMinutes());
    assertEquals(AvailabilityStatus.AVAILABLE, result.getStatus());

    verify(repository)
        .findByShopIdAndBarberIdAndDateAndStartTime(
            "shop-1", "barber-1", LocalDate.of(2026, 4, 10), LocalTime.of(10, 0));
  }

  @Test
  void shouldReserveAvailability() {

    BarberAvailability availability =
        BarberAvailability.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .status(AvailabilityStatus.AVAILABLE)
            .build();

    BarberAvailabilityJpaEntity savedEntity =
        new BarberAvailabilityJpaEntity(
            "shop-1", "barber-1", LocalDate.of(2026, 4, 10), LocalTime.of(10, 0), 30, "RESERVED");

    when(repository.save(any(BarberAvailabilityJpaEntity.class))).thenReturn(savedEntity);

    BarberAvailability result = adapter.reserve(availability);

    assertEquals(AvailabilityStatus.RESERVED, result.getStatus());

    verify(repository).save(any(BarberAvailabilityJpaEntity.class));
  }
}
