package com.barbersaas.booking.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.barbersaas.booking.application.command.CreateBookingCommand;
import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class BookingApplicationServiceTest {

  private final BookingApplicationService service = new BookingApplicationService();

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
  void shouldReturnBookingById() {
    Booking booking = service.getBooking("booking-123");

    assertEquals("booking-123", booking.getBookingId());
    assertEquals("shop-1", booking.getShopId());
    assertEquals(BookingStatus.PENDING, booking.getStatus());
  }
}
