package edu.neu.cs6650.client;

import edu.neu.cs6650.client.model.LatencyRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CalAndStatsExport {

    private final List<LatencyRecord> records;

    // for print stats and write csv
    public CalAndStatsExport(List<LatencyRecord> records) {
        this.records = records;
    }

    public void printStats() {
        if (records.isEmpty()) {
            System.out.println("No records for us to analyze.");
            return;
        }

        //get all the latency and sort it increasingly
        long[] latencies = records.stream()
                .mapToLong(LatencyRecord::getLatency) // keep as nanos
                .sorted()
                .toArray();

        int n = latencies.length;
        //mean
        double meanMs = Arrays.stream(latencies).average().orElse(0) / 1_000_000.0;
        //median
        double medianMs = (n % 2 == 1)
                ? latencies[n / 2] / 1_000_000.0
                : (latencies[n / 2 - 1] + latencies[n / 2]) / 2_000_000.0;

        //p95 & p99
        int p95Idx = Math.min((int) Math.ceil(0.95 * n) - 1, n - 1);
        int p99Idx = Math.min((int) Math.ceil(0.99 * n) - 1, n - 1);
        double p95Ms = latencies[p95Idx] / 1_000_000.0;
        double p99Ms = latencies[p99Idx] / 1_000_000.0;
        //min & max
        double minMs = latencies[0] / 1_000_000.0;
        double maxMs = latencies[n - 1] / 1_000_000.0;

        System.out.println("=== Latency Statistics (ms) ===");
        System.out.println("Mean:   " + String.format("%.2f", meanMs));
        System.out.println("Median: " + String.format("%.2f", medianMs));
        System.out.println("P95:    " + String.format("%.2f", p95Ms));
        System.out.println("P99:    " + String.format("%.2f", p99Ms));
        System.out.println("Min:    " + String.format("%.2f", minMs));
        System.out.println("Max:    " + String.format("%.2f", maxMs));

        // message count per room
        System.out.println("\n=== Message Count per Room ===");
        Map<String, Long> roomCounts = records.stream()
                .collect(Collectors.groupingBy(LatencyRecord::getRoomId, Collectors.counting()));
        roomCounts.forEach((room, count) ->
                System.out.println("Room " + room + ": " + count + " messages"));

        // message type distribution
        System.out.println("\n=== Message Type Distribution ===");
        Map<String, Long> typeCounts = records.stream()
                .collect(Collectors.groupingBy(LatencyRecord::getMessageType, Collectors.counting()));
        typeCounts.forEach((type, count) ->
                System.out.println(type + ": " + count + " (" + String.format("%.1f", count * 100.0 / n) + "%)"));
    }

    public void writeCsv(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("relative_start_ms,messageType,latency_ms,statusCode,roomId\n");
            for (LatencyRecord r : records) {
                writer.write(String.format("%.2f,%s,%.2f,%d,%s\n",
                        r.getStartTime() / 1_000_000.0,
                        r.getMessageType(),
                        r.getLatency() / 1_000_000.0,
                        r.getStatusCode(),
                        r.getRoomId()));
            }
            System.out.println("\nCSV written to: " + filename);
        } catch (IOException e) {
            System.err.println("Failed to write CSV: " + e.getMessage());
        }
    }


    }
