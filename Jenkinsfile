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
                sh './gradlew test --no-daemon'
            }

            post {
                always {
                    junit(
                        testResults: '**/build/test-results/test/*.xml',
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