# Build stage
FROM docker.io/library/maven:3.8.5-openjdk-17-slim AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
COPY settings.xml /usr/src/app
WORKDIR /usr/src/app
RUN java -version && mvn -v
RUN mvn --batch-mode -s /usr/src/app/settings.xml -f /usr/src/app/pom.xml package &&\
  mvn --batch-mode -s /usr/src/app/settings.xml -f /usr/src/app/pom.xml javadoc:javadoc

# Package stage
FROM docker.io/library/eclipse-temurin:17
LABEL maintainer="Mike Millson <mmillson@redhat.com>"
RUN apt-get update && \
  apt-get clean && rm -rf /var/lib/apt/lists/*

# Add garbagecat user and group
RUN groupadd -g 1001 garbagecat && \
  useradd --no-log-init -m -d /home/garbagecat -u 1001 -g 1001 garbagecat

COPY --from=build /usr/src/app/target/garbagecat-*.jar /home/garbagecat/garbagecat.jar

# Run everything as garbagecat
USER 1001
WORKDIR /home/garbagecat

RUN mkdir -p /home/garbagecat/files &&\
  chown -R 1001:1001 /home/garbagecat/files

# Default home dir
ENV HOME=/home/garbagecat

ENTRYPOINT ["java", "-jar", "garbagecat.jar"]
