#!/bin/sh
set -eu

export AWS_DEFAULT_REGION=us-east-1

echo "Creating SNS topic..."
BOOKING_TOPIC_ARN=$(awslocal sns create-topic \
  --name booking-events \
  --query TopicArn \
  --output text)

echo "Creating DLQ..."
BOOKING_EVENTS_DLQ_URL=$(awslocal sqs create-queue \
  --queue-name booking-events-dlq \
  --query QueueUrl \
  --output text)

BOOKING_EVENTS_DLQ_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url "$BOOKING_EVENTS_DLQ_URL" \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' \
  --output text)

echo "Creating main queue..."
BOOKING_EVENTS_QUEUE_URL=$(awslocal sqs create-queue \
  --queue-name booking-events-queue \
  --attributes "{\"RedrivePolicy\":\"{\\\"deadLetterTargetArn\\\":\\\"$BOOKING_EVENTS_DLQ_ARN\\\",\\\"maxReceiveCount\\\":\\\"3\\\"}\"}" \
  --query QueueUrl \
  --output text)

BOOKING_EVENTS_QUEUE_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url "$BOOKING_EVENTS_QUEUE_URL" \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' \
  --output text)

echo "Allowing SNS topic to publish to SQS queue..."
awslocal sqs set-queue-attributes \
  --queue-url "$BOOKING_EVENTS_QUEUE_URL" \
  --attributes "{\"Policy\":\"{\\\"Version\\\":\\\"2012-10-17\\\",\\\"Statement\\\":[{\\\"Sid\\\":\\\"Allow-SNS-SendMessage\\\",\\\"Effect\\\":\\\"Allow\\\",\\\"Principal\\\":{\\\"Service\\\":\\\"sns.amazonaws.com\\\"},\\\"Action\\\":\\\"sqs:SendMessage\\\",\\\"Resource\\\":\\\"$BOOKING_EVENTS_QUEUE_ARN\\\",\\\"Condition\\\":{\\\"ArnEquals\\\":{\\\"aws:SourceArn\\\":\\\"$BOOKING_TOPIC_ARN\\\"}}}]}\"}"

echo "Subscribing queue to topic..."
awslocal sns subscribe \
  --topic-arn "$BOOKING_TOPIC_ARN" \
  --protocol sqs \
  --notification-endpoint "$BOOKING_EVENTS_QUEUE_ARN"

echo "Creating DynamoDB table..."
awslocal dynamodb create-table \
  --table-name idempotency_keys \
  --attribute-definitions \
    AttributeName=idempotencyKey,AttributeType=S \
    AttributeName=createdAt,AttributeType=S \
  --key-schema \
    AttributeName=idempotencyKey,KeyType=HASH \
    AttributeName=createdAt,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST

echo "AWS bootstrap completed."