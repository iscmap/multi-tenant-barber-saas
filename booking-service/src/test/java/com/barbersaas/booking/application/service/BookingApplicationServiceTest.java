package com.barbersaas.booking.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.barbersaas.booking.adapters.out.persistence.memory.InMemoryBookingRepository;
import com.barbersaas.booking.application.command.CreateBookingCommand;
import com.barbersaas.booking.application.query.GetBookingQuery;
import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class BookingApplicationServiceTest {

  private final InMemoryBookingRepository repository = new InMemoryBookingRepository();
  private final BookingApplicationService service =
      new BookingApplicationService(repository, repository);

  @Test
  void shouldCreateBookingFromCommand() {
    CreateBookingCommand command =
        CreateBookingCommand.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .build();

    Booking booking = service.createBooking(command);

    assertEquals("temp-booking-id", booking.getBookingId());
    assertEquals("shop-1", booking.getShopId());
    assertEquals(BookingStatus.PENDING, booking.getStatus());
  }

  @Test
  void shouldReturnSavedBookingById() {
    CreateBookingCommand command =
        CreateBookingCommand.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .build();

    Booking createdBooking = service.createBooking(command);

    GetBookingQuery query =
        GetBookingQuery.builder().bookingId(createdBooking.getBookingId()).build();

    Booking loadedBooking = service.getBooking(query);

    assertEquals(createdBooking.getBookingId(), loadedBooking.getBookingId());
    assertEquals("shop-1", loadedBooking.getShopId());
    assertEquals(BookingStatus.PENDING, loadedBooking.getStatus());
  }

  @Test
  void shouldThrowWhenBookingDoesNotExist() {

    GetBookingQuery query = GetBookingQuery.builder().bookingId("missing-id").build();

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> service.getBooking(query));

    assertEquals("Booking not found: missing-id", exception.getMessage());
  }
}
