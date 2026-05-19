package com.barbersaas.availability.adapters.out.deduplication.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.barbersaas.availability.domain.model.event.ProcessedBookingEvent;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class InMemoryProcessedBookingEventRepositoryTest {

  private final InMemoryProcessedBookingEventRepository repository =
      new InMemoryProcessedBookingEventRepository();

  @Test
  void shouldSaveAndLoadProcessedBookingEvent() {
    ProcessedBookingEvent processedBookingEvent =
        ProcessedBookingEvent.builder()
            .bookingId("booking-1")
            .eventType("BookingCreated")
            .processedAt(LocalDateTime.of(2026, 4, 10, 10, 0))
            .build();

    repository.save(processedBookingEvent);

    ProcessedBookingEvent loaded =
        repository.loadByBookingIdAndEventType("booking-1", "BookingCreated").orElseThrow();

    assertEquals("booking-1", loaded.getBookingId());
    assertEquals("BookingCreated", loaded.getEventType());
  }

  @Test
  void shouldReturnEmptyWhenProcessedEventDoesNotExist() {
    assertTrue(
        repository.loadByBookingIdAndEventType("missing-booking", "BookingCreated").isEmpty());
  }
}
