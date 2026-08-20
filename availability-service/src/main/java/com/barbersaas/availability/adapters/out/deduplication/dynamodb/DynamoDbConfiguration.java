package com.barbersaas.availability.adapters.out.deduplication.dynamodb;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

@Configuration
public class DynamoDbConfiguration {

  @Bean
  public DynamoDbClient dynamoDbClient(
      @Value("${spring.cloud.aws.region.static:us-east-1}") String awsRegion,
      @Value("${spring.cloud.aws.endpoint:}") String awsEndpoint) {

    DynamoDbClientBuilder builder = DynamoDbClient.builder().region(Region.of(awsRegion));

    if (StringUtils.hasText(awsEndpoint)) {
      builder.endpointOverride(URI.create(awsEndpoint));
    }

    return builder.build();
  }
}
