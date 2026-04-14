package com.barbersaas.shared.events.contract;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailabilityDecidedEvent {
  String eventId;
  String eventType;
  String occurredAt;
  String correlationId;

  String bookingId;
  String shopId;
  String barberId;
  String decision;
  String reason;
}
