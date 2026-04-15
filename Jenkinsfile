pipeline {
    agent any

    environment {
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
        DOCKER_IMAGE = 'order-service'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.DOCKER_TAG = "${BUILD_NUMBER}-${GIT_COMMIT.take(7)}"
                }
                sh 'java -version'
                sh 'chmod +x maven-build.sh'
            }
        }

        stage('Build') {
            steps {
                sh './maven-build.sh clean compile'
            }
        }

        stage('Unit Tests') {
            steps {
                sh './maven-build.sh test'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build JAR') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                sh './maven-build.sh clean package -DskipTests'
            }
        }

        stage('Deploy Staging') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Deploying to STAGING...'
            }
        }

        stage('Deploy Production') {
            when {
                branch 'main'
            }
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    input message: 'Deploy to Production?', ok: 'Deploy'
                }
                echo 'Deploying to PRODUCTION...'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo "Build SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
        }
        failure {
            echo "Build FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
        }
    }
}
