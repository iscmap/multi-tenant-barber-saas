package com.barbersaas.shared.ownership;

import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class AvailabilityServiceOwnedData {

  @Singular("field")
  List<String> fields;
}
