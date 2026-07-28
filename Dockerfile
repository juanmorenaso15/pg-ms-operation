FROM maven:3.9.9-eclipse-temurin-21-alpine AS build-common
WORKDIR /build
COPY pg-lib-common/pom.xml pg-lib-common/pom.xml
COPY pg-lib-common/src pg-lib-common/src
RUN mvn -f pg-lib-common/pom.xml -q install -DskipTests

FROM maven:3.9.9-eclipse-temurin-21-alpine AS build-ms
WORKDIR /build
COPY --from=build-common /root/.m2 /root/.m2
COPY pg-ms-operation/pom.xml pg-ms-operation/pom.xml
COPY pg-ms-operation/src pg-ms-operation/src
RUN mvn -f pg-ms-operation/pom.xml -q package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build-ms /build/pg-ms-operation/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]