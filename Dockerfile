FROM openjdk:17.0.8-jdk-alpine
COPY build/libs/studyProject-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","/app.jar"]
