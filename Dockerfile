FROM gradle:6.3.0-jre8
LABEL maintainer="thirdlif2@gmail.com"

WORKDIR /home/task

ARG JAR_FILE
COPY ${JAR_FILE} task.jar

ENTRYPOINT ["java", "-jar", "task.jar"]
