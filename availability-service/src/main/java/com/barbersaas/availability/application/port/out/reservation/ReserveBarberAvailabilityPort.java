package com.barbersaas.availability.application.port.out.reservation;

import com.barbersaas.availability.domain.model.BarberAvailability;

public interface ReserveBarberAvailabilityPort {

  BarberAvailability reserve(BarberAvailability availability);
}
