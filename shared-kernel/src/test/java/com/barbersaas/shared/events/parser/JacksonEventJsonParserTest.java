package com.barbersaas.shared.events.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.barbersaas.shared.events.contract.AvailabilityDecidedEvent;
import com.barbersaas.shared.events.contract.BookingCreatedEvent;
import org.junit.jupiter.api.Test;

class JacksonEventJsonParserTest {

  private final EventJsonParser parser = new JacksonEventJsonParser();

  @Test
  void shouldParseBookingCreatedEventFromJson() {
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
                  "startTime": "10:00:00",
                  "durationMinutes": 30,
                  "serviceCode": "HAIRCUT",
                  "status": "PENDING"
                }
                """;

    BookingCreatedEvent event = parser.parse(payload, BookingCreatedEvent.class);

    assertEquals("evt-1", event.getEventId());
    assertEquals("BookingCreated", event.getEventType());
    assertEquals("booking-1", event.getBookingId());
    assertEquals("PENDING", event.getStatus());
  }

  @Test
  void shouldSerializeAvailabilityDecidedEventToJson() {
    AvailabilityDecidedEvent event =
        AvailabilityDecidedEvent.builder()
            .eventId("evt-2")
            .eventType("AvailabilityDecided")
            .occurredAt("2026-04-10T10:00:05Z")
            .correlationId("corr-1")
            .bookingId("booking-1")
            .shopId("shop-1")
            .barberId("barber-1")
            .decision("CONFIRMED")
            .reason("SLOT_RESERVED")
            .build();

    String json = parser.toJson(event);

    assertTrue(json.contains("\"eventType\":\"AvailabilityDecided\""));
    assertTrue(json.contains("\"bookingId\":\"booking-1\""));
    assertTrue(json.contains("\"decision\":\"CONFIRMED\""));
  }

  @Test
  void shouldFailForInvalidJson() {
    String invalidPayload = "{ invalid-json }";

    try {
      parser.parse(invalidPayload, BookingCreatedEvent.class);
    } catch (IllegalArgumentException exception) {
      assertTrue(exception.getMessage().contains("Failed to parse event payload"));
      return;
    }

    throw new AssertionError("Expected IllegalArgumentException to be thrown");
  }
}
