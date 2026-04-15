package com.barbersaas.booking.application.port.in;

import com.barbersaas.booking.domain.model.Booking;

public interface GetBookingUseCase {

  Booking getBooking(String bookingId);
}
