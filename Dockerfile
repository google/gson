FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project into the container
COPY . .

# Install Maven and update package index
RUN apt-get update && apt-get install -y maven

# Apply formatting fixes first
RUN mvn spotless:apply

# Build without tests
RUN mvn clean install -DskipTests

# Default command to indicate deployment automation completed successfully
# Note: Project has no executable main method, so this is just a placeholder
CMD ["echo", "Deployment automation demonstrated"]
