package com.barbersaas.shared.events.contract;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingCreatedEvent {
  String eventId;
  String eventType;
  String occurredAt;
  String correlationId;

  String bookingId;
  String shopId;
  String barberId;
  String customerId;
  String date;
  String startTime;
  Integer durationMinutes;
  String serviceCode;
  String status;
}
