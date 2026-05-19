package com.barbersaas.availability.adapters.out.event.logging;

import com.barbersaas.availability.application.port.out.event.PublishAvailabilityDecidedEventPort;
import com.barbersaas.shared.events.contract.AvailabilityDecidedEvent;
import com.barbersaas.shared.events.envelope.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingAvailabilityDecidedEventPublisher
    implements PublishAvailabilityDecidedEventPort {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(LoggingAvailabilityDecidedEventPublisher.class);

  @Override
  public void publish(EventEnvelope<AvailabilityDecidedEvent> eventEnvelope) {
    LOGGER.info(
        "availability_decided_event_published eventId={} eventType={} bookingId={} shopId={} decision={} reason={} correlationId={}",
        eventEnvelope.getEventId(),
        eventEnvelope.getEventType(),
        eventEnvelope.getPayload().getBookingId(),
        eventEnvelope.getPayload().getShopId(),
        eventEnvelope.getPayload().getDecision(),
        eventEnvelope.getPayload().getReason(),
        eventEnvelope.getCorrelationId());
  }
}
