package com.barbersaas.booking.adapters.out.persistence.idempotency;

import com.barbersaas.booking.application.port.out.idempotency.LoadIdempotencyRecordPort;
import com.barbersaas.booking.application.port.out.idempotency.SaveIdempotencyRecordPort;
import com.barbersaas.booking.domain.model.idempotency.IdempotencyRecord;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@Repository
public class DynamoDbIdempotencyRepository
    implements LoadIdempotencyRecordPort, SaveIdempotencyRecordPort {

  private static final String TABLE_NAME = "idempotency_keys";

  private static final String IDEMPOTENCY_KEY = "idempotencyKey";
  private static final String BOOKING_ID = "bookingId";
  private static final String CREATED_AT = "createdAt";

  private final DynamoDbClient dynamoDbClient;

  public DynamoDbIdempotencyRepository(DynamoDbClient dynamoDbClient) {
    this.dynamoDbClient = dynamoDbClient;
  }

  @Override
  public Optional<IdempotencyRecord> loadByKey(String idempotencyKey) {

    GetItemRequest request =
        GetItemRequest.builder()
            .tableName(TABLE_NAME)
            .key(Map.of(IDEMPOTENCY_KEY, AttributeValue.builder().s(idempotencyKey).build()))
            .consistentRead(true)
            .build();

    Map<String, AttributeValue> item = dynamoDbClient.getItem(request).item();

    if (item == null || item.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(
        IdempotencyRecord.builder()
            .idempotencyKey(item.get(IDEMPOTENCY_KEY).s())
            .bookingId(item.get(BOOKING_ID).s())
            .createdAt(LocalDateTime.parse(item.get(CREATED_AT).s()))
            .build());
  }

  @Override
  public IdempotencyRecord save(IdempotencyRecord record) {

    Map<String, AttributeValue> item =
        Map.of(
            IDEMPOTENCY_KEY,
            AttributeValue.builder().s(record.getIdempotencyKey()).build(),
            BOOKING_ID,
            AttributeValue.builder().s(record.getBookingId()).build(),
            CREATED_AT,
            AttributeValue.builder().s(record.getCreatedAt().toString()).build());

    PutItemRequest request = PutItemRequest.builder().tableName(TABLE_NAME).item(item).build();

    dynamoDbClient.putItem(request);

    return record;
  }
}
