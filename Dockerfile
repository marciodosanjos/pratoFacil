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
# Limita a heap a ~75% da memoria do container (ajuda no plano Free de 512MB).
# Pode ser sobrescrito definindo a variavel JAVA_OPTS no Render.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
# O Render injeta a variavel PORT; a aplicacao escuta nela (server.port=${PORT:8080}).
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
