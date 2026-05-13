package com.barbersaas.booking.adapters.out.event.logging;

import com.barbersaas.booking.application.port.out.event.PublishBookingCreatedEventPort;
import com.barbersaas.shared.events.contract.BookingCreatedEvent;
import com.barbersaas.shared.events.envelope.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingBookingCreatedEventPublisher implements PublishBookingCreatedEventPort {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(LoggingBookingCreatedEventPublisher.class);

  @Override
  public void publish(EventEnvelope<BookingCreatedEvent> eventEnvelope) {
    LOGGER.info(
        "booking_created_event_published eventId={} eventType={} bookingId={} shopId={} correlationId={}",
        eventEnvelope.getEventId(),
        eventEnvelope.getEventType(),
        eventEnvelope.getPayload().getBookingId(),
        eventEnvelope.getPayload().getShopId(),
        eventEnvelope.getCorrelationId());
  }
}
