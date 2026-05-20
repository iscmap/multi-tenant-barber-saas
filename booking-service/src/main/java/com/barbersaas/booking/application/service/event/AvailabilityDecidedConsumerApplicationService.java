package com.barbersaas.booking.application.service.event;

import com.barbersaas.booking.application.port.in.event.ConsumeAvailabilityDecidedUseCase;
import com.barbersaas.booking.application.port.out.LoadBookingPort;
import com.barbersaas.booking.application.port.out.SaveBookingPort;
import com.barbersaas.booking.domain.model.Booking;
import com.barbersaas.booking.domain.rule.BookingStateTransitions;
import com.barbersaas.shared.events.contract.AvailabilityDecidedEvent;
import com.barbersaas.shared.events.envelope.EventEnvelope;
import com.barbersaas.shared.events.parser.EventJsonParser;
import com.barbersaas.shared.events.parser.JacksonEventJsonParser;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityDecidedConsumerApplicationService
    implements ConsumeAvailabilityDecidedUseCase {

  private final LoadBookingPort loadBookingPort;
  private final SaveBookingPort saveBookingPort;
  private final EventJsonParser eventJsonParser;

  public AvailabilityDecidedConsumerApplicationService(
      LoadBookingPort loadBookingPort, SaveBookingPort saveBookingPort) {
    this.loadBookingPort = loadBookingPort;
    this.saveBookingPort = saveBookingPort;
    this.eventJsonParser = new JacksonEventJsonParser();
  }

  @Override
  public void consume(String payload) {
    EventEnvelope<AvailabilityDecidedEvent> envelope =
        eventJsonParser.parseEventEnvelope(payload, AvailabilityDecidedEvent.class);

    AvailabilityDecidedEvent event = envelope.getPayload();

    Booking booking =
        loadBookingPort
            .loadById(event.getBookingId())
            .orElseThrow(
                () -> new IllegalArgumentException("Booking not found: " + event.getBookingId()));

    Booking updatedBooking;
    if ("CONFIRMED".equals(event.getDecision())) {
      updatedBooking = BookingStateTransitions.confirm(booking);
    } else if ("REJECTED".equals(event.getDecision())) {
      updatedBooking = BookingStateTransitions.reject(booking);
    } else {
      throw new IllegalArgumentException(
          "Unsupported availability decision: " + event.getDecision());
    }

    saveBookingPort.save(updatedBooking);
  }
}
