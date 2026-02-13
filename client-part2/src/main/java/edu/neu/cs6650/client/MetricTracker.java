package edu.neu.cs6650.client;

import edu.neu.cs6650.client.model.LatencyRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MetricTracker {

    // atonomic ops
    // make sure the incrementandget()
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failCount = new AtomicInteger(0);
    private final AtomicInteger totalConnections = new AtomicInteger(0);
    private final AtomicInteger reconnections = new AtomicInteger(0);
    //no lock, performance for concurrency
    private final ConcurrentLinkedQueue<LatencyRecord> latencyRecords = new ConcurrentLinkedQueue<>();
    private long startTime;
    private long endTime;

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void end() {
        endTime = System.currentTimeMillis();
    }

    public void recordSuccess() {
        successCount.incrementAndGet();
    }

    public void recordFail() {
        failCount.incrementAndGet();
    }

    public void recordConnection() {
        totalConnections.incrementAndGet();
    }

    public void recordReconnection() {
        reconnections.incrementAndGet();
    }

    public void recordLatency(LatencyRecord record) {
        latencyRecords.add(record);
    }

    public List<LatencyRecord> getLatencyRecords() {
        return new ArrayList<>(latencyRecords);
    }

    public void printResults() {
        long duration = endTime - startTime;
        double seconds = duration / 1000.0;
        double throughput = successCount.get() / seconds;

        System.out.println("--- Test Results ---");
        System.out.println("Successful messages: " + successCount.get());
        System.out.println("Failed messages:     " + failCount.get());
        System.out.println("Total runtime:       " + duration + " ms (" + String.format("%.2f", seconds) + " s)");
        System.out.println("Throughput:          " + String.format("%.2f", throughput) + " messages/sec");
        System.out.println("Total connections:   " + totalConnections.get());
        System.out.println("Reconnections:       " + reconnections.get());
    }
}


