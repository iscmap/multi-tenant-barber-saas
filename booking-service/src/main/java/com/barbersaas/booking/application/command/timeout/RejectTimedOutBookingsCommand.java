package com.barbersaas.booking.application.command.timeout;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RejectTimedOutBookingsCommand {
  OffsetDateTime now;
}
