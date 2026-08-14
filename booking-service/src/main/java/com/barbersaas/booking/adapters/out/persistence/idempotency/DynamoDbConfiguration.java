package com.barbersaas.booking.adapters.out.persistence.idempotency;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDbConfiguration {

  @Bean
  public DynamoDbClient dynamoDbClient(
          @Value("${spring.cloud.aws.endpoint:http://localhost:4566}") String awsEndpoint) {

    return DynamoDbClient.builder()
            .endpointOverride(URI.create(awsEndpoint))
            .region(Region.US_EAST_1)
            .credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("test", "test")))
            .build();
  }
}