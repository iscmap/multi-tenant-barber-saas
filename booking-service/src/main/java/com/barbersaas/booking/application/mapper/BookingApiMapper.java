package com.barbersaas.booking.application.mapper;

import com.barbersaas.booking.api.contract.CreateBookingRequest;
import com.barbersaas.booking.api.contract.CreateBookingResponse;
import com.barbersaas.booking.api.contract.GetBookingResponse;
import com.barbersaas.booking.domain.model.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingApiMapper {

  public Booking toDomainForCreate(CreateBookingRequest request) {
    return Booking.builder()
        .shopId(request.getShopId())
        .barberId(request.getBarberId())
        .customerId(request.getCustomerId())
        .date(request.getDate())
        .startTime(request.getStartTime())
        .durationMinutes(request.getDurationMinutes())
        .serviceCode(request.getServiceCode())
        .build();
  }

  public CreateBookingResponse toCreateBookingResponse(Booking booking) {
    return CreateBookingResponse.builder()
        .bookingId(booking.getBookingId())
        .status(booking.getStatus())
        .build();
  }

  public GetBookingResponse toGetBookingResponse(Booking booking) {
    return GetBookingResponse.builder()
        .bookingId(booking.getBookingId())
        .shopId(booking.getShopId())
        .barberId(booking.getBarberId())
        .customerId(booking.getCustomerId())
        .date(booking.getDate())
        .startTime(booking.getStartTime())
        .durationMinutes(booking.getDurationMinutes())
        .serviceCode(booking.getServiceCode())
        .status(booking.getStatus())
        .build();
  }
}
