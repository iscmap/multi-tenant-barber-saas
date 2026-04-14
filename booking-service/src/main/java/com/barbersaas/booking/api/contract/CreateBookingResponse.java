package com.barbersaas.booking.api.contract;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateBookingResponse {

  String bookingId;
  String status;
}
