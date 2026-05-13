package com.barbersaas.availability.application.port.in;

import com.barbersaas.availability.domain.model.BarberAvailability;

public interface GetBarberAvailabilityUseCase {

  BarberAvailability getAvailability(String shopId, String barberId, String date, String startTime);
}
