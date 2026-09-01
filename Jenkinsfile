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