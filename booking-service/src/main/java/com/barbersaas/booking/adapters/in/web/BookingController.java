package com.barbersaas.booking.adapters.in.web;

import com.barbersaas.booking.api.contract.CreateBookingRequest;
import com.barbersaas.booking.api.contract.CreateBookingResponse;
import com.barbersaas.booking.api.contract.GetBookingResponse;
import com.barbersaas.booking.application.command.CreateBookingCommand;
import com.barbersaas.booking.application.command.idempotency.CreateBookingWithIdempotencyCommand;
import com.barbersaas.booking.application.command.timeout.RejectTimedOutBookingsCommand;
import com.barbersaas.booking.application.mapper.BookingApiMapper;
import com.barbersaas.booking.application.port.in.CreateBookingUseCase;
import com.barbersaas.booking.application.port.in.GetBookingUseCase;
import com.barbersaas.booking.application.port.in.timeout.RejectTimedOutBookingsUseCase;
import com.barbersaas.booking.application.query.GetBookingQuery;
import com.barbersaas.booking.domain.model.Booking;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Validated
public class BookingController {

  private final CreateBookingUseCase createBookingUseCase;
  private final GetBookingUseCase getBookingUseCase;
  private final RejectTimedOutBookingsUseCase rejectTimedOutBookingsUseCase;
  private final BookingApiMapper bookingApiMapper;

  public BookingController(
      CreateBookingUseCase createBookingUseCase,
      GetBookingUseCase getBookingUseCase,
      RejectTimedOutBookingsUseCase rejectTimedOutBookingsUseCase,
      BookingApiMapper bookingApiMapper) {
    this.createBookingUseCase = createBookingUseCase;
    this.getBookingUseCase = getBookingUseCase;
    this.rejectTimedOutBookingsUseCase = rejectTimedOutBookingsUseCase;
    this.bookingApiMapper = bookingApiMapper;
  }

  @PostMapping("/bookings")
  @ResponseStatus(HttpStatus.CREATED)
  public CreateBookingResponse createBooking(
      @RequestHeader(value = "Idempotency-Key", required = true)
          @Size(min = 1, max = 128)
          @Pattern(regexp = "^[A-Za-z0-9._:-]+$")
          String idempotencyKey,
      @Valid @RequestBody CreateBookingRequest request) {
    CreateBookingCommand createBookingCommand = bookingApiMapper.toCommand(request);

    CreateBookingWithIdempotencyCommand command =
        CreateBookingWithIdempotencyCommand.builder()
            .idempotencyKey(idempotencyKey)
            .createBookingCommand(createBookingCommand)
            .build();

    Booking createdBooking = createBookingUseCase.createBooking(command);
    return bookingApiMapper.toCreateBookingResponse(createdBooking);
  }

  @GetMapping("/bookings/{bookingId}")
  public GetBookingResponse getBooking(
      @PathVariable @Size(min = 1, max = 64) @Pattern(regexp = "^[A-Za-z0-9_-]+$")
          String bookingId) {
    GetBookingQuery query = GetBookingQuery.builder().bookingId(bookingId).build();

    Booking booking = getBookingUseCase.getBooking(query);
    return bookingApiMapper.toGetBookingResponse(booking);
  }

  @PostMapping("/internal/bookings/reject-timeouts")
  public Map<String, Object> rejectTimedOutBookings() {
    RejectTimedOutBookingsCommand command =
        RejectTimedOutBookingsCommand.builder().now(OffsetDateTime.now()).build();

    int rejectedCount = rejectTimedOutBookingsUseCase.rejectTimedOutBookings(command);

    return Map.of("rejectedCount", rejectedCount, "processedAt", command.getNow().toString());
  }
}
