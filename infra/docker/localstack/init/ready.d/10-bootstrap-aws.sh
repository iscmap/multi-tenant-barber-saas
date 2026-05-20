#!/bin/sh
set -eu

AWS_REGION="us-east-1"
AWS_ACCOUNT_ID="000000000000"

BOOKING_EVENTS_TOPIC="booking-events"
AVAILABILITY_EVENTS_TOPIC="availability-events"

BOOKING_CREATED_QUEUE="booking-created-queue"
AVAILABILITY_DECIDED_QUEUE="availability-decided-queue"

BOOKING_EVENTS_DLQ="booking-events-dlq"
AVAILABILITY_EVENTS_DLQ="availability-events-dlq"

echo "Creating SNS topics..."
awslocal sns create-topic --name "${BOOKING_EVENTS_TOPIC}" --region "${AWS_REGION}" >/dev/null
awslocal sns create-topic --name "${AVAILABILITY_EVENTS_TOPIC}" --region "${AWS_REGION}" >/dev/null

BOOKING_EVENTS_TOPIC_ARN="arn:aws:sns:${AWS_REGION}:${AWS_ACCOUNT_ID}:${BOOKING_EVENTS_TOPIC}"
AVAILABILITY_EVENTS_TOPIC_ARN="arn:aws:sns:${AWS_REGION}:${AWS_ACCOUNT_ID}:${AVAILABILITY_EVENTS_TOPIC}"

echo "Creating DLQs..."
awslocal sqs create-queue --queue-name "${BOOKING_EVENTS_DLQ}" --region "${AWS_REGION}" >/dev/null
awslocal sqs create-queue --queue-name "${AVAILABILITY_EVENTS_DLQ}" --region "${AWS_REGION}" >/dev/null

BOOKING_EVENTS_DLQ_URL=$(awslocal sqs get-queue-url --queue-name "${BOOKING_EVENTS_DLQ}" --region "${AWS_REGION}" --query 'QueueUrl' --output text)
AVAILABILITY_EVENTS_DLQ_URL=$(awslocal sqs get-queue-url --queue-name "${AVAILABILITY_EVENTS_DLQ}" --region "${AWS_REGION}" --query 'QueueUrl' --output text)

BOOKING_EVENTS_DLQ_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url "${BOOKING_EVENTS_DLQ_URL}" \
  --attribute-names QueueArn \
  --region "${AWS_REGION}" \
  --query 'Attributes.QueueArn' \
  --output text)

AVAILABILITY_EVENTS_DLQ_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url "${AVAILABILITY_EVENTS_DLQ_URL}" \
  --attribute-names QueueArn \
  --region "${AWS_REGION}" \
  --query 'Attributes.QueueArn' \
  --output text)

echo "Creating main queues..."
awslocal sqs create-queue --queue-name "${BOOKING_CREATED_QUEUE}" --region "${AWS_REGION}" >/dev/null
awslocal sqs create-queue --queue-name "${AVAILABILITY_DECIDED_QUEUE}" --region "${AWS_REGION}" >/dev/null

BOOKING_CREATED_QUEUE_URL=$(awslocal sqs get-queue-url --queue-name "${BOOKING_CREATED_QUEUE}" --region "${AWS_REGION}" --query 'QueueUrl' --output text)
AVAILABILITY_DECIDED_QUEUE_URL=$(awslocal sqs get-queue-url --queue-name "${AVAILABILITY_DECIDED_QUEUE}" --region "${AWS_REGION}" --query 'QueueUrl' --output text)

BOOKING_CREATED_QUEUE_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url "${BOOKING_CREATED_QUEUE_URL}" \
  --attribute-names QueueArn \
  --region "${AWS_REGION}" \
  --query 'Attributes.QueueArn' \
  --output text)

AVAILABILITY_DECIDED_QUEUE_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url "${AVAILABILITY_DECIDED_QUEUE_URL}" \
  --attribute-names QueueArn \
  --region "${AWS_REGION}" \
  --query 'Attributes.QueueArn' \
  --output text)

echo "Applying redrive policies..."
BOOKING_REDRIVE_POLICY=$(printf '{"deadLetterTargetArn":"%s","maxReceiveCount":"3"}' "${BOOKING_EVENTS_DLQ_ARN}")
AVAILABILITY_REDRIVE_POLICY=$(printf '{"deadLetterTargetArn":"%s","maxReceiveCount":"3"}' "${AVAILABILITY_EVENTS_DLQ_ARN}")

BOOKING_REDRIVE_POLICY_ESCAPED=$(printf '%s' "${BOOKING_REDRIVE_POLICY}" | sed 's/"/\\"/g')
AVAILABILITY_REDRIVE_POLICY_ESCAPED=$(printf '%s' "${AVAILABILITY_REDRIVE_POLICY}" | sed 's/"/\\"/g')

awslocal sqs set-queue-attributes \
  --queue-url "${BOOKING_CREATED_QUEUE_URL}" \
  --attributes "{\"RedrivePolicy\":\"${BOOKING_REDRIVE_POLICY_ESCAPED}\"}" \
  --region "${AWS_REGION}" >/dev/null

awslocal sqs set-queue-attributes \
  --queue-url "${AVAILABILITY_DECIDED_QUEUE_URL}" \
  --attributes "{\"RedrivePolicy\":\"${AVAILABILITY_REDRIVE_POLICY_ESCAPED}\"}" \
  --region "${AWS_REGION}" >/dev/null

echo "Applying queue policies..."
BOOKING_CREATED_QUEUE_POLICY=$(cat <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowBookingEventsTopicToSend",
      "Effect": "Allow",
      "Principal": {
        "Service": "sns.amazonaws.com"
      },
      "Action": "sqs:SendMessage",
      "Resource": "${BOOKING_CREATED_QUEUE_ARN}",
      "Condition": {
        "ArnEquals": {
          "aws:SourceArn": "${BOOKING_EVENTS_TOPIC_ARN}"
        }
      }
    }
  ]
}
EOF
)

AVAILABILITY_DECIDED_QUEUE_POLICY=$(cat <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowAvailabilityEventsTopicToSend",
      "Effect": "Allow",
      "Principal": {
        "Service": "sns.amazonaws.com"
      },
      "Action": "sqs:SendMessage",
      "Resource": "${AVAILABILITY_DECIDED_QUEUE_ARN}",
      "Condition": {
        "ArnEquals": {
          "aws:SourceArn": "${AVAILABILITY_EVENTS_TOPIC_ARN}"
        }
      }
    }
  ]
}
EOF
)

BOOKING_CREATED_QUEUE_POLICY_ESCAPED=$(printf '%s' "${BOOKING_CREATED_QUEUE_POLICY}" | tr -d '\n' | sed 's/"/\\"/g')
AVAILABILITY_DECIDED_QUEUE_POLICY_ESCAPED=$(printf '%s' "${AVAILABILITY_DECIDED_QUEUE_POLICY}" | tr -d '\n' | sed 's/"/\\"/g')

awslocal sqs set-queue-attributes \
  --queue-url "${BOOKING_CREATED_QUEUE_URL}" \
  --attributes "{\"Policy\":\"${BOOKING_CREATED_QUEUE_POLICY_ESCAPED}\"}" \
  --region "${AWS_REGION}" >/dev/null

awslocal sqs set-queue-attributes \
  --queue-url "${AVAILABILITY_DECIDED_QUEUE_URL}" \
  --attributes "{\"Policy\":\"${AVAILABILITY_DECIDED_QUEUE_POLICY_ESCAPED}\"}" \
  --region "${AWS_REGION}" >/dev/null

echo "Creating SNS subscriptions..."
awslocal sns subscribe \
  --topic-arn "${BOOKING_EVENTS_TOPIC_ARN}" \
  --protocol sqs \
  --notification-endpoint "${BOOKING_CREATED_QUEUE_ARN}" \
  --region "${AWS_REGION}" >/dev/null

awslocal sns subscribe \
  --topic-arn "${AVAILABILITY_EVENTS_TOPIC_ARN}" \
  --protocol sqs \
  --notification-endpoint "${AVAILABILITY_DECIDED_QUEUE_ARN}" \
  --region "${AWS_REGION}" >/dev/null

echo "Bootstrapping DynamoDB tables..."
awslocal dynamodb create-table \
  --table-name idempotency_keys \
  --attribute-definitions AttributeName=idempotencyKey,AttributeType=S \
  --key-schema AttributeName=idempotencyKey,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region "${AWS_REGION}" >/dev/null 2>&1 || true

awslocal dynamodb create-table \
  --table-name processed_booking_events \
  --attribute-definitions AttributeName=bookingIdEventType,AttributeType=S \
  --key-schema AttributeName=bookingIdEventType,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region "${AWS_REGION}" >/dev/null 2>&1 || true

echo "LocalStack AWS bootstrap completed."
