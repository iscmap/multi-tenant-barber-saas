package com.barbersaas.availability.adapters.in.messaging.kafka;

import com.barbersaas.availability.application.port.in.event.ConsumeBookingCreatedUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingCreatedKafkaListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(BookingCreatedKafkaListener.class);
  private final ConsumeBookingCreatedUseCase consumeBookingCreatedUseCase;

  public BookingCreatedKafkaListener(ConsumeBookingCreatedUseCase consumeBookingCreatedUseCase) {
    this.consumeBookingCreatedUseCase = consumeBookingCreatedUseCase;
  }

  @KafkaListener(
      topics = "${barbersaas.messaging.booking-created-kafka-topic}",
      groupId = "${barbersaas.messaging.availability-kafka-consumer-group}")
  public void listen(String payload) {
    LOGGER.info("kafka_booking_created_received payload={}", payload);
    consumeBookingCreatedUseCase.consume(payload);
  }
}
