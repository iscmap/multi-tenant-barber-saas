package com.barbersaas.booking.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.barbersaas.booking.adapters.out.persistence.memory.InMemoryBookingRepository;
import com.barbersaas.booking.application.command.CreateBookingCommand;
import com.barbersaas.booking.application.factory.BookingEventFactory;
import com.barbersaas.booking.application.port.out.event.PublishBookingCreatedEventPort;
import com.barbersaas.booking.application.query.GetBookingQuery;
import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class BookingApplicationServiceTest {

  private final InMemoryBookingRepository repository = new InMemoryBookingRepository();
  private final PublishBookingCreatedEventPort publishBookingCreatedEventPort =
      mock(PublishBookingCreatedEventPort.class);
  private final BookingEventFactory bookingEventFactory = new BookingEventFactory();
  private final BookingApplicationService service =
      new BookingApplicationService(
          repository, repository, publishBookingCreatedEventPort, bookingEventFactory);

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

    assertTrue(booking.getBookingId() != null && !booking.getBookingId().isBlank());
    assertEquals("shop-1", booking.getShopId());
    assertEquals(BookingStatus.PENDING, booking.getStatus());
    assertNotNull(booking.getCreatedAt());
    verify(publishBookingCreatedEventPort).publish(any());
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
    assertNotNull(loadedBooking.getCreatedAt());
  }

  @Test
  void shouldThrowWhenBookingDoesNotExist() {
    GetBookingQuery query = GetBookingQuery.builder().bookingId("missing-id").build();

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> service.getBooking(query));

    assertEquals("Booking not found: missing-id", exception.getMessage());
  }

  @Test
  void shouldConfirmBookingState() {
    Booking booking =
        Booking.builder()
            .bookingId("booking-10")
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .status(BookingStatus.PENDING)
            .createdAt(java.time.LocalDateTime.of(2026, 4, 10, 10, 0))
            .build();

    Booking confirmedBooking = service.confirmBookingState(booking);

    assertEquals(BookingStatus.CONFIRMED, confirmedBooking.getStatus());
  }

  @Test
  void shouldRejectBookingState() {
    Booking booking =
        Booking.builder()
            .bookingId("booking-11")
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .status(BookingStatus.PENDING)
            .createdAt(java.time.LocalDateTime.of(2026, 4, 10, 10, 0))
            .build();

    Booking rejectedBooking = service.rejectBookingState(booking);

    assertEquals(BookingStatus.REJECTED, rejectedBooking.getStatus());
  }

  @Test
  void shouldFailInvalidConfirmTransition() {
    Booking booking =
        Booking.builder()
            .bookingId("booking-12")
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .status(BookingStatus.CONFIRMED)
            .createdAt(java.time.LocalDateTime.of(2026, 4, 10, 10, 0))
            .build();

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> service.confirmBookingState(booking));

    assertEquals(
        "Invalid booking status transition from CONFIRMED to CONFIRMED", exception.getMessage());
  }
}
