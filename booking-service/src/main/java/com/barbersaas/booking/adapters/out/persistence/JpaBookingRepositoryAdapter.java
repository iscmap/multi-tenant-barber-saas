package com.barbersaas.booking.adapters.out.persistence;

import com.barbersaas.booking.application.port.out.LoadBookingPort;
import com.barbersaas.booking.application.port.out.SaveBookingPort;
import com.barbersaas.booking.application.port.out.timeout.LoadPendingBookingsPort;
import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaBookingRepositoryAdapter
    implements SaveBookingPort, LoadBookingPort, LoadPendingBookingsPort {

  private final SpringDataBookingRepository repository;

  public JpaBookingRepositoryAdapter(SpringDataBookingRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<Booking> loadById(String bookingId) {
    return repository.findById(bookingId).map(this::toDomain);
  }

  @Override
  public Booking save(Booking booking) {
    BookingJpaEntity entity = toEntity(booking);
    BookingJpaEntity savedEntity = repository.save(entity);
    return toDomain(savedEntity);
  }

  @Override
  public List<Booking> loadPendingBookings() {
    return repository.findByStatus(BookingStatus.PENDING.name()).stream()
        .map(this::toDomain)
        .toList();
  }

  private BookingJpaEntity toEntity(Booking booking) {
    return new BookingJpaEntity(
        booking.getBookingId(),
        booking.getShopId(),
        booking.getBarberId(),
        booking.getCustomerId(),
        booking.getDate(),
        booking.getStartTime(),
        booking.getDurationMinutes(),
        booking.getServiceCode(),
        booking.getStatus().name(),
        booking.getCreatedAt());
  }

  private Booking toDomain(BookingJpaEntity entity) {
    return Booking.builder()
        .bookingId(entity.getBookingId())
        .shopId(entity.getShopId())
        .barberId(entity.getBarberId())
        .customerId(entity.getCustomerId())
        .date(entity.getDate())
        .startTime(entity.getStartTime())
        .durationMinutes(entity.getDurationMinutes())
        .serviceCode(entity.getServiceCode())
        .status(BookingStatus.valueOf(entity.getStatus()))
        .createdAt(entity.getCreatedAt())
        .build();
  }
}
