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
    }

    post {
        success {
            echo "Pipeline completed successfully."
        }

        failure {
            echo "Pipeline failed."
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