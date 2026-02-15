# --- Stage 1: Build (Maven) ---
# Image incl Maven und Java 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Nur pom.xml kopieren & Dependencies laden (Caching!)
COPY pom.xml .
# erstes Mal lahm, dann yay for cahche
RUN mvn dependency:go-offline

# 2. Quellcode kopieren und bauen
COPY src ./src
# -DskipTests spart Zeit & RAM beim Deployment
RUN mvn clean package -DskipTests

# --- Stage 2: Ausführen (JRE) ---
# Kleines Alpine Linux nur zum Ausführen
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# fertige JAR aus Stage 1 holen
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# RAM-Grenze direkt im Container setzen hopefully no more OOM
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]


#----------------------- wenn maven-build schon auf vm gelaufen ist------------------
# Wir brauchen kein Maven mehr, wir haben ja schon gebaut
#FROM eclipse-temurin:21-jre-alpine

#WORKDIR /app

# Wir kopieren die JAR, die dank deines "BUILD SUCCESS" auf dem Host liegt
#COPY target/*.jar app.jar

#EXPOSE 8080

#ENTRYPOINT ["java", "-jar", "app.jar"]