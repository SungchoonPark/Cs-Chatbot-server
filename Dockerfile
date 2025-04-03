# jdk17 Image start
FROM openjdk:17

# 인자 설정
ARG JAR_FILE=build/libs/cschatbot-0.0.1-SNAPSHOT.jar

# jar파일 복제
COPY ${JAR_FILE} app.jar

# 실행 명령어
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "app.jar"]
