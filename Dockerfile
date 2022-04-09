FROM maven:3-openjdk-11-slim
RUN apt update \
    && apt install -y \
        libfreetype6 \
        fontconfig \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /usr/src/maven
