package com.barbersaas.availability.application.port.in.event;

public interface ConsumeBookingCreatedUseCase {

  void consume(String payload);
}
