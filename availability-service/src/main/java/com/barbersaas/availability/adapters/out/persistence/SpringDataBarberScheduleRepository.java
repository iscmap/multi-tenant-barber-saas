package com.barbersaas.availability.adapters.out.persistence;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBarberScheduleRepository
    extends JpaRepository<BarberScheduleJpaEntity, BarberScheduleJpaId> {

  Optional<BarberScheduleJpaEntity> findByShopIdAndBarberIdAndDate(
      String shopId, String barberId, LocalDate date);
}
