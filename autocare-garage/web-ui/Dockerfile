# Build the JAR first:  mvn clean package
# Then:                  docker build -t web-ui .
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S autocare && adduser -S autocare -G autocare

WORKDIR /app
COPY target/web-ui-*.jar app.jar
RUN chown -R autocare:autocare /app

USER autocare
EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
