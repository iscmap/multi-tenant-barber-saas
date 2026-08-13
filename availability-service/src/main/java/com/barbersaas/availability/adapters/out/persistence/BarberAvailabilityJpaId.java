package com.barbersaas.availability.adapters.out.persistence;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class BarberAvailabilityJpaId implements Serializable {

  private String shopId;
  private String barberId;
  private LocalDate date;
  private LocalTime startTime;

  public BarberAvailabilityJpaId() {}

  public BarberAvailabilityJpaId(
      String shopId, String barberId, LocalDate date, LocalTime startTime) {
    this.shopId = shopId;
    this.barberId = barberId;
    this.date = date;
    this.startTime = startTime;
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

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }

    if (!(object instanceof BarberAvailabilityJpaId)) {
      return false;
    }

    BarberAvailabilityJpaId that = (BarberAvailabilityJpaId) object;

    return Objects.equals(shopId, that.shopId)
        && Objects.equals(barberId, that.barberId)
        && Objects.equals(date, that.date)
        && Objects.equals(startTime, that.startTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(shopId, barberId, date, startTime);
  }
}
