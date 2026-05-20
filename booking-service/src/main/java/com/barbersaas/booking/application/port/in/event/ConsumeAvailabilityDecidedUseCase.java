package com.barbersaas.booking.application.port.in.event;

public interface ConsumeAvailabilityDecidedUseCase {

  void consume(String payload);
}
