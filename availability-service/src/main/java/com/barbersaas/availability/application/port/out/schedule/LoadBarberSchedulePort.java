package com.barbersaas.availability.application.port.out.schedule;

import com.barbersaas.availability.domain.model.BarberSchedule;
import java.time.LocalDate;
import java.util.Optional;

public interface LoadBarberSchedulePort {

  Optional<BarberSchedule> loadByBarberAndDate(String shopId, String barberId, LocalDate date);
}
