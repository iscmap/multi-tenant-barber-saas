package com.barbersaas.booking.application.service.timeout;

import com.barbersaas.booking.application.command.timeout.RejectTimedOutBookingsCommand;
import com.barbersaas.booking.application.port.in.timeout.RejectTimedOutBookingsUseCase;
import com.barbersaas.booking.application.port.out.SaveBookingPort;
import com.barbersaas.booking.application.port.out.timeout.LoadPendingBookingsPort;
import com.barbersaas.booking.domain.model.Booking;
import com.barbersaas.booking.domain.policy.BookingTimeoutPolicy;
import com.barbersaas.booking.domain.rule.BookingStateTransitions;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BookingTimeoutApplicationService implements RejectTimedOutBookingsUseCase {

  private final LoadPendingBookingsPort loadPendingBookingsPort;
  private final SaveBookingPort saveBookingPort;

  public BookingTimeoutApplicationService(
      LoadPendingBookingsPort loadPendingBookingsPort, SaveBookingPort saveBookingPort) {
    this.loadPendingBookingsPort = loadPendingBookingsPort;
    this.saveBookingPort = saveBookingPort;
  }

  @Override
  public int rejectTimedOutBookings(RejectTimedOutBookingsCommand command) {

    List<Booking> pendingBookings = loadPendingBookingsPort.loadPendingBookings();

    int rejectedCount = 0;
    for (Booking booking : pendingBookings) {
      if (BookingTimeoutPolicy.isTimedOut(booking, command.getNow())) {
        Booking rejectedBooking = BookingStateTransitions.reject(booking);
        saveBookingPort.save(rejectedBooking);
        rejectedCount++;
      }
    }

    return rejectedCount;
  }
}
