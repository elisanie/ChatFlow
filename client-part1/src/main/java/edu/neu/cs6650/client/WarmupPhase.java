package edu.neu.cs6650.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.neu.cs6650.client.model.ChatMessage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class WarmupPhase {
//    Create 32 threads at startup
//    Each thread establishes a WebSocket connection
//    Each thread sends 1000 messages then terminates
//    Measure this phase separately as "warmup"


    //use to generate 32 threads and await till all done
    private static final int THREADS = 32;
    private static final int MESSAGES_PER_THREAD = 1000;
//    private static final String SERVER_URL = "ws://localhost:8080";
    private static final String SERVER_URL = "ws://35.92.170.243:8080";
    private static final ObjectMapper mapper = new ObjectMapper();

    private final LinkedBlockingQueue<ChatMessage> queue;
    private final MetricTracker metrics;

    public WarmupPhase(LinkedBlockingQueue<ChatMessage> queue, MetricTracker metrics) {
        this.queue = queue;
        this.metrics = metrics;
    }

    public void run() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(THREADS);

        for (int i = 0; i < THREADS; i++) {
            //thread 0 - room1 ... thread 19 - room 20
            // to spread out 32 t to 20 rooms
            String roomId = String.valueOf((i % 20) + 1);

            new Thread(() -> {
                ChatWebSocketClient client = null;
                try {
                    // every client has a individual connection
                    client = new ChatWebSocketClient(SERVER_URL, roomId);
                    if (!client.connectAndWait()) {
                        System.err.println("Warmup: connection failed for room " + roomId);
                        return;
                    }

                    //connection --> metrics
                    metrics.recordConnection();

                    //0 -1000
                    for (int j = 0; j < MESSAGES_PER_THREAD; j++) {
                        ChatMessage msg = queue.poll(2, TimeUnit.SECONDS);
                        if (msg == null) break;

                        //msg's roomid == connection's roomid
                        msg.setRoomId(roomId);
                        //convert to JSON
                        String json = mapper.writeValueAsString(msg);


                        int maxRetries = 5;
                        for (int attempt = 0; attempt < maxRetries; attempt++) {
                            if (client == null || !client.isOpen()) {
                                if (client != null) {
                                    try { client.close(); } catch (Exception ignore) {}
                                }
                                client = new ChatWebSocketClient(SERVER_URL, roomId);
                                if (!client.connectAndWait()) {
                                    if (attempt < maxRetries - 1) {
                                        Thread.sleep((1L << attempt) * 1000L);
                                    }
                                    continue;
                                }
                                metrics.recordReconnection();
                            }

                            try {
                                boolean ok = client.sendAndWait(json);
                                if (ok) break;
                            } catch (Exception e) {
                                // connection issue
                            }

                            if (attempt < maxRetries - 1) {
                                Thread.sleep((1L << attempt) * 1000L);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Warmup thread error: " + e.getMessage());
                } finally {
                    if (client != null) client.close();
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        System.out.println("Warm-up phase complete.");
    }
}