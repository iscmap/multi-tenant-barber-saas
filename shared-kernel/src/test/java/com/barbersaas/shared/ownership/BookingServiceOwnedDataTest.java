package com.barbersaas.shared.ownership;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BookingServiceOwnedDataTest {

  @Test
  void shouldBuildBookingServiceOwnedData() {
    BookingServiceOwnedData ownedData =
        BookingServiceOwnedData.builder()
            .field("bookingId")
            .field("shopId")
            .field("status")
            .build();

    assertEquals(3, ownedData.getFields().size());
    assertEquals("bookingId", ownedData.getFields().get(0));
    assertEquals("status", ownedData.getFields().get(2));
  }
}
