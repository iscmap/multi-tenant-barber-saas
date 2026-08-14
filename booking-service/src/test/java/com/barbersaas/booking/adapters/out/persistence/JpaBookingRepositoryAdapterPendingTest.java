package com.barbersaas.booking.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class JpaBookingRepositoryAdapterPendingTest {

  @Mock private SpringDataBookingRepository repository;

  private JpaBookingRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    adapter = new JpaBookingRepositoryAdapter(repository);
  }

  @Test
  void shouldLoadPendingBookings() {

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
            LocalDateTime.of(2026, 8, 13, 19, 0));

    when(repository.findByStatus("PENDING")).thenReturn(List.of(entity));

    List<Booking> result = adapter.loadPendingBookings();

    assertEquals(1, result.size());
    assertEquals(BookingStatus.PENDING, result.get(0).getStatus());
    assertEquals("booking-1", result.get(0).getBookingId());
  }
}
