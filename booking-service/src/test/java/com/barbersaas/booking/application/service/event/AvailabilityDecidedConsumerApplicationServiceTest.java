package com.barbersaas.booking.application.service.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.barbersaas.booking.adapters.out.persistence.memory.InMemoryBookingRepository;
import com.barbersaas.booking.domain.enums.BookingStatus;
import com.barbersaas.booking.domain.model.Booking;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class AvailabilityDecidedConsumerApplicationServiceTest {

  private final InMemoryBookingRepository repository = new InMemoryBookingRepository();
  private final AvailabilityDecidedConsumerApplicationService service =
      new AvailabilityDecidedConsumerApplicationService(repository, repository);

  @Test
  void shouldConfirmBookingWhenDecisionIsConfirmed() {
    Booking booking =
        Booking.builder()
            .bookingId("booking-1")
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

    repository.save(booking);

    String payload =
        """
                {
                  "eventId": "evt-1",
                  "eventType": "AvailabilityDecided",
                  "occurredAt": "2026-04-10T10:00:00Z",
                  "correlationId": "corr-1",
                  "source": "availability-service",
                  "tenantId": "shop-1",
                  "payload": {
                    "eventId": "evt-1",
                    "eventType": "AvailabilityDecided",
                    "occurredAt": "2026-04-10T10:00:00Z",
                    "correlationId": "corr-1",
                    "bookingId": "booking-1",
                    "shopId": "shop-1",
                    "barberId": "barber-1",
                    "decision": "CONFIRMED",
                    "reason": "SLOT_RESERVED"
                  }
                }
                """;

    service.consume(payload);

    Booking updatedBooking = repository.loadById("booking-1").orElseThrow();

    assertEquals(BookingStatus.CONFIRMED, updatedBooking.getStatus());
  }

  @Test
  void shouldRejectBookingWhenDecisionIsRejected() {
    Booking booking =
        Booking.builder()
            .bookingId("booking-2")
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

    repository.save(booking);

    String payload =
        """
                {
                  "eventId": "evt-2",
                  "eventType": "AvailabilityDecided",
                  "occurredAt": "2026-04-10T10:00:00Z",
                  "correlationId": "corr-2",
                  "source": "availability-service",
                  "tenantId": "shop-1",
                  "payload": {
                    "eventId": "evt-2",
                    "eventType": "AvailabilityDecided",
                    "occurredAt": "2026-04-10T10:00:00Z",
                    "correlationId": "corr-2",
                    "bookingId": "booking-2",
                    "shopId": "shop-1",
                    "barberId": "barber-1",
                    "decision": "REJECTED",
                    "reason": "Requested slot is not available"
                  }
                }
                """;

    service.consume(payload);

    Booking updatedBooking = repository.loadById("booking-2").orElseThrow();

    assertEquals(BookingStatus.REJECTED, updatedBooking.getStatus());
  }

  @Test
  void shouldThrowWhenDecisionIsUnsupported() {
    Booking booking =
        Booking.builder()
            .bookingId("booking-3")
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

    repository.save(booking);

    String payload =
        """
                {
                  "eventId": "evt-3",
                  "eventType": "AvailabilityDecided",
                  "occurredAt": "2026-04-10T10:00:00Z",
                  "correlationId": "corr-3",
                  "source": "availability-service",
                  "tenantId": "shop-1",
                  "payload": {
                    "eventId": "evt-3",
                    "eventType": "AvailabilityDecided",
                    "occurredAt": "2026-04-10T10:00:00Z",
                    "correlationId": "corr-3",
                    "bookingId": "booking-3",
                    "shopId": "shop-1",
                    "barberId": "barber-1",
                    "decision": "MAYBE",
                    "reason": "UNKNOWN"
                  }
                }
                """;

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> service.consume(payload));

    assertEquals("Unsupported availability decision: MAYBE", exception.getMessage());
  }
}
