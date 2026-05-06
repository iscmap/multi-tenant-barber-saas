package com.barbersaas.booking.application.service;

import com.barbersaas.booking.application.command.CreateBookingCommand;
import com.barbersaas.booking.application.factory.BookingEventFactory;
import com.barbersaas.booking.application.port.in.CreateBookingUseCase;
import com.barbersaas.booking.application.port.in.GetBookingUseCase;
import com.barbersaas.booking.application.port.out.LoadBookingPort;
import com.barbersaas.booking.application.port.out.SaveBookingPort;
import com.barbersaas.booking.application.port.out.event.PublishBookingCreatedEventPort;
import com.barbersaas.booking.application.query.GetBookingQuery;
import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import com.barbersaas.booking.domain.rule.BookingStateTransitions;
import com.barbersaas.shared.events.contract.BookingCreatedEvent;
import com.barbersaas.shared.events.envelope.EventEnvelope;
import org.springframework.stereotype.Service;

@Service
public class BookingApplicationService implements CreateBookingUseCase, GetBookingUseCase {

  private final SaveBookingPort saveBookingPort;
  private final LoadBookingPort loadBookingPort;
  private final PublishBookingCreatedEventPort publishBookingCreatedEventPort;
  private final BookingEventFactory bookingEventFactory;

  public BookingApplicationService(
      SaveBookingPort saveBookingPort,
      LoadBookingPort loadBookingPort,
      PublishBookingCreatedEventPort publishBookingCreatedEventPort,
      BookingEventFactory bookingEventFactory) {
    this.saveBookingPort = saveBookingPort;
    this.loadBookingPort = loadBookingPort;
    this.publishBookingCreatedEventPort = publishBookingCreatedEventPort;
    this.bookingEventFactory = bookingEventFactory;
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
    Booking savedBooking = saveBookingPort.save(booking);

    EventEnvelope<BookingCreatedEvent> eventEnvelope =
        bookingEventFactory.buildBookingCreatedEvent(savedBooking);

    publishBookingCreatedEventPort.publish(eventEnvelope);

    return savedBooking;
  }

  @Override
  public Booking getBooking(GetBookingQuery query) {
    return loadBookingPort
        .loadById(query.getBookingId())
        .orElseThrow(
            () -> new IllegalArgumentException("Booking not found: " + query.getBookingId()));
  }

  public Booking confirmBookingState(Booking booking) {
    return BookingStateTransitions.confirm(booking);
  }

  public Booking rejectBookingState(Booking booking) {
    return BookingStateTransitions.reject(booking);
  }
}
