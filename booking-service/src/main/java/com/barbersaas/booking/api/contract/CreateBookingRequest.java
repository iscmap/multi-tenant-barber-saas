package com.barbersaas.booking.api.contract;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateBookingRequest {
  @NotBlank String shopId;

  @NotBlank String barberId;

  @NotBlank String customerId;

  @NotNull LocalDate date;

  @NotNull LocalTime startTime;

  @NotNull
  @Min(1)
  Integer durationMinutes;

  @NotBlank String serviceCode;
}
