package com.barbersaas.booking.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.barbersaas.booking.adapters.out.event.kafka.KafkaBookingCreatedEventPublisher;
import com.barbersaas.booking.application.command.CreateBookingCommand;
import com.barbersaas.booking.application.command.idempotency.CreateBookingWithIdempotencyCommand;
import com.barbersaas.booking.application.factory.BookingEventFactory;
import com.barbersaas.booking.application.port.out.LoadBookingPort;
import com.barbersaas.booking.application.port.out.SaveBookingPort;
import com.barbersaas.booking.application.port.out.event.PublishBookingCreatedEventPort;
import com.barbersaas.booking.application.port.out.idempotency.LoadIdempotencyRecordPort;
import com.barbersaas.booking.application.port.out.idempotency.SaveIdempotencyRecordPort;
import com.barbersaas.booking.application.query.GetBookingQuery;
import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import com.barbersaas.booking.domain.model.idempotency.IdempotencyRecord;
import com.barbersaas.booking.observability.metrics.BookingMetrics;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class BookingApplicationServiceTest {

  private SaveBookingPort saveBookingPort;
  private LoadBookingPort loadBookingPort;
  private LoadIdempotencyRecordPort loadIdempotencyRecordPort;
  private SaveIdempotencyRecordPort saveIdempotencyRecordPort;

  private PublishBookingCreatedEventPort publishBookingCreatedEventPort;
  private KafkaBookingCreatedEventPublisher kafkaBookingCreatedEventPublisher;

  private ObjectProvider<KafkaBookingCreatedEventPublisher> kafkaPublisherProvider;

  private BookingApplicationService service;

  private Map<String, Booking> bookingStorage;
  private Map<String, IdempotencyRecord> idempotencyStorage;
  private BookingMetrics bookingMetrics;
  ;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {

    saveBookingPort = mock(SaveBookingPort.class);
    loadBookingPort = mock(LoadBookingPort.class);

    loadIdempotencyRecordPort = mock(LoadIdempotencyRecordPort.class);

    saveIdempotencyRecordPort = mock(SaveIdempotencyRecordPort.class);

    publishBookingCreatedEventPort = mock(PublishBookingCreatedEventPort.class);

    kafkaBookingCreatedEventPublisher = mock(KafkaBookingCreatedEventPublisher.class);

    kafkaPublisherProvider = mock(ObjectProvider.class);

    bookingMetrics = mock(BookingMetrics.class);

    bookingStorage = new HashMap<>();
    idempotencyStorage = new HashMap<>();

    when(kafkaPublisherProvider.getIfAvailable()).thenReturn(kafkaBookingCreatedEventPublisher);

    when(saveBookingPort.save(any(Booking.class)))
        .thenAnswer(
            invocation -> {
              Booking booking = invocation.getArgument(0);

              String bookingId = booking.getBookingId();

              if (bookingId == null || bookingId.isBlank()) {
                bookingId = UUID.randomUUID().toString();
              }

              LocalDateTime createdAt =
                  booking.getCreatedAt() != null ? booking.getCreatedAt() : LocalDateTime.now();

              Booking savedBooking =
                  Booking.builder()
                      .bookingId(bookingId)
                      .shopId(booking.getShopId())
                      .barberId(booking.getBarberId())
                      .customerId(booking.getCustomerId())
                      .date(booking.getDate())
                      .startTime(booking.getStartTime())
                      .durationMinutes(booking.getDurationMinutes())
                      .serviceCode(booking.getServiceCode())
                      .status(booking.getStatus())
                      .createdAt(createdAt)
                      .build();

              bookingStorage.put(savedBooking.getBookingId(), savedBooking);

              return savedBooking;
            });

    when(loadBookingPort.loadById(any(String.class)))
        .thenAnswer(
            invocation -> Optional.ofNullable(bookingStorage.get(invocation.getArgument(0))));

    when(loadIdempotencyRecordPort.loadByKey(any(String.class)))
        .thenAnswer(
            invocation -> Optional.ofNullable(idempotencyStorage.get(invocation.getArgument(0))));

    when(saveIdempotencyRecordPort.save(any(IdempotencyRecord.class)))
        .thenAnswer(
            invocation -> {
              IdempotencyRecord record = invocation.getArgument(0);

              idempotencyStorage.put(record.getIdempotencyKey(), record);

              return record;
            });

    service =
        new BookingApplicationService(
            saveBookingPort,
            loadBookingPort,
            publishBookingCreatedEventPort,
            new BookingEventFactory(),
            loadIdempotencyRecordPort,
            saveIdempotencyRecordPort,
            kafkaPublisherProvider,
            bookingMetrics);
  }

  @Test
  void shouldCreateBookingFromCommand() {

    CreateBookingCommand createBookingCommand = createBookingCommand();

    CreateBookingWithIdempotencyCommand command =
        CreateBookingWithIdempotencyCommand.builder()
            .idempotencyKey("idem-1")
            .createBookingCommand(createBookingCommand)
            .build();

    Booking booking = service.createBooking(command);

    assertTrue(booking.getBookingId() != null && !booking.getBookingId().isBlank());

    assertEquals("shop-1", booking.getShopId());

    assertEquals(BookingStatus.PENDING, booking.getStatus());

    assertNotNull(booking.getCreatedAt());

    verify(publishBookingCreatedEventPort).publish(any());

    verify(kafkaBookingCreatedEventPublisher).publish(any());
  }

  @Test
  void shouldReturnSameBookingForSameIdempotencyKey() {

    CreateBookingWithIdempotencyCommand command =
        CreateBookingWithIdempotencyCommand.builder()
            .idempotencyKey("idem-same")
            .createBookingCommand(createBookingCommand())
            .build();

    Booking firstBooking = service.createBooking(command);

    Booking secondBooking = service.createBooking(command);

    assertEquals(firstBooking.getBookingId(), secondBooking.getBookingId());

    verify(publishBookingCreatedEventPort, times(1)).publish(any());

    verify(kafkaBookingCreatedEventPublisher, times(1)).publish(any());
  }

  @Test
  void shouldReturnSavedBookingById() {

    CreateBookingWithIdempotencyCommand command =
        CreateBookingWithIdempotencyCommand.builder()
            .idempotencyKey("idem-2")
            .createBookingCommand(createBookingCommand())
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

    Booking booking = bookingWithStatus("booking-10", BookingStatus.PENDING);

    Booking confirmedBooking = service.confirmBookingState(booking);

    assertEquals(BookingStatus.CONFIRMED, confirmedBooking.getStatus());
  }

  @Test
  void shouldRejectBookingState() {

    Booking booking = bookingWithStatus("booking-11", BookingStatus.PENDING);

    Booking rejectedBooking = service.rejectBookingState(booking);

    assertEquals(BookingStatus.REJECTED, rejectedBooking.getStatus());
  }

  @Test
  void shouldFailInvalidConfirmTransition() {

    Booking booking = bookingWithStatus("booking-12", BookingStatus.CONFIRMED);

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> service.confirmBookingState(booking));

    assertEquals(
        "Invalid booking status transition from CONFIRMED to CONFIRMED", exception.getMessage());
  }

  private CreateBookingCommand createBookingCommand() {

    return CreateBookingCommand.builder()
        .shopId("shop-1")
        .barberId("barber-1")
        .customerId("customer-1")
        .date(LocalDate.of(2026, 4, 10))
        .startTime(LocalTime.of(10, 0))
        .durationMinutes(30)
        .serviceCode("HAIRCUT")
        .build();
  }

  private Booking bookingWithStatus(String bookingId, BookingStatus status) {

    return Booking.builder()
        .bookingId(bookingId)
        .shopId("shop-1")
        .barberId("barber-1")
        .customerId("customer-1")
        .date(LocalDate.of(2026, 4, 10))
        .startTime(LocalTime.of(10, 0))
        .durationMinutes(30)
        .serviceCode("HAIRCUT")
        .status(status)
        .createdAt(LocalDateTime.of(2026, 4, 10, 10, 0))
        .build();
  }
}
