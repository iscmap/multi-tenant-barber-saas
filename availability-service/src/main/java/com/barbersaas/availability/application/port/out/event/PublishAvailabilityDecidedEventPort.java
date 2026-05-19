package com.barbersaas.availability.application.port.out.event;

import com.barbersaas.shared.events.contract.AvailabilityDecidedEvent;
import com.barbersaas.shared.events.envelope.EventEnvelope;

public interface PublishAvailabilityDecidedEventPort {

  void publish(EventEnvelope<AvailabilityDecidedEvent> eventEnvelope);
}
