package com.barbersaas.booking.application.service;

import com.barbersaas.booking.api.contract.CreateBookingRequest;
import com.barbersaas.booking.application.port.in.CreateBookingUseCase;
import com.barbersaas.booking.application.port.in.GetBookingUseCase;
import com.barbersaas.booking.domain.model.Booking;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Service;

@Service
public class BookingApplicationService implements CreateBookingUseCase, GetBookingUseCase {

  @Override
  public Booking createBooking(CreateBookingRequest request) {
    return Booking.builder()
        .bookingId("temp-booking-id")
        .shopId(request.getShopId())
        .barberId(request.getBarberId())
        .customerId(request.getCustomerId())
        .date(request.getDate())
        .startTime(request.getStartTime())
        .durationMinutes(request.getDurationMinutes())
        .serviceCode(request.getServiceCode())
        .status("PENDING")
        .build();
  }

  @Override
  public Booking getBooking(String bookingId) {
    return Booking.builder()
        .bookingId(bookingId)
        .shopId("shop-1")
        .barberId("barber-1")
        .customerId("customer-1")
        .date(LocalDate.of(2026, 4, 10))
        .startTime(LocalTime.of(10, 0))
        .durationMinutes(30)
        .serviceCode("HAIRCUT")
        .status("PENDING")
        .build();
  }
}
