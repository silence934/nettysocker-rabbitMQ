FROM java:8
COPY /target/*.jar app.jar
ENTRYPOINT ["java", "-Dfile.encoding=utf-8","-jar", "/app.jar"]

