FROM jenkins/jenkins:lts

USER root

# Instalar Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Verificar instalação
RUN mvn -version

USER jenkins
