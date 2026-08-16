# Build the JAR first:  mvn clean package
# Then:                  docker build -t workshop-service .
FROM eclipse-temurin:21-jre-alpine

# Numeric UID on purpose. Kubernetes runAsNonRoot cannot verify a username,
# only a UID, and refuses to start the container without one.
RUN addgroup -S -g 10001 autocare && adduser -S -u 10001 -G autocare autocare

WORKDIR /app
COPY target/workshop-service-*.jar app.jar
RUN chown -R autocare:autocare /app

USER 10001
EXPOSE 8082

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
