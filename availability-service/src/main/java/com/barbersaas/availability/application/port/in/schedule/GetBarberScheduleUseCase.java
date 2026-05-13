package com.barbersaas.availability.application.port.in.schedule;

import com.barbersaas.availability.domain.model.BarberSchedule;

public interface GetBarberScheduleUseCase {

  BarberSchedule getSchedule(String shopId, String barberId, String date);
}
