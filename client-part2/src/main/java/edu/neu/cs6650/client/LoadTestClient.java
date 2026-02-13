package edu.neu.cs6650.client;

import edu.neu.cs6650.client.model.ChatMessage;

import java.util.concurrent.LinkedBlockingQueue;

public class LoadTestClient {

    public static void main(String[] args) throws Exception {
        //ready to coshare obj -- pool and tracker
        LinkedBlockingQueue<ChatMessage> queue = new LinkedBlockingQueue<>();
        MetricTracker metrics = new MetricTracker();

        // Step 1: producer
        System.out.println("Generating messages...");
        MsgGenerator generator = new MsgGenerator(queue);
        Thread genThread = new Thread(generator);
        genThread.start();
        genThread.join(); // wait til all finish
        System.out.println("Queue size: " + queue.size());

        // Step 2: Warmup phase (not put to metrics)
        System.out.println("Starting warmup phase...");
        WarmupPhase warmup = new WarmupPhase(queue, metrics);
        warmup.run();
        System.out.println("Remaining after warmup: " + queue.size());

        // Step 3: Main phase (start time here)
        System.out.println("Starting main phase...");
        metrics.start();
        MainPhase main = new MainPhase(queue, metrics);
        main.run();
        metrics.end();


        // Step 4: results
        metrics.printResults();

        CalAndStatsExport export = new CalAndStatsExport(metrics.getLatencyRecords());
        export.printStats();
        export.writeCsv("results.csv");
    }
}