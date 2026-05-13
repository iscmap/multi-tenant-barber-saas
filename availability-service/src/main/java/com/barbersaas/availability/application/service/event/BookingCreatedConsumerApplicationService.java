package com.barbersaas.availability.application.service.event;

import com.barbersaas.availability.application.port.in.event.ConsumeBookingCreatedUseCase;
import com.barbersaas.availability.application.port.in.validation.ValidateSlotUseCase;
import com.barbersaas.availability.application.port.out.LoadBarberAvailabilityPort;
import com.barbersaas.availability.application.port.out.reservation.ReserveBarberAvailabilityPort;
import com.barbersaas.availability.domain.model.BarberAvailability;
import com.barbersaas.shared.events.contract.BookingCreatedEvent;
import com.barbersaas.shared.events.parser.EventJsonParser;
import com.barbersaas.shared.events.parser.JacksonEventJsonParser;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Service;

@Service
public class BookingCreatedConsumerApplicationService implements ConsumeBookingCreatedUseCase {

  private final ValidateSlotUseCase validateSlotUseCase;
  private final LoadBarberAvailabilityPort loadBarberAvailabilityPort;
  private final ReserveBarberAvailabilityPort reserveBarberAvailabilityPort;
  private final EventJsonParser eventJsonParser;

  public BookingCreatedConsumerApplicationService(
      ValidateSlotUseCase validateSlotUseCase,
      LoadBarberAvailabilityPort loadBarberAvailabilityPort,
      ReserveBarberAvailabilityPort reserveBarberAvailabilityPort) {
    this.validateSlotUseCase = validateSlotUseCase;
    this.loadBarberAvailabilityPort = loadBarberAvailabilityPort;
    this.reserveBarberAvailabilityPort = reserveBarberAvailabilityPort;
    this.eventJsonParser = new JacksonEventJsonParser();
  }

  @Override
  public void consume(String payload) {
    BookingCreatedEvent event = eventJsonParser.parse(payload, BookingCreatedEvent.class);

    validateSlotUseCase.validateSlot(
        event.getShopId(),
        event.getBarberId(),
        event.getDate(),
        event.getStartTime(),
        event.getDurationMinutes());

    BarberAvailability availability =
        loadBarberAvailabilityPort
            .loadByBarberAndSlot(
                event.getShopId(),
                event.getBarberId(),
                LocalDate.parse(event.getDate()),
                LocalTime.parse(event.getStartTime()))
            .orElseThrow(
                () -> new IllegalStateException("Validated slot was not found for reservation"));

    reserveBarberAvailabilityPort.reserve(availability);
  }
}
