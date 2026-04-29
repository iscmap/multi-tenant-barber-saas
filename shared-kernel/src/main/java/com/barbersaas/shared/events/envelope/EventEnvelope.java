package com.barbersaas.shared.events.envelope;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class EventEnvelope<T> {
  String eventId;
  String eventType;
  String occurredAt;
  String correlationId;
  String source;
  String tenantId;
  T payload;
}
