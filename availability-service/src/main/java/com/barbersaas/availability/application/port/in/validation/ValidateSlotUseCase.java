package com.barbersaas.availability.application.port.in.validation;

public interface ValidateSlotUseCase {

  void validateSlot(
      String shopId, String barberId, String date, String startTime, int durationMinutes);
}
