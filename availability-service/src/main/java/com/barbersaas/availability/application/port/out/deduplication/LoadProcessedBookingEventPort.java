package com.barbersaas.availability.application.port.out.deduplication;

import com.barbersaas.availability.domain.model.event.ProcessedBookingEvent;
import java.util.Optional;

public interface LoadProcessedBookingEventPort {

  Optional<ProcessedBookingEvent> loadByBookingIdAndEventType(String bookingId, String eventType);
}
