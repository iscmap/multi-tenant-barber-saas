package com.barbersaas.booking.application.query;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GetBookingQuery {
  String bookingId;
}
