package com.barbersaas.availability.adapters.out.persistence;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class BarberScheduleJpaId implements Serializable {

  private String shopId;
  private String barberId;
  private LocalDate date;

  public BarberScheduleJpaId() {}

  public BarberScheduleJpaId(String shopId, String barberId, LocalDate date) {
    this.shopId = shopId;
    this.barberId = barberId;
    this.date = date;
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

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }

    if (!(object instanceof BarberScheduleJpaId)) {
      return false;
    }

    BarberScheduleJpaId that = (BarberScheduleJpaId) object;

    return Objects.equals(shopId, that.shopId)
        && Objects.equals(barberId, that.barberId)
        && Objects.equals(date, that.date);
  }

  @Override
  public int hashCode() {
    return Objects.hash(shopId, barberId, date);
  }
}
