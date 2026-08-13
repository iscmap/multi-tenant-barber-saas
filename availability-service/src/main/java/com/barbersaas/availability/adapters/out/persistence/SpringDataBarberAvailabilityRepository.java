package com.barbersaas.availability.adapters.out.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBarberAvailabilityRepository
    extends JpaRepository<BarberAvailabilityJpaEntity, BarberAvailabilityJpaId> {
  Optional<BarberAvailabilityJpaEntity> findByShopIdAndBarberIdAndDateAndStartTime(
      String shopId, String barberId, LocalDate date, LocalTime startTime);
}
