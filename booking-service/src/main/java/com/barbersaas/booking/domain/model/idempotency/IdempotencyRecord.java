package com.barbersaas.booking.domain.model.idempotency;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class IdempotencyRecord {
  String idempotencyKey;
  String bookingId;
  LocalDateTime createdAt;
}
