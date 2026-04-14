package com.barbersaas.shared.ownership;

import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class BookingServiceOwnedData {

  @Singular("field")
  List<String> fields;
}
