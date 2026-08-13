package com.barbersaas.availability.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "barber_availability", schema = "availability")
@IdClass(BarberAvailabilityJpaId.class)
public class BarberAvailabilityJpaEntity {

  @Id
  @Column(name = "shop_id", nullable = false, length = 100)
  private String shopId;

  @Id
  @Column(name = "barber_id", nullable = false, length = 100)
  private String barberId;

  @Id
  @Column(name = "availability_date", nullable = false)
  private LocalDate date;

  @Id
  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "duration_minutes", nullable = false)
  private Integer durationMinutes;

  @Column(name = "status", nullable = false, length = 30)
  private String status;

  protected BarberAvailabilityJpaEntity() {
    // Required by JPA
  }

  public BarberAvailabilityJpaEntity(
      String shopId,
      String barberId,
      LocalDate date,
      LocalTime startTime,
      Integer durationMinutes,
      String status) {
    this.shopId = shopId;
    this.barberId = barberId;
    this.date = date;
    this.startTime = startTime;
    this.durationMinutes = durationMinutes;
    this.status = status;
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

  public LocalTime getStartTime() {
    return startTime;
  }

  public Integer getDurationMinutes() {
    return durationMinutes;
  }

  public String getStatus() {
    return status;
  }
}
