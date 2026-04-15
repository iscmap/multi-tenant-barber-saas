package com.barbersaas.booking.adapters.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.barbersaas.booking.application.port.in.CreateBookingUseCase;
import com.barbersaas.booking.application.port.in.GetBookingUseCase;
import com.barbersaas.booking.domain.model.Booking;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private CreateBookingUseCase createBookingUseCase;

  @MockBean private GetBookingUseCase getBookingUseCase;

  @Test
  void shouldCreateBookingContractResponse() throws Exception {
    Booking booking =
        Booking.builder()
            .bookingId("temp-booking-id")
            .shopId("shop-1")
            .barberId("barber-1")
            .customerId("customer-1")
            .date(LocalDate.of(2026, 4, 10))
            .startTime(LocalTime.of(10, 0))
            .durationMinutes(30)
            .serviceCode("HAIRCUT")
            .status("PENDING")
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
        .perform(post("/api/v1/bookings").contentType("application/json").content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.bookingId").value("temp-booking-id"))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  void shouldRejectInvalidCreateBookingRequest() throws Exception {
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
        .perform(post("/api/v1/bookings").contentType("application/json").content(requestBody))
        .andExpect(status().isBadRequest());
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
            .status("PENDING")
            .build();

    when(getBookingUseCase.getBooking("booking-123")).thenReturn(booking);

    mockMvc
        .perform(get("/api/v1/bookings/booking-123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookingId").value("booking-123"))
        .andExpect(jsonPath("$.shopId").value("shop-1"))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }
}
