package com.barbersaas.availability.adapters.out.persistence;

import com.barbersaas.availability.application.port.out.LoadBarberAvailabilityPort;
import com.barbersaas.availability.application.port.out.reservation.ReserveBarberAvailabilityPort;
import com.barbersaas.availability.domain.enums.AvailabilityStatus;
import com.barbersaas.availability.domain.model.BarberAvailability;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class JpaBarberAvailabilityRepositoryAdapter
    implements LoadBarberAvailabilityPort, ReserveBarberAvailabilityPort {

  private final SpringDataBarberAvailabilityRepository repository;

  public JpaBarberAvailabilityRepositoryAdapter(SpringDataBarberAvailabilityRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<BarberAvailability> loadByBarberAndSlot(
      String shopId, String barberId, LocalDate date, LocalTime startTime) {

    return repository
        .findByShopIdAndBarberIdAndDateAndStartTime(shopId, barberId, date, startTime)
        .map(this::toDomain);
  }

  @Override
  public BarberAvailability reserve(BarberAvailability availability) {

    BarberAvailability reserved =
        BarberAvailability.builder()
            .shopId(availability.getShopId())
            .barberId(availability.getBarberId())
            .date(availability.getDate())
            .startTime(availability.getStartTime())
            .durationMinutes(availability.getDurationMinutes())
            .status(AvailabilityStatus.RESERVED)
            .build();

    BarberAvailabilityJpaEntity saved = repository.save(toEntity(reserved));

    return toDomain(saved);
  }

  private BarberAvailabilityJpaEntity toEntity(BarberAvailability availability) {

    return new BarberAvailabilityJpaEntity(
        availability.getShopId(),
        availability.getBarberId(),
        availability.getDate(),
        availability.getStartTime(),
        availability.getDurationMinutes(),
        availability.getStatus().name());
  }

  private BarberAvailability toDomain(BarberAvailabilityJpaEntity entity) {

    return BarberAvailability.builder()
        .shopId(entity.getShopId())
        .barberId(entity.getBarberId())
        .date(entity.getDate())
        .startTime(entity.getStartTime())
        .durationMinutes(entity.getDurationMinutes())
        .status(AvailabilityStatus.valueOf(entity.getStatus()))
        .build();
  }
}
