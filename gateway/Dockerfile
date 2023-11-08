FROM amazoncorretto:11
WORKDIR ./
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]