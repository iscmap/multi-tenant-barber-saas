package com.barbersaas.availability.adapters.in.web;

import com.barbersaas.availability.api.contract.GetBarberAvailabilityResponse;
import com.barbersaas.availability.application.port.in.GetBarberAvailabilityUseCase;
import com.barbersaas.availability.domain.model.BarberAvailability;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/availability")
public class AvailabilityController {

  private final GetBarberAvailabilityUseCase getBarberAvailabilityUseCase;

  public AvailabilityController(GetBarberAvailabilityUseCase getBarberAvailabilityUseCase) {
    this.getBarberAvailabilityUseCase = getBarberAvailabilityUseCase;
  }

  @GetMapping("/{shopId}/{barberId}/{date}/{startTime}")
  public GetBarberAvailabilityResponse getAvailability(
      @PathVariable String shopId,
      @PathVariable String barberId,
      @PathVariable String date,
      @PathVariable String startTime) {
    BarberAvailability availability =
        getBarberAvailabilityUseCase.getAvailability(shopId, barberId, date, startTime);

    return GetBarberAvailabilityResponse.builder()
        .shopId(availability.getShopId())
        .barberId(availability.getBarberId())
        .date(availability.getDate())
        .startTime(availability.getStartTime())
        .durationMinutes(availability.getDurationMinutes())
        .status(availability.getStatus().name())
        .build();
  }
}
