package com.barbersaas.availability.config.security;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.barbersaas.availability.adapters.in.web.AvailabilityController;
import com.barbersaas.availability.application.port.in.GetBarberAvailabilityUseCase;
import com.barbersaas.availability.application.port.in.schedule.GetBarberScheduleUseCase;
import com.barbersaas.availability.application.port.in.validation.ValidateSlotUseCase;
import com.barbersaas.availability.domain.enums.AvailabilityStatus;
import com.barbersaas.availability.domain.model.BarberAvailability;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AvailabilityController.class)
@Import(SecurityConfig.class)
@TestPropertySource(
    properties = {
      "barbersaas.security.jwt.secret=local-test-jwt-secret-012345678901234567890123456789"
    })
class SecurityConfigTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private GetBarberAvailabilityUseCase getBarberAvailabilityUseCase;

  @MockBean private GetBarberScheduleUseCase getBarberScheduleUseCase;

  @MockBean private ValidateSlotUseCase validateSlotUseCase;

  @Test
  void shouldRejectRequestWithoutJwt() throws Exception {
    mockMvc
        .perform(get("/api/v1/availability/shop-1/barber-1/2026-04-10/10:00"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldRejectRequestWithoutRequiredAuthorities() throws Exception {
    mockMvc
        .perform(get("/api/v1/availability/shop-1/barber-1/2026-04-10/10:00").with(jwt()))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldAllowAuthorizedCustomer() throws Exception {
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
        .perform(
            get("/api/v1/availability/shop-1/barber-1/2026-04-10/10:00")
                .with(
                    jwt()
                        .authorities(
                            new SimpleGrantedAuthority("ROLE_CUSTOMER"),
                            new SimpleGrantedAuthority("SCOPE_availability.read"))))
        .andExpect(status().isOk());
  }
}
