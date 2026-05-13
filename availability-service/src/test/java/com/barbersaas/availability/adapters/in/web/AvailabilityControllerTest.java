package com.barbersaas.availability.adapters.in.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.barbersaas.availability.application.port.in.GetBarberAvailabilityUseCase;
import com.barbersaas.availability.domain.enums.AvailabilityStatus;
import com.barbersaas.availability.domain.model.BarberAvailability;
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
}
