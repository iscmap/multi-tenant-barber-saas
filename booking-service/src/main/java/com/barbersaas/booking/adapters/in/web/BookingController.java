package com.barbersaas.booking.adapters.in.web;

import com.barbersaas.booking.api.contract.CreateBookingRequest;
import com.barbersaas.booking.api.contract.CreateBookingResponse;
import com.barbersaas.booking.api.contract.GetBookingResponse;
import com.barbersaas.booking.application.command.CreateBookingCommand;
import com.barbersaas.booking.application.mapper.BookingApiMapper;
import com.barbersaas.booking.application.port.in.CreateBookingUseCase;
import com.barbersaas.booking.application.port.in.GetBookingUseCase;
import com.barbersaas.booking.domain.model.Booking;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

  private final CreateBookingUseCase createBookingUseCase;
  private final GetBookingUseCase getBookingUseCase;
  private final BookingApiMapper bookingApiMapper;

  public BookingController(
      CreateBookingUseCase createBookingUseCase,
      GetBookingUseCase getBookingUseCase,
      BookingApiMapper bookingApiMapper) {
    this.createBookingUseCase = createBookingUseCase;
    this.getBookingUseCase = getBookingUseCase;
    this.bookingApiMapper = bookingApiMapper;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CreateBookingResponse createBooking(@Valid @RequestBody CreateBookingRequest request) {
    CreateBookingCommand command = bookingApiMapper.toCommand(request);
    Booking createdBooking = createBookingUseCase.createBooking(command);
    return bookingApiMapper.toCreateBookingResponse(createdBooking);
  }

  @GetMapping("/{bookingId}")
  public GetBookingResponse getBooking(@PathVariable String bookingId) {
    Booking booking = getBookingUseCase.getBooking(bookingId);
    return bookingApiMapper.toGetBookingResponse(booking);
  }
}
