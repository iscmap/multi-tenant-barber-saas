package com.barbersaas.availability.application.service.event;

import com.barbersaas.availability.application.port.in.event.ConsumeBookingCreatedUseCase;
import com.barbersaas.availability.application.port.in.validation.ValidateSlotUseCase;
import com.barbersaas.shared.events.contract.BookingCreatedEvent;
import com.barbersaas.shared.events.parser.EventJsonParser;
import com.barbersaas.shared.events.parser.JacksonEventJsonParser;
import org.springframework.stereotype.Service;

@Service
public class BookingCreatedConsumerApplicationService implements ConsumeBookingCreatedUseCase {

  private final ValidateSlotUseCase validateSlotUseCase;
  private final EventJsonParser eventJsonParser;

  public BookingCreatedConsumerApplicationService(ValidateSlotUseCase validateSlotUseCase) {
    this.validateSlotUseCase = validateSlotUseCase;
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
  }
}
