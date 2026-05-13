package com.barbersaas.booking.adapters.out.idempotency.memory;

import com.barbersaas.booking.application.port.out.idempotency.LoadIdempotencyRecordPort;
import com.barbersaas.booking.application.port.out.idempotency.SaveIdempotencyRecordPort;
import com.barbersaas.booking.domain.model.idempotency.IdempotencyRecord;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryIdempotencyRepository
    implements LoadIdempotencyRecordPort, SaveIdempotencyRecordPort {
  private final Map<String, IdempotencyRecord> storage = new ConcurrentHashMap<>();

  @Override
  public Optional<IdempotencyRecord> loadByKey(String idempotencyKey) {
    return Optional.ofNullable(storage.get(idempotencyKey));
  }

  @Override
  public IdempotencyRecord save(IdempotencyRecord record) {
    storage.put(record.getIdempotencyKey(), record);
    return record;
  }
}
