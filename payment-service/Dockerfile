FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY  app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
