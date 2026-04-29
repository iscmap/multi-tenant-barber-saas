package com.barbersaas.shared.api.problem;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ApiProblem {
  String type;
  String title;
  Integer status;
  String detail;
  String instance;
  String correlationId;
  OffsetDateTime timestamp;
}
