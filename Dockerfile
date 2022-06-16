# Build stage
FROM docker.io/library/maven:3.8.5-openjdk-17-slim AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
WORKDIR /usr/src/app
RUN java -version && mvn -v
RUN mvn --batch-mode -f /usr/src/app/pom.xml package &&\
  mvn --batch-mode -f /usr/src/app/pom.xml javadoc:javadoc

# Package stage
FROM docker.io/library/openjdk:17-jdk-slim
LABEL maintainer="Mike Millson <mmillson@redhat.com>"
USER root
ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update && \
  apt-get clean && rm -rf /var/lib/apt/lists/*

# Add garbagecat user and group
RUN groupadd -g 30001 garbagecat && \
  useradd --no-log-init -m -d /home/garbagecat -u 30001 -g 30001 garbagecat

COPY --from=build /usr/src/app/target/garbagecat-4.0.1-SNAPSHOT.jar /home/garbagecat/garbagecat.jar

# Run everything as garbagecat
USER garbagecat
WORKDIR /home/garbagecat

RUN mkdir -p /home/garbagecat/files &&\
  chown -R 30001:30001 /home/garbagecat/files

# Default home dir
ENV HOME=/home/garbagecat

ENTRYPOINT ["java", "-jar", "garbagecat.jar"]
