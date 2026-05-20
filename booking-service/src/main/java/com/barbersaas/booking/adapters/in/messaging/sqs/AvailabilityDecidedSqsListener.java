package com.barbersaas.booking.adapters.in.messaging.sqs;

import com.barbersaas.booking.application.port.in.event.ConsumeAvailabilityDecidedUseCase;
import io.awspring.cloud.sqs.annotation.SnsNotificationMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityDecidedSqsListener {

  private final ConsumeAvailabilityDecidedUseCase consumeAvailabilityDecidedUseCase;

  public AvailabilityDecidedSqsListener(
      ConsumeAvailabilityDecidedUseCase consumeAvailabilityDecidedUseCase) {
    this.consumeAvailabilityDecidedUseCase = consumeAvailabilityDecidedUseCase;
  }

  @SqsListener("${barbersaas.messaging.availability-decided-queue}")
  public void listen(@SnsNotificationMessage String payload) {
    consumeAvailabilityDecidedUseCase.consume(payload);
  }
}
