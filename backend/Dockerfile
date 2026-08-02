# Etapa de compilación reproducible
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# El proceso vulnerable no necesita ejecutarse como root.
RUN addgroup -S tienda &&     adduser -S tienda -G tienda &&     mkdir -p /app/uploads &&     chown -R tienda:tienda /app

COPY --from=build --chown=tienda:tienda     /workspace/target/tienda-insegura.jar /app/app.jar

USER tienda
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
