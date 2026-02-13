# CS6650 Assignment 1 – ChatFlow

A WebSocket-based chat server with a multithreaded load-testing client developed for CS6650 Assignment 1.

## Repository Structure

```
├── server/          - Spring Boot WebSocket server
├── client-part1/    - Basic load testing client
├── client-part2/    - Client with latency analysis and statistics
└── results/         - Test results, CSV data, and charts
```

## Server

### Requirements
- Java 17+
- Maven

### Running Locally
```bash
cd server
mvn spring-boot:run
```
Server starts on port 8080.
- Health check: `GET http://localhost:8080/health`
- WebSocket endpoint: `ws://localhost:8080/chat/{roomId}`

### EC2 Deployment
```bash
cd server
mvn clean package -DskipTests
scp -i your-key.pem target/server-0.0.1-SNAPSHOT.jar ec2-user@<EC2-IP>:~/server.jar
# On EC2:
nohup java -jar server.jar &
```
Ensure EC2 Security Group allows inbound TCP traffic on port 8080 and SSH on port 22.

WebSocket endpoint on EC2: `ws://<EC2-IP>:8080/chat/{roomId}`

## Client Part 1 (Basic Load Test)

### Requirements
- Java 17+
- Maven
- Server running on port 8080

### Running
```bash
cd client-part1
mvn clean package -DskipTests
java -cp target/client-part1-0.0.1-SNAPSHOT.jar edu.neu.cs6650.client.LoadTestClient
```

### Configuration
Edit `SERVER_URL` in `WarmupPhase.java` and `MainPhase.java` to point to your server.
- Warmup: 32 threads × 1,000 messages
- Main: 100 threads × remaining messages
- Total: 500,000 messages

## Client Part 2 (With Performance Analysis)

### Requirements
- Java 17+
- Maven
- Python 3 with matplotlib (for chart)
- Server running on port 8080

### Running
```bash
cd client-part2
mvn clean package -DskipTests
java -cp target/client-part2-0.0.1-SNAPSHOT.jar edu.neu.cs6650.client.LoadTestClient
```

### Generate Chart
```bash
python3 plot.py
```

### Configuration
Edit `SERVER_URL` in `WarmupPhase.java` and `MainPhase.java` to point to your server.

### Output
- Console: latency statistics (mean, median, P95, P99, min, max)
- `results.csv`: per-message latency data
- `throughput.png`: throughput over time chart

## Test Results (Main Phase Only – EC2 t2.micro, us-west-2)

| Metric | Value |
|--------|-------|
| Successful messages | 468,000 |
| Failed messages | 0 |
| Throughput | 3,095 msg/sec |
| Mean latency | 31.78 ms |
| P95 latency | 38.56 ms |
| P99 latency | 50.16 ms |

Measured during main phase only. Warmup phase (32,000 messages) excluded from metrics.