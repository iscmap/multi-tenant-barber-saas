package com.barbersaas.booking.application.port.out;

import com.barbersaas.booking.domain.model.Booking;

public interface SaveBookingPort {
  Booking save(Booking booking);
}
