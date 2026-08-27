package com.barbersaas.availability.adapters.in.event;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.barbersaas.availability.application.port.in.event.ConsumeBookingCreatedUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookingCreatedEventController.class)
class BookingCreatedEventControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ConsumeBookingCreatedUseCase consumeBookingCreatedUseCase;

  @Test
  void shouldAcceptBookingCreatedEvent() throws Exception {
    doNothing().when(consumeBookingCreatedUseCase).consume(anyString());

    String payload =
        """
                {
                  "eventId": "evt-1",
                  "eventType": "BookingCreated",
                  "occurredAt": "2026-04-10T10:00:00Z",
                  "correlationId": "corr-1",
                  "bookingId": "booking-1",
                  "shopId": "shop-1",
                  "barberId": "barber-1",
                  "customerId": "customer-1",
                  "date": "2026-04-10",
                  "startTime": "10:00",
                  "durationMinutes": 30,
                  "serviceCode": "HAIRCUT",
                  "status": "PENDING"
                }
                """;

    mockMvc
        .perform(
            post("/api/v1/internal/events/booking-created")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.accepted").value(true))
        .andExpect(jsonPath("$.eventType").value("BookingCreated"));
  }
}
