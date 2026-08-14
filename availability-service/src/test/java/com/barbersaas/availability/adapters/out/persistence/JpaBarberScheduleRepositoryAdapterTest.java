package com.barbersaas.availability.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.barbersaas.availability.domain.model.BarberSchedule;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class JpaBarberScheduleRepositoryAdapterTest {

  @Mock private SpringDataBarberScheduleRepository repository;

  private JpaBarberScheduleRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    adapter = new JpaBarberScheduleRepositoryAdapter(repository);
  }

  @Test
  void shouldLoadBarberSchedule() {

    LocalDate date = LocalDate.of(2026, 4, 10);

    BarberScheduleJpaEntity entity =
        new BarberScheduleJpaEntity(
            "shop-1", "barber-1", date, LocalTime.of(10, 0), LocalTime.of(18, 0), 30);

    when(repository.findByShopIdAndBarberIdAndDate("shop-1", "barber-1", date))
        .thenReturn(Optional.of(entity));

    Optional<BarberSchedule> result = adapter.loadByBarberAndDate("shop-1", "barber-1", date);

    assertTrue(result.isPresent());

    assertEquals("shop-1", result.get().getShopId());

    assertEquals("barber-1", result.get().getBarberId());

    assertEquals(LocalTime.of(10, 0), result.get().getWorkStartTime());

    assertEquals(LocalTime.of(18, 0), result.get().getWorkEndTime());

    assertEquals(30, result.get().getSlotDurationMinutes());
  }
}
