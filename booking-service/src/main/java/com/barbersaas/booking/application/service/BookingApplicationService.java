package com.barbersaas.booking.application.service;

import com.barbersaas.booking.adapters.out.event.kafka.KafkaBookingCreatedEventPublisher;
import com.barbersaas.booking.application.command.CreateBookingCommand;
import com.barbersaas.booking.application.command.idempotency.CreateBookingWithIdempotencyCommand;
import com.barbersaas.booking.application.factory.BookingEventFactory;
import com.barbersaas.booking.application.port.in.CreateBookingUseCase;
import com.barbersaas.booking.application.port.in.GetBookingUseCase;
import com.barbersaas.booking.application.port.out.LoadBookingPort;
import com.barbersaas.booking.application.port.out.SaveBookingPort;
import com.barbersaas.booking.application.port.out.event.PublishBookingCreatedEventPort;
import com.barbersaas.booking.application.port.out.idempotency.LoadIdempotencyRecordPort;
import com.barbersaas.booking.application.port.out.idempotency.SaveIdempotencyRecordPort;
import com.barbersaas.booking.application.query.GetBookingQuery;
import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import com.barbersaas.booking.domain.model.idempotency.IdempotencyRecord;
import com.barbersaas.booking.domain.rule.BookingStateTransitions;
import com.barbersaas.shared.events.contract.BookingCreatedEvent;
import com.barbersaas.shared.events.envelope.EventEnvelope;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class BookingApplicationService implements CreateBookingUseCase, GetBookingUseCase {

  private final SaveBookingPort saveBookingPort;
  private final LoadBookingPort loadBookingPort;
  private final PublishBookingCreatedEventPort publishBookingCreatedEventPort;
  private final BookingEventFactory bookingEventFactory;
  private final LoadIdempotencyRecordPort loadIdempotencyRecordPort;
  private final SaveIdempotencyRecordPort saveIdempotencyRecordPort;
  private final ObjectProvider<KafkaBookingCreatedEventPublisher>
      kafkaBookingCreatedEventPublisherProvider;

  public BookingApplicationService(
      SaveBookingPort saveBookingPort,
      LoadBookingPort loadBookingPort,
      PublishBookingCreatedEventPort publishBookingCreatedEventPort,
      BookingEventFactory bookingEventFactory,
      LoadIdempotencyRecordPort loadIdempotencyRecordPort,
      SaveIdempotencyRecordPort saveIdempotencyRecordPort,
      ObjectProvider<KafkaBookingCreatedEventPublisher> kafkaBookingCreatedEventPublisherProvider) {
    this.saveBookingPort = saveBookingPort;
    this.loadBookingPort = loadBookingPort;
    this.publishBookingCreatedEventPort = publishBookingCreatedEventPort;
    this.bookingEventFactory = bookingEventFactory;
    this.loadIdempotencyRecordPort = loadIdempotencyRecordPort;
    this.saveIdempotencyRecordPort = saveIdempotencyRecordPort;
    this.kafkaBookingCreatedEventPublisherProvider = kafkaBookingCreatedEventPublisherProvider;
  }

  @Override
  public Booking createBooking(CreateBookingWithIdempotencyCommand command) {

    IdempotencyRecord existingRecord =
        loadIdempotencyRecordPort.loadByKey(command.getIdempotencyKey()).orElse(null);

    if (existingRecord != null) {
      return loadBookingPort
          .loadById(existingRecord.getBookingId())
          .orElseThrow(
              () ->
                  new IllegalStateException(
                      "Idempotency record exists but booking was not found: "
                          + existingRecord.getBookingId()));
    }

    CreateBookingCommand createBookingCommand = command.getCreateBookingCommand();

    Booking booking =
        Booking.builder()
            .bookingId(UUID.randomUUID().toString())
            .shopId(createBookingCommand.getShopId())
            .barberId(createBookingCommand.getBarberId())
            .customerId(createBookingCommand.getCustomerId())
            .date(createBookingCommand.getDate())
            .startTime(createBookingCommand.getStartTime())
            .durationMinutes(createBookingCommand.getDurationMinutes())
            .serviceCode(createBookingCommand.getServiceCode())
            .status(BookingStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

    Booking savedBooking = saveBookingPort.save(booking);

    IdempotencyRecord record =
        IdempotencyRecord.builder()
            .idempotencyKey(command.getIdempotencyKey())
            .bookingId(savedBooking.getBookingId())
            .createdAt(LocalDateTime.now())
            .build();

    saveIdempotencyRecordPort.save(record);

    EventEnvelope<BookingCreatedEvent> eventEnvelope =
        bookingEventFactory.buildBookingCreatedEvent(savedBooking);

    publishBookingCreatedEventPort.publish(eventEnvelope);

    KafkaBookingCreatedEventPublisher kafkaPublisher =
        kafkaBookingCreatedEventPublisherProvider.getIfAvailable();
    if (kafkaPublisher != null) {
      kafkaPublisher.publish(eventEnvelope);
    }

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
