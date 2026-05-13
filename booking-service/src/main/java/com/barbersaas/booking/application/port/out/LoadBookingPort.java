package com.barbersaas.booking.application.port.out;

import com.barbersaas.booking.domain.model.Booking;
import java.util.Optional;

public interface LoadBookingPort {
  Optional<Booking> loadById(String bookingId);
}
