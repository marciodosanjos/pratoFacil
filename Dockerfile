# ---------- Etapa de build (compila o jar com Maven + JDK 21) ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

# ---------- Etapa de execucao (roda o jar com JRE 21) ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# O Render injeta a variavel PORT; a aplicacao escuta nela (server.port=${PORT:8080}).
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
