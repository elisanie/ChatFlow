# ChatFlow Client Part 2

Load testing client with latency analysis and statistics.

## Requirements
- Java 17+
- Maven
- Python 3 with matplotlib 
- Server running on port 8080

## Running
```bash
mvn compile exec:java -Dexec.mainClass="edu.neu.cs6650.client.LoadTestClient"
```

## Generate Chart
```bash
python3 plot.py
```

## Configuration
Edit SERVER_URL in WarmupPhase.java and MainPhase.java to point to your server.

## Output
- Latency statistics (mean, median, P95, P99, min, max)
- Message count per room
- Message type distribution
- results.csv with per-message data
- throughput.png chart