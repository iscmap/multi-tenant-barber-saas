package com.barbersaas.shared.api.problem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

public class ApiProblemTest {

  @Test
  void shouldBuildApiProblem() {
    OffsetDateTime timestamp = OffsetDateTime.parse("2026-04-10T10:00:00Z");

    ApiProblem problem =
        ApiProblem.builder()
            .type("type")
            .title("title")
            .status(400)
            .detail("detail")
            .instance("/api/v1/bookings")
            .correlationId("corr-1")
            .timestamp(timestamp)
            .build();

    assertEquals("type", problem.getType());
    assertEquals("title", problem.getTitle());
    assertEquals(400, problem.getStatus());
    assertEquals("corr-1", problem.getCorrelationId());
  }
}
