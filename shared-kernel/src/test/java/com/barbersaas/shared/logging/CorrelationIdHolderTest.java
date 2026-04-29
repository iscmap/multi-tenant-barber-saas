package com.barbersaas.shared.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CorrelationIdHolderTest {

  @Test
  void shouldSetGetAndClearCorrelationId() {
    CorrelationIdHolder.set("corr-123");

    assertEquals("corr-123", CorrelationIdHolder.get());

    CorrelationIdHolder.clear();

    assertNull(CorrelationIdHolder.get());
  }
}
