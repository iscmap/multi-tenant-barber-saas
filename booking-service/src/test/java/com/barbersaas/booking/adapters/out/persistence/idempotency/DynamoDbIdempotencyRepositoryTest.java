package com.barbersaas.booking.adapters.out.persistence.idempotency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.barbersaas.booking.domain.model.idempotency.IdempotencyRecord;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

class DynamoDbIdempotencyRepositoryTest {

  @Mock private DynamoDbClient dynamoDbClient;

  private DynamoDbIdempotencyRepository repository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    repository = new DynamoDbIdempotencyRepository(dynamoDbClient);
  }

  @Test
  void shouldLoadIdempotencyRecordWhenKeyExists() {

    when(dynamoDbClient.getItem(any(GetItemRequest.class)))
        .thenReturn(
            GetItemResponse.builder()
                .item(
                    Map.of(
                        "idempotencyKey",
                        AttributeValue.builder().s("idem-7401").build(),
                        "bookingId",
                        AttributeValue.builder().s("booking-1").build(),
                        "createdAt",
                        AttributeValue.builder().s("2026-08-13T19:00:00").build()))
                .build());

    Optional<IdempotencyRecord> result = repository.loadByKey("idem-7401");

    assertTrue(result.isPresent());

    assertEquals("idem-7401", result.get().getIdempotencyKey());

    assertEquals("booking-1", result.get().getBookingId());

    assertEquals(LocalDateTime.of(2026, 8, 13, 19, 0), result.get().getCreatedAt());

    verify(dynamoDbClient).getItem(any(GetItemRequest.class));
  }

  @Test
  void shouldReturnEmptyWhenKeyDoesNotExist() {

    when(dynamoDbClient.getItem(any(GetItemRequest.class)))
        .thenReturn(GetItemResponse.builder().item(Map.of()).build());

    Optional<IdempotencyRecord> result = repository.loadByKey("idem-missing");

    assertTrue(result.isEmpty());

    verify(dynamoDbClient).getItem(any(GetItemRequest.class));
  }

  @Test
  void shouldSaveIdempotencyRecord() {

    IdempotencyRecord record =
        IdempotencyRecord.builder()
            .idempotencyKey("idem-7401")
            .bookingId("booking-1")
            .createdAt(LocalDateTime.of(2026, 8, 13, 19, 0))
            .build();

    IdempotencyRecord result = repository.save(record);

    assertEquals(record, result);

    verify(dynamoDbClient).putItem(any(PutItemRequest.class));
  }
}
