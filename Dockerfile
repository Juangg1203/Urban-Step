# ============================================================
#  Etapa 1: compilar
#  Se usa una imagen con Maven + JDK 21 solo para construir el .war.
#  Esta capa no viaja al contenedor final: por eso el resultado
#  pesa mucho menos que si Maven quedara instalado en produccion.
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Se copia primero solo el pom.xml para aprovechar el cache de Docker:
# si no cambian las dependencias, Docker no vuelve a descargarlas en
# cada build, solo cuando cambia el codigo fuente.
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# ============================================================
#  Etapa 2: ejecutar
#  Imagen liviana, solo con el JRE (no el JDK completo) y el .war
#  ya compilado. Es la que realmente corre en Render.
# ============================================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# El war de Spring Boot es ejecutable por si solo (trae Tomcat embebido).
COPY --from=build /app/target/tienda-ropa.war app.war

# Render asigna el puerto en la variable de entorno PORT; hay que
# escuchar ahi, no en el 8080 fijo que se usa en desarrollo local.
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.war"]
