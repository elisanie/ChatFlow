# Client Part 1

Basic multithreaded load testing client.

## Requirements
- Java 17+
- Maven
- Server running on port 8080

## Running
```bash
mvn compile exec:java -Dexec.mainClass="edu.neu.cs6650.client.LoadTestClient"
```

## Configuration
Edit SERVER_URL in WarmupPhase.java and MainPhase.java to point to your server.

## Output
- Successful/failed message counts
- Total runtime and throughput
- Connection statistics