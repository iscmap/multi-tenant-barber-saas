package com.barbersaas.booking.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBookingRepository extends JpaRepository<BookingJpaEntity, String> {}
