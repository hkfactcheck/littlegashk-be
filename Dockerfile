FROM maven:3.6.0-jdk-11-slim

WORKDIR /app

# Install backend dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build app
COPY src src
RUN mvn package -DskipTests -B
EXPOSE 80
ENTRYPOINT ["java","-jar","/app/target/littlegashk.jar"]
