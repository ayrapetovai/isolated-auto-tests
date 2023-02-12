FROM openjdk:latest
#ENV JDBC_URL="jdbc:postgresql://host.docker.internal:5432/postgres"
COPY ./dependency/app.jar app.jar
#EXPOSE 8080
CMD ["java","-jar","app.jar"]
