package com.barbersaas.booking.application.port.out.idempotency;

import com.barbersaas.booking.domain.model.idempotency.IdempotencyRecord;

public interface SaveIdempotencyRecordPort {
  IdempotencyRecord save(IdempotencyRecord record);
}
