package com.barbersaas.shared.ownership;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AvailabilityServiceOwnedDataTest {

  @Test
  void shouldBuildAvailabilityServiceOwnedData() {
    AvailabilityServiceOwnedData ownedData =
        AvailabilityServiceOwnedData.builder()
            .field("barberSchedule")
            .field("reservedSlot")
            .field("decision")
            .build();

    assertEquals(3, ownedData.getFields().size());
    assertEquals("barberSchedule", ownedData.getFields().get(0));
    assertEquals("decision", ownedData.getFields().get(2));
  }
}
