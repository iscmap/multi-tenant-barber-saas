package com.barbersaas.availability.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "barber_schedule", schema = "availability")
@IdClass(BarberScheduleJpaId.class)
public class BarberScheduleJpaEntity {

  @Id
  @Column(name = "shop_id", nullable = false, length = 100)
  private String shopId;

  @Id
  @Column(name = "barber_id", nullable = false, length = 100)
  private String barberId;

  @Id
  @Column(name = "schedule_date", nullable = false)
  private LocalDate date;

  @Column(name = "work_start_time", nullable = false)
  private LocalTime workStartTime;

  @Column(name = "work_end_time", nullable = false)
  private LocalTime workEndTime;

  @Column(name = "slot_duration_minutes", nullable = false)
  private Integer slotDurationMinutes;

  protected BarberScheduleJpaEntity() {}

  public BarberScheduleJpaEntity(
      String shopId,
      String barberId,
      LocalDate date,
      LocalTime workStartTime,
      LocalTime workEndTime,
      Integer slotDurationMinutes) {
    this.shopId = shopId;
    this.barberId = barberId;
    this.date = date;
    this.workStartTime = workStartTime;
    this.workEndTime = workEndTime;
    this.slotDurationMinutes = slotDurationMinutes;
  }

  public String getShopId() {
    return shopId;
  }

  public String getBarberId() {
    return barberId;
  }

  public LocalDate getDate() {
    return date;
  }

  public LocalTime getWorkStartTime() {
    return workStartTime;
  }

  public LocalTime getWorkEndTime() {
    return workEndTime;
  }

  public Integer getSlotDurationMinutes() {
    return slotDurationMinutes;
  }
}
