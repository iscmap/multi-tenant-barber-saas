package com.barbersaas.booking.application.port.out.idempotency;

import com.barbersaas.booking.domain.model.idempotency.IdempotencyRecord;
import java.util.Optional;

public interface LoadIdempotencyRecordPort {
  Optional<IdempotencyRecord> loadByKey(String idempotencyKey);
}
