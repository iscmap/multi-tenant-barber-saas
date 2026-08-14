package com.barbersaas.booking.adapters.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBookingRepository extends JpaRepository<BookingJpaEntity, String> {
  List<BookingJpaEntity> findByStatus(String status);
}
