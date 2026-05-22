# ==========================================
# Phase 1: Build Java Project using Maven
# ==========================================
FROM maven:3.8.4-openjdk-11-slim AS build
WORKDIR /app

# Copy pom.xml and fetch dependencies (improves Docker caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the WAR file
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Phase 2: Run built WAR in Apache Tomcat 9
# ==========================================
FROM tomcat:9.0-jdk11-openjdk-slim

# Remove Tomcat's default apps to keep it clean and performant
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the built WAR from the Maven stage and rename it to ROOT.war so it serves at "/"
COPY --from=build /app/target/OnlineAuctionSystem.war /usr/local/tomcat/webapps/ROOT.war

# Expose the default Tomcat port
EXPOSE 8080

# Start Tomcat server
CMD ["catalina.sh", "run"]
