package com.barbersaas.booking.application.port.in.timeout;

import com.barbersaas.booking.application.command.timeout.RejectTimedOutBookingsCommand;

public interface RejectTimedOutBookingsUseCase {
  int rejectTimedOutBookings(RejectTimedOutBookingsCommand command);
}
