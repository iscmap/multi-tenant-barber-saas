package com.barbersaas.booking.api.controller;

import com.barbersaas.booking.api.contract.CreateBookingRequest;
import com.barbersaas.booking.api.contract.CreateBookingResponse;
import com.barbersaas.booking.api.contract.GetBookingResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CreateBookingResponse createBooking(@Valid @RequestBody CreateBookingRequest request) {
    return CreateBookingResponse.builder().bookingId("temp-booking-id").status("PENDING").build();
  }

  @GetMapping("/{bookingId}")
  public GetBookingResponse getBooking(@PathVariable String bookingId) {
    return GetBookingResponse.builder()
        .bookingId(bookingId)
        .shopId("shop-1")
        .barberId("barber-id")
        .customerId("customer-id")
        .date(LocalDate.of(20026, 4, 10))
        .startTime(LocalTime.of(10, 0))
        .durationMinutes(30)
        .serviceCode("HAIRCUT")
        .status("PENDING")
        .build();
  }
}
