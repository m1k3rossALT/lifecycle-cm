# ============================================================
# ecm-resource-manager — Dockerfile
# Multi-stage build: Maven build stage + lean runtime stage.
# ============================================================

# Stage 1 — Build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml ./
COPY ecm-common/pom.xml ecm-common/
COPY ecm-library-service/pom.xml ecm-library-service/
COPY ecm-resource-manager/pom.xml ecm-resource-manager/

RUN mvn dependency:go-offline -pl ecm-common,ecm-resource-manager -am -q

COPY ecm-common/src ecm-common/src
COPY ecm-resource-manager/src ecm-resource-manager/src

RUN mvn clean package -pl ecm-common,ecm-resource-manager -am -DskipTests -q

# ---------------------------------------------------------------
# Stage 2 — Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S ecm && adduser -S ecm -G ecm
USER ecm

COPY --from=build /build/ecm-resource-manager/target/ecm-resource-manager-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
