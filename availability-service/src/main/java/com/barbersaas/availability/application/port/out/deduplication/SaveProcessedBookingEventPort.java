package com.barbersaas.availability.application.port.out.deduplication;

import com.barbersaas.availability.domain.model.event.ProcessedBookingEvent;

public interface SaveProcessedBookingEventPort {

  ProcessedBookingEvent save(ProcessedBookingEvent processedBookingEvent);
}
