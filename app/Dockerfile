FROM jenkins/jenkins:lts

USER root

# Instalar dependencias del sistema
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    unzip \
    sudo \
    git \
    python3 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

# Descargar e instalar OpenJDK 17 manualmente
RUN wget -O /tmp/openjdk-17.tar.gz https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz && \
    tar -xzf /tmp/openjdk-17.tar.gz -C /opt && \
    mv /opt/jdk-17.0.2 /opt/jdk-17 && \
    rm /tmp/openjdk-17.tar.gz

ENV JAVA_HOME=/opt/jdk-17
ENV PATH=$JAVA_HOME/bin:$PATH

# Instalar Maven (después de tener Java 17)
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Instalar Docker Compose V2
RUN mkdir -p /usr/libexec/docker/cli-plugins && \
    curl -SL "https://github.com/docker/compose/releases/download/v2.24.5/docker-compose-linux-$(uname -m)" -o /usr/libexec/docker/cli-plugins/docker-compose && \
    chmod +x /usr/libexec/docker/cli-plugins/docker-compose && \
    ln -s /usr/libexec/docker/cli-plugins/docker-compose /usr/local/bin/docker-compose

# Instalar Docker CLI
RUN curl -fsSL https://get.docker.com | sh

# Instalar plugins de Jenkins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# Dar permisos sudo a jenkins
RUN echo "jenkins ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers

USER jenkins

ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false