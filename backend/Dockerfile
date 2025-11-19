FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY checkstyle.xml .
COPY formatter.xml .

RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN groupadd --system spring && useradd --system -g spring spring
USER spring

COPY --from=builder /app/target/*.jar app.jar

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD [ "curl", "-f", "http://localhost:8000/actuator/health" ] || exit 1

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]
