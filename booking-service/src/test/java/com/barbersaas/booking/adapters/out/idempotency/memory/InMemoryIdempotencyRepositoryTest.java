package com.barbersaas.booking.adapters.out.idempotency.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.barbersaas.booking.domain.model.idempotency.IdempotencyRecord;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class InMemoryIdempotencyRepositoryTest {

  private final InMemoryIdempotencyRepository repository = new InMemoryIdempotencyRepository();

  @Test
  void shouldSaveAndLoadIdempotencyRecord() {
    IdempotencyRecord record =
        IdempotencyRecord.builder()
            .idempotencyKey("idem-100")
            .bookingId("booking-100")
            .createdAt(LocalDateTime.of(2026, 4, 10, 10, 0))
            .build();

    repository.save(record);

    IdempotencyRecord loadedRecord = repository.loadByKey("idem-100").orElseThrow();

    assertEquals("idem-100", loadedRecord.getIdempotencyKey());
    assertEquals("booking-100", loadedRecord.getBookingId());
  }

  @Test
  void shouldReturnEmptyWhenIdempotencyRecordDoesNotExist() {
    assertTrue(repository.loadByKey("missing-idem").isEmpty());
  }
}
