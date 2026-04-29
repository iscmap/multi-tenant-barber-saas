package com.barbersaas.booking.application.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.barbersaas.booking.api.contract.CreateBookingRequest;
import com.barbersaas.booking.api.contract.CreateBookingResponse;
import com.barbersaas.booking.api.contract.GetBookingResponse;
import com.barbersaas.booking.application.command.CreateBookingCommand;
import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class BookingApiMapperTest {

  private final BookingApiMapper mapper = new BookingApiMapper();

  @Test
  void shouldMapCreateRequestToCommand() {
    CreateBookingRequest request =
        CreateBookingRequest.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .build();

    CreateBookingCommand command = mapper.toCommand(request);

    assertEquals("shop-1", command.getShopId());
    assertEquals("barber-1", command.getBarberId());
    assertEquals("customer-1", command.getCustomerId());
    assertEquals(30, command.getDurationMinutes());
    assertEquals("HAIRCUT", command.getServiceCode());
  }

  @Test
  void shouldMapDomainToCreateResponse() {
    Booking booking =
        Booking.builder().bookingId("booking-123").status(BookingStatus.PENDING).build();

    CreateBookingResponse response = mapper.toCreateBookingResponse(booking);

    assertEquals("booking-123", response.getBookingId());
    assertEquals("PENDING", response.getStatus());
  }

  @Test
  void shouldMapDomainToGetResponse() {
    Booking booking =
        Booking.builder()
            .bookingId("booking-123")
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .status(BookingStatus.PENDING)
            .build();

    GetBookingResponse response = mapper.toGetBookingResponse(booking);

    assertEquals("booking-123", response.getBookingId());
    assertEquals("shop-1", response.getShopId());
    assertEquals("PENDING", response.getStatus());
  }
}
