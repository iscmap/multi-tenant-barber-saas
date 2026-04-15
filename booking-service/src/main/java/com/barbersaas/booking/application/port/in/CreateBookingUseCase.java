package com.barbersaas.booking.application.port.in;

import com.barbersaas.booking.api.contract.CreateBookingRequest;
import com.barbersaas.booking.domain.model.Booking;

public interface CreateBookingUseCase {

  Booking createBooking(CreateBookingRequest request);
}
