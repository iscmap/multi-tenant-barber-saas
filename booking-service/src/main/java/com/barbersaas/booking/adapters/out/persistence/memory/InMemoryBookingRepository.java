package com.barbersaas.booking.adapters.out.persistence.memory;

import com.barbersaas.booking.application.port.out.LoadBookingPort;
import com.barbersaas.booking.application.port.out.SaveBookingPort;
import com.barbersaas.booking.application.port.out.timeout.LoadPendingBookingsPort;
import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryBookingRepository
    implements SaveBookingPort, LoadBookingPort, LoadPendingBookingsPort {
  private final Map<String, Booking> storage = new ConcurrentHashMap<>();

  @Override
  public Optional<Booking> loadById(String bookingId) {
    return Optional.ofNullable(storage.get(bookingId));
  }

  @Override
  public Booking save(Booking booking) {
    String bookingId = booking.getBookingId();

    if (bookingId == null || bookingId.isBlank()) {
      bookingId = UUID.randomUUID().toString();
    }

    LocalDateTime createdAt = booking.getCreatedAt();
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }

    Booking bookingToStore =
        Booking.builder()
            .bookingId(bookingId)
            .shopId(booking.getShopId())
            .barberId(booking.getBarberId())
            .customerId(booking.getCustomerId())
            .date(booking.getDate())
            .startTime(booking.getStartTime())
            .durationMinutes(booking.getDurationMinutes())
            .serviceCode(booking.getServiceCode())
            .status(booking.getStatus())
            .createdAt(createdAt)
            .build();
    storage.put(bookingId, bookingToStore);
    return bookingToStore;
  }

  @Override
  public List<Booking> loadPendingBookings() {
    return storage.values().stream()
        .filter(booking -> booking.getStatus() == BookingStatus.PENDING)
        .toList();
  }
}
