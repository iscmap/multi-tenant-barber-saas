package com.barbersaas.availability.adapters.out.deduplication.memory;

import com.barbersaas.availability.application.port.out.deduplication.LoadProcessedBookingEventPort;
import com.barbersaas.availability.application.port.out.deduplication.SaveProcessedBookingEventPort;
import com.barbersaas.availability.domain.model.event.ProcessedBookingEvent;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryProcessedBookingEventRepository
    implements LoadProcessedBookingEventPort, SaveProcessedBookingEventPort {

  private final Map<String, ProcessedBookingEvent> storage = new ConcurrentHashMap<>();

  @Override
  public Optional<ProcessedBookingEvent> loadByBookingIdAndEventType(
      String bookingId, String eventType) {
    return Optional.ofNullable(storage.get(key(bookingId, eventType)));
  }

  @Override
  public ProcessedBookingEvent save(ProcessedBookingEvent processedBookingEvent) {
    storage.put(
        key(processedBookingEvent.getBookingId(), processedBookingEvent.getEventType()),
        processedBookingEvent);
    return processedBookingEvent;
  }

  private String key(String bookingId, String eventType) {
    return bookingId + "|" + eventType;
  }
}
