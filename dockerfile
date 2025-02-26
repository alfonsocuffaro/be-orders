# Use an official java runtime as a parent image
FROM openjdk:23-rc

# Set the working directory to /app
WORKDIR /app

# copy the spring boot application jar
COPY build/libs/beorders-0.0.1-SNAPSHOT.jar /app/beorders.jar

# expose the port that be-orders will use
EXPOSE 8080

# start the be-orders app with the following command
CMD ["java", "-jar", "beorders.jar"]