package com.barbersaas.availability.adapters.in.event;

import com.barbersaas.availability.application.port.in.event.ConsumeBookingCreatedUseCase;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/events")
public class BookingCreatedEventController {

  private final ConsumeBookingCreatedUseCase consumeBookingCreatedUseCase;

  public BookingCreatedEventController(ConsumeBookingCreatedUseCase consumeBookingCreatedUseCase) {
    this.consumeBookingCreatedUseCase = consumeBookingCreatedUseCase;
  }

  @PostMapping("/booking-created")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Map<String, Object> consumeBookingCreated(@RequestBody String payload) {
    consumeBookingCreatedUseCase.consume(payload);

    return Map.of("accepted", true, "eventType", "BookingCreated");
  }
}
