package com.barbersaas.availability.adapters.out.persistence;

import com.barbersaas.availability.application.port.out.schedule.LoadBarberSchedulePort;
import com.barbersaas.availability.domain.model.BarberSchedule;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaBarberScheduleRepositoryAdapter implements LoadBarberSchedulePort {

  private final SpringDataBarberScheduleRepository repository;

  public JpaBarberScheduleRepositoryAdapter(SpringDataBarberScheduleRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<BarberSchedule> loadByBarberAndDate(
      String shopId, String barberId, LocalDate date) {

    return repository.findByShopIdAndBarberIdAndDate(shopId, barberId, date).map(this::toDomain);
  }

  private BarberSchedule toDomain(BarberScheduleJpaEntity entity) {

    return BarberSchedule.builder()
        .shopId(entity.getShopId())
        .barberId(entity.getBarberId())
        .date(entity.getDate())
        .workStartTime(entity.getWorkStartTime())
        .workEndTime(entity.getWorkEndTime())
        .slotDurationMinutes(entity.getSlotDurationMinutes())
        .build();
  }
}
