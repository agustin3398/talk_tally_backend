# Use the official OpenJDK image as the base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project file to the container
COPY pom.xml .

# Download the project dependencies
RUN mvn dependency:go-offline

# Copy the project source code to the container
COPY src ./src

# Build the project using Maven
RUN mvn package

# Start with a base WildFly image
FROM jboss/wildfly:28

# Copy the built JAR file to the WildFly deployments directory
COPY --from=0 /app/target/TalkTally-HablarCuenta-1.0.war /opt/jboss/wildfly/standalone/deployments/

# Expose the port on which WildFly will run
EXPOSE 8080

# Start WildFly
CMD ["standalone.sh", "-b", "0.0.0.0"]