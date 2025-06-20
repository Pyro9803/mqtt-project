# MQTT Project Development Guidelines

This document provides essential information for developers working on the MQTT project.

## Build and Configuration Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- Docker and Docker Compose (for running the MQTT broker)

### Project Structure
The project is organized as a multi-module Maven project:
- **common**: Contains shared code, including MQTT client configuration and connection properties
- **publisher**: Publishes temperature data to the MQTT broker
- **subscriber**: Subscribes to temperature data from the MQTT broker

### Building the Project
```bash
# Build all modules
mvn clean install

# Build a specific module
mvn clean install -pl common
mvn clean install -pl publisher
mvn clean install -pl subscriber
```

### Running the Project
1. Start the MQTT broker using Docker Compose:
   ```bash
   docker-compose up -d
   ```

2. Run the publisher and subscriber applications:
   ```bash
   # Run the publisher
   mvn spring-boot:run -pl publisher

   # Run the subscriber (in a separate terminal)
   mvn spring-boot:run -pl subscriber
   ```

### Configuration
The MQTT connection is configured in the following files:
- `common/src/main/resources/application.yml`: Contains connection properties
- `common/src/main/java/com/minhhn/constants/MqttConstants.java`: Contains broker URL, client ID, and topic names

Key configuration parameters:
- Broker URL: `tcp://localhost:1883`
- Temperature topic: `sensor/temperature`
- Client ID: `client-1`
- Connection properties:
    - Automatic reconnect: `true`
    - Clean session: `true`
    - Connection timeout: `30` seconds
    - Keep alive interval: `60` seconds
    - Max in-flight messages: `100`

To modify these settings, update the appropriate files and rebuild the project.


### Running Tests
```bash
# Run all tests
mvn test

# Run tests for a specific module
mvn test -pl common
mvn test -pl publisher
mvn test -pl subscriber

# Run a specific test class
mvn test -Dtest=TemperatureTest -pl publisher
```

## Additional Development Information

### Code Style and Conventions
- Use standard Java code style
- Follow Spring Boot best practices
- Use Lombok annotations to reduce boilerplate code
- Use Java records for DTOs when appropriate

### MQTT Implementation Details
- The project uses the Eclipse Paho MQTT client library (version 1.2.5)
- QoS level 1 is used for publishing and subscribing
- Messages are retained on the broker
- Automatic reconnection is implemented for both publisher and subscriber
- The publisher generates random temperature data between 15 and 30 degrees Celsius
- The subscriber logs received temperature data
