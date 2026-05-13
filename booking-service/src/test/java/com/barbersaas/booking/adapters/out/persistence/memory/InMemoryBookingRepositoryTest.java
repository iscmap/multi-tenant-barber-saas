package com.barbersaas.booking.adapters.out.persistence.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

public class InMemoryBookingRepositoryTest {

  private final InMemoryBookingRepository repository = new InMemoryBookingRepository();

  @Test
  void shouldGenerateBookingIdAndSaveBooking() {
    Booking booking =
        Booking.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .status(BookingStatus.PENDING)
            .build();

    Booking savedBooking = repository.save(booking);

    assertTrue(savedBooking.getBookingId() != null && !savedBooking.getBookingId().isBlank());
    assertEquals("shop-1", savedBooking.getShopId());
    assertEquals(BookingStatus.PENDING, savedBooking.getStatus());
  }

  @Test
  void shouldLoadSavedBookingById() {
    Booking booking =
        Booking.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .status(BookingStatus.PENDING)
            .build();

    Booking savedBooking = repository.save(booking);

    assertTrue(repository.loadById(savedBooking.getBookingId()).isPresent());
    assertEquals(
        savedBooking.getBookingId(),
        repository.loadById(savedBooking.getBookingId()).get().getBookingId());
  }
}
