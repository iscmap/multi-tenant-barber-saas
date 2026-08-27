package com.barbersaas.booking.config.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.barbersaas.booking.adapters.in.web.BookingController;
import com.barbersaas.booking.application.mapper.BookingApiMapper;
import com.barbersaas.booking.application.port.in.CreateBookingUseCase;
import com.barbersaas.booking.application.port.in.GetBookingUseCase;
import com.barbersaas.booking.application.port.in.timeout.RejectTimedOutBookingsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookingController.class)
@Import(SecurityConfig.class)
@TestPropertySource(
    properties = {
      "barbersaas.security.jwt.secret=local-test-jwt-secret-012345678901234567890123456789"
    })
class SecurityConfigTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private CreateBookingUseCase createBookingUseCase;

  @MockBean private GetBookingUseCase getBookingUseCase;

  @MockBean private RejectTimedOutBookingsUseCase rejectTimedOutBookingsUseCase;

  @MockBean private BookingApiMapper bookingApiMapper;

  @Test
  void shouldRejectInternalRequestWithoutJwt() throws Exception {
    mockMvc
        .perform(post("/api/v1/internal/bookings/reject-timeouts"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldRejectInternalRequestWithoutRequiredAuthorities() throws Exception {
    mockMvc
        .perform(post("/api/v1/internal/bookings/reject-timeouts").with(jwt()))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldAllowInternalRequestForTrustedService() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/internal/bookings/reject-timeouts")
                .with(
                    jwt()
                        .authorities(
                            new SimpleGrantedAuthority("ROLE_SERVICE"),
                            new SimpleGrantedAuthority("SCOPE_internal"))))
        .andExpect(status().isOk());
  }
}
