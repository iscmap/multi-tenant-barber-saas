package com.barbersaas.booking.adapters.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.barbersaas.booking.adapters.in.web.exception.GlobalExceptionHandler;
import com.barbersaas.booking.adapters.in.web.filter.CorrelationIdFilter;
import com.barbersaas.booking.application.mapper.BookingApiMapper;
import com.barbersaas.booking.application.port.in.CreateBookingUseCase;
import com.barbersaas.booking.application.port.in.GetBookingUseCase;
import com.barbersaas.booking.application.port.in.timeout.RejectTimedOutBookingsUseCase;
import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookingController.class)
@Import({BookingApiMapper.class, GlobalExceptionHandler.class, CorrelationIdFilter.class})
class BookingControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private CreateBookingUseCase createBookingUseCase;

  @MockBean private GetBookingUseCase getBookingUseCase;

  @MockBean private RejectTimedOutBookingsUseCase rejectTimedOutBookingsUseCase;

  @Test
  void shouldCreateBookingContractResponse() throws Exception {
    Booking booking =
        Booking.builder()
            .bookingId("generated-booking-id")
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .status(BookingStatus.PENDING)
            .createdAt(LocalDateTime.of(2026, 4, 10, 10, 0))
            .build();

    when(createBookingUseCase.createBooking(any())).thenReturn(booking);

    String requestBody =
        """
            {
              "shopId": "shop-1",
              "barberId": "barber-1",
              "customerId": "customer-1",
              "date": "2026-04-10",
              "startTime": "10:00:00",
              "durationMinutes": 30,
              "serviceCode": "HAIRCUT"
            }
            """;

    mockMvc
        .perform(
            post("/api/v1/bookings")
                .with(
                    jwt()
                        .authorities(
                            new SimpleGrantedAuthority("ROLE_CUSTOMER"),
                            new SimpleGrantedAuthority("SCOPE_bookings.write")))
                .header("X-Correlation-Id", "corr-123")
                .header("Idempotency-Key", "idem-header-1")
                .contentType("application/json")
                .content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(header().string("X-Correlation-Id", "corr-123"))
        .andExpect(jsonPath("$.bookingId").value("generated-booking-id"))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  void shouldFailWhenIdempotencyHeaderIsMissing() throws Exception {
    String requestBody =
        """
            {
              "shopId": "shop-1",
              "barberId": "barber-1",
              "customerId": "customer-1",
              "date": "2026-04-10",
              "startTime": "10:00:00",
              "durationMinutes": 30,
              "serviceCode": "HAIRCUT"
            }
            """;

    mockMvc
        .perform(
            post("/api/v1/bookings")
                .with(
                    jwt()
                        .authorities(
                            new SimpleGrantedAuthority("ROLE_CUSTOMER"),
                            new SimpleGrantedAuthority("SCOPE_bookings.write")))
                .header("X-Correlation-Id", "corr-124")
                .contentType("application/json")
                .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnProblemJsonForValidationError() throws Exception {
    String requestBody =
        """
            {
              "shopId": "",
              "barberId": "barber-1",
              "customerId": "customer-1",
              "date": "2026-04-10",
              "startTime": "10:00:00",
              "durationMinutes": 0,
              "serviceCode": ""
            }
            """;

    mockMvc
        .perform(
            post("/api/v1/bookings")
                .with(
                    jwt()
                        .authorities(
                            new SimpleGrantedAuthority("ROLE_CUSTOMER"),
                            new SimpleGrantedAuthority("SCOPE_bookings.write")))
                .header("X-Correlation-Id", "corr-456")
                .header("Idempotency-Key", "idem-header-2")
                .contentType("application/json")
                .content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(header().string("X-Correlation-Id", "corr-456"))
        .andExpect(jsonPath("$.title").value("Validation error"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.correlationId").value("corr-456"));
  }

  @Test
  void shouldGetBookingContractResponse() throws Exception {
    Booking booking =
        Booking.builder()
            .bookingId("booking-123")
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .status(BookingStatus.PENDING)
            .createdAt(LocalDateTime.of(2026, 4, 10, 10, 0))
            .build();

    when(getBookingUseCase.getBooking(any())).thenReturn(booking);

    mockMvc
        .perform(
            get("/api/v1/bookings/booking-123").with(jwt()).header("X-Correlation-Id", "corr-789"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Correlation-Id", "corr-789"))
        .andExpect(jsonPath("$.bookingId").value("booking-123"))
        .andExpect(jsonPath("$.shopId").value("shop-1"))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  void shouldReturnNotFoundProblemWhenBookingDoesNotExist() throws Exception {
    when(getBookingUseCase.getBooking(any()))
        .thenThrow(new IllegalArgumentException("Booking not found: missing-id"));

    mockMvc
        .perform(
            get("/api/v1/bookings/missing-id").with(jwt()).header("X-Correlation-Id", "corr-999"))
        .andExpect(status().isNotFound())
        .andExpect(header().string("X-Correlation-Id", "corr-999"))
        .andExpect(jsonPath("$.title").value("Resource not found"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("Booking not found: missing-id"))
        .andExpect(jsonPath("$.correlationId").value("corr-999"));
  }

  @Test
  void shouldRejectTimedOutBookings() throws Exception {
    when(rejectTimedOutBookingsUseCase.rejectTimedOutBookings(any())).thenReturn(2);

    mockMvc
        .perform(
            post("/api/v1/internal/bookings/reject-timeouts")
                .with(jwt())
                .header("X-Correlation-Id", "corr-777"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Correlation-Id", "corr-777"))
        .andExpect(jsonPath("$.rejectedCount").value(2))
        .andExpect(jsonPath("$.processedAt").exists());
  }
}
