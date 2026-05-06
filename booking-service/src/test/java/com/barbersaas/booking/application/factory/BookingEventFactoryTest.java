package com.barbersaas.booking.application.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import com.barbersaas.shared.events.contract.BookingCreatedEvent;
import com.barbersaas.shared.events.envelope.EventEnvelope;
import com.barbersaas.shared.logging.CorrelationIdHolder;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class BookingEventFactoryTest {

  private final BookingEventFactory factory = new BookingEventFactory();

  @AfterEach
  void tearDown() {
    CorrelationIdHolder.clear();
  }

  @Test
  void shouldBuildBookingCreatedEventEnvelope() {
    CorrelationIdHolder.set("corr-500");

    Booking booking =
        Booking.builder()
            .bookingId("booking-1")
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .status(BookingStatus.PENDING)
            .build();

    EventEnvelope<BookingCreatedEvent> envelope = factory.buildBookingCreatedEvent(booking);

    assertNotNull(envelope.getEventId());
    assertEquals("BookingCreated", envelope.getEventType());
    assertEquals("corr-500", envelope.getCorrelationId());
    assertEquals("booking-service", envelope.getSource());
    assertEquals("shop-1", envelope.getTenantId());
    assertEquals("booking-1", envelope.getPayload().getBookingId());
    assertEquals("PENDING", envelope.getPayload().getStatus());
  }
}
