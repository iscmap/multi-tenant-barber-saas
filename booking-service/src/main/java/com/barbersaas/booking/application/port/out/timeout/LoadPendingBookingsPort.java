package com.barbersaas.booking.application.port.out.timeout;

import com.barbersaas.booking.domain.model.Booking;
import java.util.List;

public interface LoadPendingBookingsPort {
  List<Booking> loadPendingBookings();
}
