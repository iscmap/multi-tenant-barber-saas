package com.barbersaas.availability.adapters.in.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.barbersaas.availability.application.port.in.GetBarberAvailabilityUseCase;
import com.barbersaas.availability.application.port.in.schedule.GetBarberScheduleUseCase;
import com.barbersaas.availability.application.port.in.validation.ValidateSlotUseCase;
import com.barbersaas.availability.domain.enums.AvailabilityStatus;
import com.barbersaas.availability.domain.model.BarberAvailability;
import com.barbersaas.availability.domain.model.BarberSchedule;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AvailabilityController.class)
class AvailabilityControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private GetBarberAvailabilityUseCase getBarberAvailabilityUseCase;

  @MockBean private GetBarberScheduleUseCase getBarberScheduleUseCase;

  @MockBean private ValidateSlotUseCase validateSlotUseCase;

  @Test
  void shouldReturnAvailabilityResponse() throws Exception {
    BarberAvailability availability =
        BarberAvailability.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .status(AvailabilityStatus.AVAILABLE)
            .build();

    when(getBarberAvailabilityUseCase.getAvailability(
            eq("shop-1"), eq("barber-1"), eq("2026-04-10"), eq("10:00")))
        .thenReturn(availability);

    mockMvc
        .perform(get("/api/v1/availability/shop-1/barber-1/2026-04-10/10:00"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shopId").value("shop-1"))
        .andExpect(jsonPath("$.barberId").value("barber-1"))
        .andExpect(jsonPath("$.status").value("AVAILABLE"));
  }

  @Test
  void shouldReturnScheduleResponse() throws Exception {
    BarberSchedule schedule =
        BarberSchedule.builder()
            .shopId("shop-1")
            .barberId("barber-1")
            .date(LocalDate.of(2026, 4, 10))
            .workStartTime(LocalTime.of(10, 0))
            .workEndTime(LocalTime.of(18, 0))
            .slotDurationMinutes(30)
            .build();

    when(getBarberScheduleUseCase.getSchedule(eq("shop-1"), eq("barber-1"), eq("2026-04-10")))
        .thenReturn(schedule);

    mockMvc
        .perform(get("/api/v1/availability/schedule/shop-1/barber-1/2026-04-10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shopId").value("shop-1"))
        .andExpect(jsonPath("$.barberId").value("barber-1"))
        .andExpect(jsonPath("$.workStartTime").value("10:00:00"))
        .andExpect(jsonPath("$.workEndTime").value("18:00:00"))
        .andExpect(jsonPath("$.slotDurationMinutes").value(30));
  }

  @Test
  void shouldReturnSlotValidationSuccess() throws Exception {
    doNothing()
        .when(validateSlotUseCase)
        .validateSlot("shop-1", "barber-1", "2026-04-10", "10:00", 30);

    mockMvc
        .perform(get("/api/v1/availability/validate/shop-1/barber-1/2026-04-10/10:00/30"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.valid").value(true))
        .andExpect(jsonPath("$.shopId").value("shop-1"))
        .andExpect(jsonPath("$.barberId").value("barber-1"))
        .andExpect(jsonPath("$.durationMinutes").value(30));
  }
}
