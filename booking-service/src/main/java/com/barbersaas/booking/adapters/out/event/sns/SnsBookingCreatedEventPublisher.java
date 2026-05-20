package com.barbersaas.booking.adapters.out.event.sns;

import com.barbersaas.booking.application.port.out.event.PublishBookingCreatedEventPort;
import com.barbersaas.booking.application.port.out.messaging.PublishMessagePort;
import com.barbersaas.shared.events.contract.BookingCreatedEvent;
import com.barbersaas.shared.events.envelope.EventEnvelope;
import com.barbersaas.shared.events.parser.EventJsonParser;
import com.barbersaas.shared.events.parser.JacksonEventJsonParser;
import com.barbersaas.shared.messaging.config.MessagingProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class SnsBookingCreatedEventPublisher implements PublishBookingCreatedEventPort {

  private final PublishMessagePort publishMessagePort;
  private final MessagingProperties messagingProperties;
  private final EventJsonParser eventJsonParser;

  public SnsBookingCreatedEventPublisher(
      PublishMessagePort publishMessagePort, MessagingProperties messagingProperties) {
    this.publishMessagePort = publishMessagePort;
    this.messagingProperties = messagingProperties;
    this.eventJsonParser = new JacksonEventJsonParser();
  }

  @Override
  public void publish(EventEnvelope<BookingCreatedEvent> eventEnvelope) {
    String payload = eventJsonParser.toJson(eventEnvelope);
    publishMessagePort.publish(messagingProperties.getBookingEventsTopic(), payload);
  }
}
