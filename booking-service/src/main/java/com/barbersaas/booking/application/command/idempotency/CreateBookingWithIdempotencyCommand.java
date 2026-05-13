package com.barbersaas.booking.application.command.idempotency;

import com.barbersaas.booking.application.command.CreateBookingCommand;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateBookingWithIdempotencyCommand {
  String idempotencyKey;
  CreateBookingCommand createBookingCommand;
}
