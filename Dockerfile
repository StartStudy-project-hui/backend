FROM openjdk:17-jdk-slim
COPY build/libs/studyProject-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","/app.jar"]
