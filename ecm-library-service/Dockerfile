# ============================================================
# ecm-library-service — Dockerfile
# Multi-stage build: Maven build stage + lean runtime stage.
# The parent POM and ecm-common must be built before this image
# if building standalone. docker compose handles the order.
# ============================================================

# Stage 1 — Build
# Uses the full Maven image to compile and package the fat jar.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copy parent POM and module POMs first — this layer is cached
# as long as POM files don't change, avoiding dependency re-download
# on every code change.
COPY pom.xml ./
COPY ecm-common/pom.xml ecm-common/
COPY ecm-library-service/pom.xml ecm-library-service/
COPY ecm-resource-manager/pom.xml ecm-resource-manager/

# Download all dependencies (cached layer)
RUN mvn dependency:go-offline -pl ecm-common,ecm-library-service -am -q

# Copy source and build
COPY ecm-common/src ecm-common/src
COPY ecm-library-service/src ecm-library-service/src

RUN mvn clean package -pl ecm-common,ecm-library-service -am -DskipTests -q

# ---------------------------------------------------------------
# Stage 2 — Runtime
# Lean JRE image. No build tools in the final image.
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Non-root user for security
RUN addgroup -S ecm && adduser -S ecm -G ecm
USER ecm

COPY --from=build /build/ecm-library-service/target/ecm-library-service-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
