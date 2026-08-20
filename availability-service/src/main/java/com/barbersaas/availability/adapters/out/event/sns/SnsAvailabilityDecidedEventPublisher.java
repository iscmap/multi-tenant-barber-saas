package com.barbersaas.availability.adapters.out.event.sns;

import com.barbersaas.availability.application.port.out.event.PublishAvailabilityDecidedEventPort;
import com.barbersaas.availability.application.port.out.messaging.PublishMessagePort;
import com.barbersaas.shared.events.contract.AvailabilityDecidedEvent;
import com.barbersaas.shared.events.envelope.EventEnvelope;
import com.barbersaas.shared.events.parser.EventJsonParser;
import com.barbersaas.shared.events.parser.JacksonEventJsonParser;
import com.barbersaas.shared.messaging.config.MessagingProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class SnsAvailabilityDecidedEventPublisher implements PublishAvailabilityDecidedEventPort {

  private final PublishMessagePort publishMessagePort;
  private final MessagingProperties messagingProperties;
  private final EventJsonParser eventJsonParser;

  public SnsAvailabilityDecidedEventPublisher(
      PublishMessagePort publishMessagePort, MessagingProperties messagingProperties) {
    this.publishMessagePort = publishMessagePort;
    this.messagingProperties = messagingProperties;
    this.eventJsonParser = new JacksonEventJsonParser();
  }

  @Override
  public void publish(EventEnvelope<AvailabilityDecidedEvent> eventEnvelope) {
    String payload = eventJsonParser.toJson(eventEnvelope);

    publishMessagePort.publish(messagingProperties.getAvailabilityEventsTopicArn(), payload);
  }
}
