package com.barbersaas.availability.adapters.in.messaging.sqs;

import com.barbersaas.availability.application.port.in.event.ConsumeBookingCreatedUseCase;
import io.awspring.cloud.sqs.annotation.SnsNotificationMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class BookingCreatedSqsListener {
  private final ConsumeBookingCreatedUseCase consumeBookingCreatedUseCase;

  public BookingCreatedSqsListener(ConsumeBookingCreatedUseCase consumeBookingCreatedUseCase) {
    this.consumeBookingCreatedUseCase = consumeBookingCreatedUseCase;
  }

  @SqsListener("${barbersaas.messaging.booking-created-queue}")
  public void listen(@SnsNotificationMessage String payload) {
    consumeBookingCreatedUseCase.consume(payload);
  }
}
