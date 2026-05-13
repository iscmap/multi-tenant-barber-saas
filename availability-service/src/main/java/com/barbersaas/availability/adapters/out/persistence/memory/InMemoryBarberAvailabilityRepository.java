package com.barbersaas.availability.adapters.out.persistence.memory;

import com.barbersaas.availability.application.port.out.LoadBarberAvailabilityPort;
import com.barbersaas.availability.application.port.out.reservation.ReserveBarberAvailabilityPort;
import com.barbersaas.availability.application.port.out.schedule.LoadBarberSchedulePort;
import com.barbersaas.availability.domain.enums.AvailabilityStatus;
import com.barbersaas.availability.domain.model.BarberAvailability;
import com.barbersaas.availability.domain.model.BarberSchedule;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryBarberAvailabilityRepository
    implements LoadBarberAvailabilityPort, LoadBarberSchedulePort, ReserveBarberAvailabilityPort {

  private final Map<String, BarberAvailability> availabilityStorage = new ConcurrentHashMap<>();
  private final Map<String, BarberSchedule> scheduleStorage = new ConcurrentHashMap<>();

  public InMemoryBarberAvailabilityRepository() {
    BarberSchedule schedule =
        BarberSchedule.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .date(LocalDate.of(2026, 4, 10))
            .workStartTime(LocalTime.of(10, 0))
            .workEndTime(LocalTime.of(18, 0))
            .slotDurationMinutes(30)
            .build();

    scheduleStorage.put(
        scheduleKey(schedule.getShopId(), schedule.getBarberId(), schedule.getDate()), schedule);

    BarberAvailability slot =
        BarberAvailability.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .status(AvailabilityStatus.AVAILABLE)
            .build();

    availabilityStorage.put(
        availabilityKey(slot.getShopId(), slot.getBarberId(), slot.getDate(), slot.getStartTime()),
        slot);
  }

  @Override
  public Optional<BarberAvailability> loadByBarberAndSlot(
      String shopId, String barberId, LocalDate date, LocalTime startTime) {
    return Optional.ofNullable(
        availabilityStorage.get(availabilityKey(shopId, barberId, date, startTime)));
  }

  @Override
  public Optional<BarberSchedule> loadByBarberAndDate(
      String shopId, String barberId, LocalDate date) {
    return Optional.ofNullable(scheduleStorage.get(scheduleKey(shopId, barberId, date)));
  }

  private String availabilityKey(
      String shopId, String barberId, LocalDate date, LocalTime startTime) {
    return shopId + "|" + barberId + "|" + date + "|" + startTime;
  }

  private String scheduleKey(String shopId, String barberId, LocalDate date) {
    return shopId + "|" + barberId + "|" + date;
  }

  @Override
  public BarberAvailability reserve(BarberAvailability availability) {
    BarberAvailability reservedAvailability =
        BarberAvailability.builder()
            .shopId(availability.getShopId())
            .barberId(availability.getBarberId())
            .date(availability.getDate())
            .startTime(availability.getStartTime())
            .durationMinutes(availability.getDurationMinutes())
            .status(AvailabilityStatus.RESERVED)
            .build();

    availabilityStorage.put(
        availabilityKey(
            reservedAvailability.getShopId(),
            reservedAvailability.getBarberId(),
            reservedAvailability.getDate(),
            reservedAvailability.getStartTime()),
        reservedAvailability);

    return reservedAvailability;
  }
}
