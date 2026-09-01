package com.barbersaas.availability.adapters.in.web;

import com.barbersaas.availability.api.contract.GetBarberAvailabilityResponse;
import com.barbersaas.availability.api.contract.schedule.GetBarberScheduleResponse;
import com.barbersaas.availability.application.port.in.GetBarberAvailabilityUseCase;
import com.barbersaas.availability.application.port.in.schedule.GetBarberScheduleUseCase;
import com.barbersaas.availability.application.port.in.validation.ValidateSlotUseCase;
import com.barbersaas.availability.domain.model.BarberAvailability;
import com.barbersaas.availability.domain.model.BarberSchedule;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/availability")
@Validated
public class AvailabilityController {

  private final GetBarberAvailabilityUseCase getBarberAvailabilityUseCase;
  private final GetBarberScheduleUseCase getBarberScheduleUseCase;
  private final ValidateSlotUseCase validateSlotUseCase;

  public AvailabilityController(
      GetBarberAvailabilityUseCase getBarberAvailabilityUseCase,
      GetBarberScheduleUseCase getBarberScheduleUseCase,
      ValidateSlotUseCase validateSlotUseCase) {
    this.getBarberAvailabilityUseCase = getBarberAvailabilityUseCase;
    this.getBarberScheduleUseCase = getBarberScheduleUseCase;
    this.validateSlotUseCase = validateSlotUseCase;
  }

  @GetMapping("/{shopId}/{barberId}/{date}/{startTime}")
  public GetBarberAvailabilityResponse getAvailability(
      @PathVariable @Size(min = 1, max = 64) @Pattern(regexp = "^[A-Za-z0-9_-]+$") String shopId,
      @PathVariable @Size(min = 1, max = 64) @Pattern(regexp = "^[A-Za-z0-9_-]+$") String barberId,
      @PathVariable @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$") String date,
      @PathVariable @Pattern(regexp = "^\\d{2}:\\d{2}$") String startTime) {

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

  @GetMapping("/schedule/{shopId}/{barberId}/{date}")
  public GetBarberScheduleResponse getSchedule(
      @PathVariable @Size(min = 1, max = 64) @Pattern(regexp = "^[A-Za-z0-9_-]+$") String shopId,
      @PathVariable @Size(min = 1, max = 64) @Pattern(regexp = "^[A-Za-z0-9_-]+$") String barberId,
      @PathVariable @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$") String date) {

    BarberSchedule schedule = getBarberScheduleUseCase.getSchedule(shopId, barberId, date);

    return GetBarberScheduleResponse.builder()
        .shopId(schedule.getShopId())
        .barberId(schedule.getBarberId())
        .date(schedule.getDate())
        .workStartTime(schedule.getWorkStartTime())
        .workEndTime(schedule.getWorkEndTime())
        .slotDurationMinutes(schedule.getSlotDurationMinutes())
        .build();
  }

  @GetMapping("/validate/{shopId}/{barberId}/{date}/{startTime}/{durationMinutes}")
  public Map<String, Object> validateSlot(
      @PathVariable @Size(min = 1, max = 64) @Pattern(regexp = "^[A-Za-z0-9_-]+$") String shopId,
      @PathVariable @Size(min = 1, max = 64) @Pattern(regexp = "^[A-Za-z0-9_-]+$") String barberId,
      @PathVariable @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$") String date,
      @PathVariable @Pattern(regexp = "^\\d{2}:\\d{2}$") String startTime,
      @PathVariable @Min(1) @Max(480) int durationMinutes) {

    validateSlotUseCase.validateSlot(shopId, barberId, date, startTime, durationMinutes);

    return Map.of(
        "valid", true,
        "shopId", shopId,
        "barberId", barberId,
        "date", date,
        "startTime", startTime,
        "durationMinutes", durationMinutes);
  }
}
