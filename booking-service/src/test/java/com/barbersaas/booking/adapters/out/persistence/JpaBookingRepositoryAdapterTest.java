package com.barbersaas.booking.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JpaBookingRepositoryAdapterTest {

  @Mock private SpringDataBookingRepository repository;

  private JpaBookingRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    adapter = new JpaBookingRepositoryAdapter(repository);
  }

  @Test
  void shouldLoadBookingFromRepository() {
    LocalDateTime createdAt = LocalDateTime.of(2026, 8, 11, 18, 0);

    BookingJpaEntity entity =
        new BookingJpaEntity(
            "booking-1",
            "shop-1",
            "barber-1",
            "customer-1",
            LocalDate.of(2026, 8, 20),
            LocalTime.of(10, 0),
            30,
            "HAIRCUT",
            "PENDING",
            createdAt);

    when(repository.findById("booking-1")).thenReturn(Optional.of(entity));

    Booking result = adapter.loadById("booking-1").orElseThrow();

    assertEquals("booking-1", result.getBookingId());
    assertEquals("shop-1", result.getShopId());
    assertEquals("barber-1", result.getBarberId());
    assertEquals("customer-1", result.getCustomerId());
    assertEquals(LocalDate.of(2026, 8, 20), result.getDate());
    assertEquals(LocalTime.of(10, 0), result.getStartTime());
    assertEquals(30, result.getDurationMinutes());
    assertEquals("HAIRCUT", result.getServiceCode());
    assertEquals(BookingStatus.PENDING, result.getStatus());
    assertEquals(createdAt, result.getCreatedAt());

    verify(repository).findById("booking-1");
  }
}
