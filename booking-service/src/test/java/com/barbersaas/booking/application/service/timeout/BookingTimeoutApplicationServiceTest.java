package com.barbersaas.booking.application.service.timeout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.barbersaas.booking.adapters.out.persistence.memory.InMemoryBookingRepository;
import com.barbersaas.booking.application.command.timeout.RejectTimedOutBookingsCommand;
import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class BookingTimeoutApplicationServiceTest {

  private final InMemoryBookingRepository repository = new InMemoryBookingRepository();
  private final BookingTimeoutApplicationService service =
      new BookingTimeoutApplicationService(repository, repository);

  @Test
  void shouldRejectTimedOutPendingBookings() {
    Booking oldPendingBooking =
        Booking.builder()
            .bookingId("booking-old")
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .status(BookingStatus.PENDING)
            .createdAt(LocalDateTime.of(2026, 4, 10, 10, 0))
            .build();

    Booking freshPendingBooking =
        Booking.builder()
            .bookingId("booking-fresh")
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 10))
            .durationMinutes(30)
            .serviceCode("BEARD")
            .status(BookingStatus.PENDING)
            .createdAt(LocalDateTime.of(2026, 4, 10, 10, 4))
            .build();

    repository.save(oldPendingBooking);
    repository.save(freshPendingBooking);

    RejectTimedOutBookingsCommand command =
        RejectTimedOutBookingsCommand.builder()
            .now(OffsetDateTime.of(2026, 4, 10, 10, 6, 0, 0, ZoneOffset.UTC))
            .build();

    int rejectedCount = service.rejectTimedOutBookings(command);

    Booking updatedOldBooking = repository.loadById("booking-old").orElseThrow();
    Booking updatedFreshBooking = repository.loadById("booking-fresh").orElseThrow();

    assertEquals(1, rejectedCount);
    assertEquals(BookingStatus.REJECTED, updatedOldBooking.getStatus());
    assertEquals(BookingStatus.PENDING, updatedFreshBooking.getStatus());
  }
}
