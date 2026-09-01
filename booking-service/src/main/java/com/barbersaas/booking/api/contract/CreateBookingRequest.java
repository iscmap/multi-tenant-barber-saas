package com.barbersaas.booking.api.contract;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateBookingRequest {
  @NotBlank
  @Size(max = 64)
  @Pattern(regexp = "^[A-Za-z0-9_-]+$")
  String shopId;

  @NotBlank
  @Size(max = 64)
  @Pattern(regexp = "^[A-Za-z0-9_-]+$")
  String barberId;

  @NotBlank
  @Size(max = 64)
  @Pattern(regexp = "^[A-Za-z0-9_-]+$")
  String customerId;

  @NotNull LocalDate date;

  @NotNull LocalTime startTime;

  @NotNull
  @Min(1)
  @Max(480)
  Integer durationMinutes;

  @NotBlank
  @Size(max = 64)
  @Pattern(regexp = "^[A-Za-z0-9_-]+$")
  String serviceCode;
}
