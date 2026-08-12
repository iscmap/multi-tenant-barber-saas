package com.barbersaas.booking.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "bookings", schema = "booking")
public class BookingJpaEntity {
  @Id
  @Column(name = "booking_id", nullable = false, length = 100)
  private String bookingId;

  @Column(name = "shop_id", nullable = false, length = 100)
  private String shopId;

  @Column(name = "barber_id", nullable = false, length = 100)
  private String barberId;

  @Column(name = "customer_id", nullable = false, length = 100)
  private String customerId;

  @Column(name = "booking_date", nullable = false)
  private LocalDate date;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "duration_minutes", nullable = false)
  private Integer durationMinutes;

  @Column(name = "service_code", nullable = false, length = 100)
  private String serviceCode;

  @Column(name = "status", nullable = false, length = 30)
  private String status;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  protected BookingJpaEntity() {
    // Required by JPA
  }

  public BookingJpaEntity(
      String bookingId,
      String shopId,
      String barberId,
      String customerId,
      LocalDate date,
      LocalTime startTime,
      Integer durationMinutes,
      String serviceCode,
      String status,
      LocalDateTime createdAt) {
    this.bookingId = bookingId;
    this.shopId = shopId;
    this.barberId = barberId;
    this.customerId = customerId;
    this.date = date;
    this.startTime = startTime;
    this.durationMinutes = durationMinutes;
    this.serviceCode = serviceCode;
    this.status = status;
    this.createdAt = createdAt;
  }

  public String getBookingId() {
    return bookingId;
  }

  public String getShopId() {
    return shopId;
  }

  public String getBarberId() {
    return barberId;
  }

  public String getCustomerId() {
    return customerId;
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

  public String getServiceCode() {
    return serviceCode;
  }

  public String getStatus() {
    return status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
