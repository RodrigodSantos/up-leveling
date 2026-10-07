# Etapa 1: compila o projeto (a imagem do Maven só existe durante o build)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# As dependências vêm antes do código: enquanto o pom.xml não muda, o Docker reaproveita esta camada
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

# Etapa 2: só o necessário para rodar (JRE enxuto, sem Maven nem código-fonte)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Roda com um usuário comum, não como root
RUN addgroup -S app && adduser -S app -G app
USER app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Ajustes para caber em 512 MB (plano gratuito): heap proporcional à memória do container,
# GC mais leve e compilação JIT mais simples (sobe mais rápido e gasta menos memória)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
ENTRYPOINT ["java", "-jar", "app.jar"]
