pipeline {
    agent none

    environment {
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
        DOCKER_IMAGE = 'order-service'
        DOCKER_TAG = "${BUILD_NUMBER}-${GIT_COMMIT.take(7)}"
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            agent any
            steps {
                checkout scm
                sh 'java -version'
                sh 'mvn -version'
            }
        }

        stage('Build') {
            agent {
                docker {
                    image 'maven:3.9-eclipse-temurin-17-alpine'
                    args '-v $HOME/.m2:/root/.m2'
                }
            }
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Unit Tests') {
            agent {
                docker {
                    image 'maven:3.9-eclipse-temurin-17-alpine'
                    args '-v $HOME/.m2:/root/.m2'
                }
            }
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    jacoco execPattern: 'target/jacoco.exec'
                }
            }
        }

        stage('Integration Tests') {
            agent any
            steps {
                script {
                    // Sobe infraestrutura com Docker Compose
                    sh 'docker-compose up -d postgres kafka rabbitmq'
                    sh 'sleep 30'

                    // Executa testes de integração
                    docker.image('maven:3.9-eclipse-temurin-17-alpine').inside(
                        "-v \${HOME}/.m2:/root/.m2 --network host"
                    ) {
                        sh '''
                            export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/orders_db
                            export SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
                            export SPRING_RABBITMQ_HOST=localhost
                            mvn verify -P integration-tests
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

        stage('Static Analysis') {
            agent {
                docker {
                    image 'maven:3.9-eclipse-temurin-17-alpine'
                    args '-v $HOME/.m2:/root/.m2'
                }
            }
            steps {
                sh 'mvn sonar:sonar -Dsonar.host.url=$SONAR_URL -Dsonar.token=$SONAR_TOKEN'
            }
        }

        stage('Build Docker Image') {
            agent any
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    // Build do JAR
                    docker.image('maven:3.9-eclipse-temurin-17-alpine').inside(
                        '-v ${HOME}/.m2:/root/.m2'
                    ) {
                        sh 'mvn clean package -DskipTests'
                    }

                    // Build e push da imagem Docker
                    def app = docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")

                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials') {
                        app.push()
                        app.push('latest')
                    }
                }
            }
        }

        stage('Deploy Staging') {
            agent any
            when {
                branch 'develop'
            }
            steps {
                script {
                    sh '''
                        echo "Deploying to STAGING..."
                        # Exemplo para ECS:
                        # aws ecs update-service --cluster staging --service order-service --force-new-deployment

                        # Exemplo para Kubernetes:
                        # kubectl set image deployment/order-service \
                        #   order-service=${DOCKER_IMAGE}:${DOCKER_TAG} -n staging
                    '''
                }
            }
        }

        stage('Deploy Production') {
            agent any
            when {
                branch 'main'
            }
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    input message: 'Deploy to Production?', ok: 'Deploy'
                }
                script {
                    sh '''
                        echo "Deploying to PRODUCTION..."
                        # Comandos de deploy em produção
                        # aws ecs update-service --cluster production --service order-service --force-new-deployment
                    '''
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            notifyBuild('SUCCESS')
        }
        failure {
            notifyBuild('FAILURE')
        }
    }
}

def notifyBuild(String status) {
    echo "Build ${status}: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    // Integração com Slack/Teams
    // slackSend color: status == 'SUCCESS' ? 'good' : 'danger',
    //           message: "${status}: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
}