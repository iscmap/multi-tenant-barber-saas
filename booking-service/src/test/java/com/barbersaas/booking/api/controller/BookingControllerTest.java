package com.barbersaas.booking.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void shouldCreateBookingContractResponse() throws Exception {
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
    mockMvc
        .perform(get("/api/v1/bookings/booking-123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookingId").value("booking-123"))
        .andExpect(jsonPath("$.shopId").value("shop-1"))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }
}
