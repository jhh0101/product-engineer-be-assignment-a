# Base image with JDK 21
FROM eclipse-temurin:21-jdk

# Set timezone to Asia/Seoul
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Set working directory
WORKDIR /app

# Create logs directory
RUN mkdir -p /app/logs

# Copy the built JAR file
COPY build/libs/*.jar app.jar


ENTRYPOINT ["java", \
  "-Dspring.profiles.active=prod", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", \
  "app.jar"]
