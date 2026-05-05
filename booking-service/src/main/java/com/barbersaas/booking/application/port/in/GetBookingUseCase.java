package com.barbersaas.booking.application.port.in;

import com.barbersaas.booking.application.query.GetBookingQuery;
import com.barbersaas.booking.domain.model.Booking;

public interface GetBookingUseCase {

  Booking getBooking(GetBookingQuery query);
}
