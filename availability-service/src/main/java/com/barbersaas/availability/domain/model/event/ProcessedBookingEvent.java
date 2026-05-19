package com.barbersaas.availability.domain.model.event;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProcessedBookingEvent {

  String bookingId;
  String eventType;
  LocalDateTime processedAt;
}
