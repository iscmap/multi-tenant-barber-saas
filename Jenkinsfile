pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }

    environment {
        PROJECT_NAME = 'barber-saas'
        RELEASE_BRANCH = 'main'
        INTEGRATION_BRANCH = 'develop'

        AWS_REGION = 'us-east-1'
        AWS_PROFILE = 'barber-dev'

        BOOKING_ECR_REPOSITORY =
                'barber-saas/dev/booking-service'

        AVAILABILITY_ECR_REPOSITORY =
                'barber-saas/dev/availability-service'

        EKS_CLUSTER = 'barber-saas-dev'
        K8S_NAMESPACE = 'default'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Pipeline Metadata') {
            steps {
                script {
                    env.SHORT_COMMIT = sh(
                            script: 'git rev-parse --short=8 HEAD',
                            returnStdout: true
                    ).trim()

                    env.PIPELINE_VERSION = resolvePipelineVersion()

                    echo """
                    Project: ${env.PROJECT_NAME}
                    Branch: ${env.BRANCH_NAME ?: 'unknown'}
                    Commit: ${env.SHORT_COMMIT}
                    Version: ${env.PIPELINE_VERSION}
                    Release build: ${isReleaseBuild()}
                    """
                }
            }
        }

        stage('Gradle Build') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean assemble --no-daemon'
            }
        }

        stage('Unit Tests') {
            steps {
                sh '''
                    ./gradlew \
                        :shared-kernel:test \
                        :booking-service:unitTest \
                        :availability-service:unitTest \
                        --no-daemon
                '''
            }

            post {
                always {
                    junit(
                            testResults: '**/build/test-results/*/*.xml',
                            allowEmptyResults: true
                    )
                }
            }
        }

        stage('Formatting') {
            steps {
                sh './gradlew spotlessCheck --no-daemon'
            }
        }

        stage('Integration Tests') {
            steps {
                withCredentials([
                        string(
                                credentialsId: 'barber-ci-jwt-secret',
                                variable: 'JWT_SECRET'
                        )
                ]) {
                    sh '''
                        set -e

                        echo "Starting integration-test infrastructure..."

                        docker compose \
                            -p multi-tenant-barber-saas \
                            up -d postgres localstack kafka

                        wait_for_container() {
                            container="$1"

                            echo "Waiting for $container..."

                            for i in $(seq 1 60); do
                                status=$(docker inspect \
                                    --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' \
                                    "$container" 2>/dev/null || echo "missing")

                                if [ "$status" = "healthy" ] || [ "$status" = "running" ]; then
                                    echo "$container is ready: $status"
                                    return 0
                                fi

                                sleep 2
                            done

                            echo "$container did not become ready."
                            docker logs "$container" || true
                            return 1
                        }

                        wait_for_container barber-postgres
                        wait_for_container barber-localstack
                        wait_for_container barber-kafka

                        echo "Running booking-service integration tests..."

                        DB_URL='jdbc:postgresql://postgres:5432/barbersaas?currentSchema=booking' \
                        DB_USERNAME='postgres' \
                        DB_PASSWORD='postgres' \
                        AWS_ENDPOINT='http://localstack:4566' \
                        AWS_REGION='us-east-1' \
                        AWS_ACCESS_KEY_ID='test' \
                        AWS_SECRET_ACCESS_KEY='test' \
                        KAFKA_BOOTSTRAP_SERVERS='kafka:29092' \
                        ./gradlew :booking-service:integrationTest --no-daemon

                        echo "Running availability-service integration tests..."

                        DB_URL='jdbc:postgresql://postgres:5432/barbersaas?currentSchema=availability' \
                        DB_USERNAME='postgres' \
                        DB_PASSWORD='postgres' \
                        AWS_ENDPOINT='http://localstack:4566' \
                        AWS_REGION='us-east-1' \
                        AWS_ACCESS_KEY_ID='test' \
                        AWS_SECRET_ACCESS_KEY='test' \
                        KAFKA_BOOTSTRAP_SERVERS='kafka:29092' \
                        ./gradlew :availability-service:integrationTest --no-daemon
                    '''
                }
            }

            post {
                always {
                    junit(
                            testResults: '**/build/test-results/integrationTest/*.xml',
                            allowEmptyResults: true
                    )
                }
            }
        }

        stage('Docker Build') {
            steps {
                withCredentials([
                        string(
                                credentialsId: 'barber-ci-jwt-secret',
                                variable: 'JWT_SECRET'
                        )
                ]) {
                    sh '''
                        set -e

                        echo "Building application Docker images..."

                        docker compose build \
                            booking-service \
                            availability-service

                        docker image inspect \
                            barber-booking-service:8.4 \
                            > /dev/null

                        docker image inspect \
                            barber-availability-service:8.4 \
                            > /dev/null

                        echo "Docker images built successfully."
                    '''
                }
            }
        }

        stage('ECR Publish') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'main'
                    buildingTag()
                }
            }

            steps {
                sh '''
                    set -e

                    echo "Validating AWS authentication..."

                    AWS_ACCOUNT_ID=$(aws sts get-caller-identity \
                        --query Account \
                        --output text)

                    ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

                    BOOKING_IMAGE="${ECR_REGISTRY}/${BOOKING_ECR_REPOSITORY}:${PIPELINE_VERSION}"
                    AVAILABILITY_IMAGE="${ECR_REGISTRY}/${AVAILABILITY_ECR_REPOSITORY}:${PIPELINE_VERSION}"

                    echo "Authenticating Docker with ECR..."

                    aws ecr get-login-password \
                        --region "$AWS_REGION" |
                        docker login \
                            --username AWS \
                            --password-stdin \
                            "$ECR_REGISTRY"

                    echo "Tagging images..."

                    docker tag \
                        barber-booking-service:8.4 \
                        "$BOOKING_IMAGE"

                    docker tag \
                        barber-availability-service:8.4 \
                        "$AVAILABILITY_IMAGE"

                    echo "Pushing booking-service..."

                    docker push "$BOOKING_IMAGE"

                    echo "Pushing availability-service..."

                    docker push "$AVAILABILITY_IMAGE"

                    echo "Verifying ECR images..."

                    aws ecr describe-images \
                        --repository-name "$BOOKING_ECR_REPOSITORY" \
                        --image-ids "imageTag=$PIPELINE_VERSION" \
                        --region "$AWS_REGION" \
                        > /dev/null

                    aws ecr describe-images \
                        --repository-name "$AVAILABILITY_ECR_REPOSITORY" \
                        --image-ids "imageTag=$PIPELINE_VERSION" \
                        --region "$AWS_REGION" \
                        > /dev/null

                    docker logout "$ECR_REGISTRY"

                    echo "ECR publish completed successfully."
                '''
            }
        }

        stage('EKS Deploy') {
            when {
                branch 'develop'
            }

            steps {
                sh '''
            set -e

            echo "Configuring access to EKS..."

            aws eks update-kubeconfig \
                --name "$EKS_CLUSTER" \
                --region "$AWS_REGION"

            echo "Verifying EKS connectivity..."

            kubectl get nodes

            echo "Resolving ECR registry..."

            AWS_ACCOUNT_ID=$(aws sts get-caller-identity \
                --query Account \
                --output text)

            ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

            BOOKING_IMAGE="${ECR_REGISTRY}/${BOOKING_ECR_REPOSITORY}:${PIPELINE_VERSION}"
            AVAILABILITY_IMAGE="${ECR_REGISTRY}/${AVAILABILITY_ECR_REPOSITORY}:${PIPELINE_VERSION}"

            echo "Deploying booking-service version ${PIPELINE_VERSION}..."

            kubectl set image \
                deployment/booking-service \
                booking-service="$BOOKING_IMAGE" \
                --namespace "$K8S_NAMESPACE"

            echo "Deploying availability-service version ${PIPELINE_VERSION}..."

            kubectl set image \
                deployment/availability-service \
                availability-service="$AVAILABILITY_IMAGE" \
                --namespace "$K8S_NAMESPACE"

            echo "Waiting for booking-service rollout..."

            kubectl rollout status \
                deployment/booking-service \
                --namespace "$K8S_NAMESPACE" \
                --timeout=240s

            echo "Waiting for availability-service rollout..."

            kubectl rollout status \
                deployment/availability-service \
                --namespace "$K8S_NAMESPACE" \
                --timeout=240s

            echo "EKS deployment completed successfully."
        '''
            }

            post {
                failure {
                    sh '''
                echo "Deployment failed. Rolling back..."

                kubectl rollout undo \
                    deployment/booking-service \
                    --namespace "$K8S_NAMESPACE" \
                    || true

                kubectl rollout undo \
                    deployment/availability-service \
                    --namespace "$K8S_NAMESPACE" \
                    || true

                echo "Waiting for rollback..."

                kubectl rollout status \
                    deployment/booking-service \
                    --namespace "$K8S_NAMESPACE" \
                    --timeout=240s \
                    || true

                kubectl rollout status \
                    deployment/availability-service \
                    --namespace "$K8S_NAMESPACE" \
                    --timeout=240s \
                    || true

                echo "Rollback attempt completed."
            '''
                }
            }
        }

        stage('Deployment Verification') {
            when {
                branch 'develop'
            }

            steps {
                sh '''
            set -e

            echo "Resolving expected images..."

            AWS_ACCOUNT_ID=$(aws sts get-caller-identity \
                --query Account \
                --output text)

            ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

            EXPECTED_BOOKING_IMAGE="${ECR_REGISTRY}/${BOOKING_ECR_REPOSITORY}:${PIPELINE_VERSION}"
            EXPECTED_AVAILABILITY_IMAGE="${ECR_REGISTRY}/${AVAILABILITY_ECR_REPOSITORY}:${PIPELINE_VERSION}"

            echo "Reading deployed images..."

            ACTUAL_BOOKING_IMAGE=$(kubectl get deployment booking-service \
                --namespace "$K8S_NAMESPACE" \
                -o jsonpath='{.spec.template.spec.containers[?(@.name=="booking-service")].image}')

            ACTUAL_AVAILABILITY_IMAGE=$(kubectl get deployment availability-service \
                --namespace "$K8S_NAMESPACE" \
                -o jsonpath='{.spec.template.spec.containers[?(@.name=="availability-service")].image}')

            echo "Verifying booking-service image..."

            if [ "$ACTUAL_BOOKING_IMAGE" != "$EXPECTED_BOOKING_IMAGE" ]; then
                echo "Unexpected booking-service image."
                echo "Expected: $EXPECTED_BOOKING_IMAGE"
                echo "Actual:   $ACTUAL_BOOKING_IMAGE"
                exit 1
            fi

            echo "Verifying availability-service image..."

            if [ "$ACTUAL_AVAILABILITY_IMAGE" != "$EXPECTED_AVAILABILITY_IMAGE" ]; then
                echo "Unexpected availability-service image."
                echo "Expected: $EXPECTED_AVAILABILITY_IMAGE"
                echo "Actual:   $ACTUAL_AVAILABILITY_IMAGE"
                exit 1
            fi

            echo "Verifying pod readiness..."

            kubectl wait \
                --for=condition=Ready \
                pod \
                -l app=booking-service \
                --namespace "$K8S_NAMESPACE" \
                --timeout=120s

            kubectl wait \
                --for=condition=Ready \
                pod \
                -l app=availability-service \
                --namespace "$K8S_NAMESPACE" \
                --timeout=120s

            BOOKING_PF_PID=""
            AVAILABILITY_PF_PID=""

            cleanup_port_forward() {
                if [ -n "$BOOKING_PF_PID" ]; then
                    kill "$BOOKING_PF_PID" 2>/dev/null || true
                fi

                if [ -n "$AVAILABILITY_PF_PID" ]; then
                    kill "$AVAILABILITY_PF_PID" 2>/dev/null || true
                fi
            }

            trap cleanup_port_forward EXIT

            echo "Starting booking-service port-forward..."

            kubectl port-forward \
                deployment/booking-service \
                18081:8081 \
                --namespace "$K8S_NAMESPACE" \
                > /tmp/booking-port-forward.log 2>&1 &

            BOOKING_PF_PID=$!

            echo "Starting availability-service port-forward..."

            kubectl port-forward \
                deployment/availability-service \
                18082:8082 \
                --namespace "$K8S_NAMESPACE" \
                > /tmp/availability-port-forward.log 2>&1 &

            AVAILABILITY_PF_PID=$!

            echo "Waiting for port-forwards..."

            sleep 5

            echo "Calling booking-service readiness endpoint..."

            curl \
                --fail \
                --silent \
                --show-error \
                http://127.0.0.1:18081/readyz \
                > /dev/null

            echo "booking-service readiness verification passed."

            echo "Calling availability-service readiness endpoint..."

            curl \
                --fail \
                --silent \
                --show-error \
                http://127.0.0.1:18082/readyz \
                > /dev/null

            echo "availability-service readiness verification passed."

            echo "Deployment verification completed successfully."
        '''
            }

            post {
                failure {
                    sh '''
                echo "Deployment verification failed. Rolling back..."

                kubectl rollout undo \
                    deployment/booking-service \
                    --namespace "$K8S_NAMESPACE" \
                    || true

                kubectl rollout undo \
                    deployment/availability-service \
                    --namespace "$K8S_NAMESPACE" \
                    || true

                echo "Waiting for rollback..."

                kubectl rollout status \
                    deployment/booking-service \
                    --namespace "$K8S_NAMESPACE" \
                    --timeout=240s \
                    || true

                kubectl rollout status \
                    deployment/availability-service \
                    --namespace "$K8S_NAMESPACE" \
                    --timeout=240s \
                    || true

                echo "Rollback attempt completed."
            '''
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully."
        }

        failure {
            echo "Pipeline failed because a quality gate did not pass."
        }

        cleanup {
            cleanWs()
        }
    }
}

String resolvePipelineVersion() {
    if (env.TAG_NAME?.matches(/^v\d+\.\d+\.\d+$/)) {
        return env.TAG_NAME.substring(1)
    }

    String branch = env.BRANCH_NAME ?: 'unknown'

    String normalizedBranch =
            branch
                    .toLowerCase()
                    .replaceAll('[^a-z0-9.-]', '-')

    return "${normalizedBranch}-${env.SHORT_COMMIT}"
}

boolean isReleaseBuild() {
    return env.TAG_NAME?.matches(/^v\d+\.\d+\.\d+$/) == true
}