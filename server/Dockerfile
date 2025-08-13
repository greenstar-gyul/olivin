FROM openjdk:21
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENV TZ=Asia/Seoul
EXPOSE 3049
ENTRYPOINT ["java","-jar","app.jar"]
