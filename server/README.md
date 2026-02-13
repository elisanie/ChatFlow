# Server

Spring Boot WebSocket server with message validation.

## Requirements
- Java 17+
- Maven

## Running
```bash
mvn spring-boot:run
```
Server starts on port 8080.

## Endpoints
- WebSocket: `ws://localhost:8080/chat/{roomId}`
- Health: `GET http://localhost:8080/health`

## EC2 Deployment
```bash
mvn clean package -DskipTests
scp -i key.pem target/*.jar ec2-user@<IP>:~/server.jar
# On EC2:
nohup java -jar server.jar &
```