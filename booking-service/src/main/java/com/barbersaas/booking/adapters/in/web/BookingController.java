package com.barbersaas.booking.adapters.in.web;

import com.barbersaas.booking.api.contract.CreateBookingRequest;
import com.barbersaas.booking.api.contract.CreateBookingResponse;
import com.barbersaas.booking.api.contract.GetBookingResponse;
import com.barbersaas.booking.application.port.in.CreateBookingUseCase;
import com.barbersaas.booking.application.port.in.GetBookingUseCase;
import com.barbersaas.booking.domain.model.Booking;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

  private final CreateBookingUseCase createBookingUseCase;
  private final GetBookingUseCase getBookingUseCase;

  public BookingController(
      CreateBookingUseCase createBookingUseCase, GetBookingUseCase getBookingUseCase) {
    this.createBookingUseCase = createBookingUseCase;
    this.getBookingUseCase = getBookingUseCase;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CreateBookingResponse createBooking(@Valid @RequestBody CreateBookingRequest request) {
    Booking booking = createBookingUseCase.createBooking(request);

    return CreateBookingResponse.builder()
        .bookingId(booking.getBookingId())
        .status(booking.getStatus())
        .build();
  }

  @GetMapping("/{bookingId}")
  public GetBookingResponse getBooking(@PathVariable String bookingId) {
    Booking booking = getBookingUseCase.getBooking(bookingId);

    return GetBookingResponse.builder()
        .bookingId(booking.getBookingId())
        .shopId(booking.getShopId())
        .barberId(booking.getBarberId())
        .customerId(booking.getCustomerId())
        .date(booking.getDate())
        .startTime(booking.getStartTime())
        .durationMinutes(booking.getDurationMinutes())
        .serviceCode(booking.getServiceCode())
        .status(booking.getStatus())
        .build();
  }
}
