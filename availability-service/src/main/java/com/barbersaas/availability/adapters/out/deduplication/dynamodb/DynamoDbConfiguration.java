package com.barbersaas.availability.adapters.out.deduplication.dynamodb;

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
      @Value("${spring.cloud.aws.endpoint}") String awsEndpoint,
      @Value("${spring.cloud.aws.region.static}") String awsRegion,
      @Value("${spring.cloud.aws.credentials.access-key}") String accessKey,
      @Value("${spring.cloud.aws.credentials.secret-key}") String secretKey) {

    return DynamoDbClient.builder()
        .endpointOverride(URI.create(awsEndpoint))
        .region(Region.of(awsRegion))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .build();
  }
}
