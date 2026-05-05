package com.barbersaas.booking.application.service;

import com.barbersaas.booking.application.command.CreateBookingCommand;
import com.barbersaas.booking.application.port.in.CreateBookingUseCase;
import com.barbersaas.booking.application.port.in.GetBookingUseCase;
import com.barbersaas.booking.application.port.out.LoadBookingPort;
import com.barbersaas.booking.application.port.out.SaveBookingPort;
import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import org.springframework.stereotype.Service;

@Service
public class BookingApplicationService implements CreateBookingUseCase, GetBookingUseCase {

  private final SaveBookingPort saveBookingPort;
  private final LoadBookingPort loadBookingPort;

  public BookingApplicationService(
      SaveBookingPort saveBookingPort, LoadBookingPort loadBookingPort) {
    this.saveBookingPort = saveBookingPort;
    this.loadBookingPort = loadBookingPort;
  }

  @Override
  public Booking createBooking(CreateBookingCommand command) {
    Booking booking =
        Booking.builder()
            .bookingId("temp-booking-id")
            .shopId(command.getShopId())
            .barberId(command.getBarberId())
            .customerId(command.getCustomerId())
            .date(command.getDate())
            .startTime(command.getStartTime())
            .durationMinutes(command.getDurationMinutes())
            .serviceCode(command.getServiceCode())
            .status(BookingStatus.PENDING)
            .build();
    return saveBookingPort.save(booking);
  }

  @Override
  public Booking getBooking(String bookingId) {
    return loadBookingPort
        .loadById(bookingId)
        .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
  }
}
