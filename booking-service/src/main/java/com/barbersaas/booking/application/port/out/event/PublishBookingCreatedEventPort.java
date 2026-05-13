package com.barbersaas.booking.application.port.out.event;

import com.barbersaas.shared.events.contract.BookingCreatedEvent;
import com.barbersaas.shared.events.envelope.EventEnvelope;

public interface PublishBookingCreatedEventPort {
  void publish(EventEnvelope<BookingCreatedEvent> eventEnvelope);
}
