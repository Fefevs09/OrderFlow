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
                sh 'docker --version'
            }
        }

        stage('Build') {
            steps {
                script {
                    docker.image('maven:3.9-eclipse-temurin-17-alpine').inside('-v ${HOME}/.m2:/root/.m2') {
                        sh 'mvn clean compile'
                    }
                }
            }
        }

        stage('Unit Tests') {
            steps {
                script {
                    docker.image('maven:3.9-eclipse-temurin-17-alpine').inside('-v ${HOME}/.m2:/root/.m2') {
                        sh 'mvn test'
                    }
                }
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Integration Tests') {
            steps {
                sh 'docker-compose up -d postgres kafka rabbitmq'
                sh 'sleep 30'
                script {
                    docker.image('maven:3.9-eclipse-temurin-17-alpine').inside('-v ${HOME}/.m2:/root/.m2 --network host') {
                        sh '''
                            export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/orders_db
                            export SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
                            export SPRING_RABBITMQ_HOST=localhost
                            mvn verify -P integration-tests || true
                        '''
                    }
                }
            }
            post {
                always {
                    sh 'docker-compose down -v || true'
                    junit testResults: 'target/failsafe-reports/*.xml', allowEmptyResults: true
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
                script {
                    docker.image('maven:3.9-eclipse-temurin-17-alpine').inside('-v ${HOME}/.m2:/root/.m2') {
                        sh 'mvn clean package -DskipTests'
                    }
                }
            }
        }

        stage('Build Docker Image') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
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
