package com.barbersaas.availability.application.service.event;

import com.barbersaas.availability.application.factory.AvailabilityEventFactory;
import com.barbersaas.availability.application.port.in.event.ConsumeBookingCreatedUseCase;
import com.barbersaas.availability.application.port.in.validation.ValidateSlotUseCase;
import com.barbersaas.availability.application.port.out.LoadBarberAvailabilityPort;
import com.barbersaas.availability.application.port.out.deduplication.LoadProcessedBookingEventPort;
import com.barbersaas.availability.application.port.out.deduplication.SaveProcessedBookingEventPort;
import com.barbersaas.availability.application.port.out.event.PublishAvailabilityDecidedEventPort;
import com.barbersaas.availability.application.port.out.reservation.ReserveBarberAvailabilityPort;
import com.barbersaas.availability.domain.exception.SlotValidationException;
import com.barbersaas.availability.domain.model.BarberAvailability;
import com.barbersaas.availability.domain.model.event.ProcessedBookingEvent;
import com.barbersaas.shared.events.contract.AvailabilityDecidedEvent;
import com.barbersaas.shared.events.contract.BookingCreatedEvent;
import com.barbersaas.shared.events.envelope.EventEnvelope;
import com.barbersaas.shared.events.parser.EventJsonParser;
import com.barbersaas.shared.events.parser.JacksonEventJsonParser;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.stereotype.Service;

@Service
public class BookingCreatedConsumerApplicationService implements ConsumeBookingCreatedUseCase {

  private final ValidateSlotUseCase validateSlotUseCase;
  private final LoadBarberAvailabilityPort loadBarberAvailabilityPort;
  private final ReserveBarberAvailabilityPort reserveBarberAvailabilityPort;
  private final PublishAvailabilityDecidedEventPort publishAvailabilityDecidedEventPort;
  private final AvailabilityEventFactory availabilityEventFactory;
  private final LoadProcessedBookingEventPort loadProcessedBookingEventPort;
  private final SaveProcessedBookingEventPort saveProcessedBookingEventPort;
  private final EventJsonParser eventJsonParser;

  public BookingCreatedConsumerApplicationService(
      ValidateSlotUseCase validateSlotUseCase,
      LoadBarberAvailabilityPort loadBarberAvailabilityPort,
      ReserveBarberAvailabilityPort reserveBarberAvailabilityPort,
      PublishAvailabilityDecidedEventPort publishAvailabilityDecidedEventPort,
      AvailabilityEventFactory availabilityEventFactory,
      LoadProcessedBookingEventPort loadProcessedBookingEventPort,
      SaveProcessedBookingEventPort saveProcessedBookingEventPort) {
    this.validateSlotUseCase = validateSlotUseCase;
    this.loadBarberAvailabilityPort = loadBarberAvailabilityPort;
    this.reserveBarberAvailabilityPort = reserveBarberAvailabilityPort;
    this.publishAvailabilityDecidedEventPort = publishAvailabilityDecidedEventPort;
    this.availabilityEventFactory = availabilityEventFactory;
    this.loadProcessedBookingEventPort = loadProcessedBookingEventPort;
    this.saveProcessedBookingEventPort = saveProcessedBookingEventPort;
    this.eventJsonParser = new JacksonEventJsonParser();
  }

  @Override
  public void consume(String payload) {
    EventEnvelope<BookingCreatedEvent> envelope =
        eventJsonParser.parseEventEnvelope(payload, BookingCreatedEvent.class);

    BookingCreatedEvent event = envelope.getPayload();

    boolean alreadyProcessed =
        loadProcessedBookingEventPort
            .loadByBookingIdAndEventType(event.getBookingId(), event.getEventType())
            .isPresent();

    if (alreadyProcessed) {
      return;
    }

    try {
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

      EventEnvelope<AvailabilityDecidedEvent> confirmedEvent =
          availabilityEventFactory.buildConfirmedEvent(event);

      publishAvailabilityDecidedEventPort.publish(confirmedEvent);

      saveProcessedEvent(event);

    } catch (SlotValidationException exception) {
      EventEnvelope<AvailabilityDecidedEvent> rejectedEvent =
          availabilityEventFactory.buildRejectedEvent(event, exception.getMessage());

      publishAvailabilityDecidedEventPort.publish(rejectedEvent);

      saveProcessedEvent(event);
    }
  }

  private void saveProcessedEvent(BookingCreatedEvent event) {
    ProcessedBookingEvent processedBookingEvent =
        ProcessedBookingEvent.builder()
            .bookingId(event.getBookingId())
            .eventType(event.getEventType())
            .processedAt(LocalDateTime.now())
            .build();

    saveProcessedBookingEventPort.save(processedBookingEvent);
  }
}
