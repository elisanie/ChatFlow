package edu.neu.cs6650.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.neu.cs6650.client.model.ChatMessage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MainPhase {
//    After initial threads complete, you're free to create optimal thread configuration
//    Continue until all 500K messages are sent
//    Threads should maintain persistent WebSocket connections where possible
    private static final int THREADS = 100;
//    private static final String SERVER_URL = "ws://localhost:8080";
    private static final String SERVER_URL = "ws://35.92.170.243:8080";
    private static final ObjectMapper mapper = new ObjectMapper();

    private final LinkedBlockingQueue<ChatMessage> queue;
    private final MetricTracker metrics;

    public MainPhase(LinkedBlockingQueue<ChatMessage> queue, MetricTracker metrics) {
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

                    while (true) {
                        ChatMessage msg = queue.poll(2, TimeUnit.SECONDS);
                        if (msg == null) break;


                        //msg's roomid == connection's roomid
                        msg.setRoomId(roomId);
                        //convert to JSON
                        String json = mapper.writeValueAsString(msg);

                        if (client.sendAndWait(json)) {
                            metrics.recordSuccess();
                        } else {
                            metrics.recordFail();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Main thread error: " + e.getMessage());
                } finally {
                    if (client != null) client.close();
                    latch.countDown();
                }
            }).start();
        }


        latch.await();
        System.out.println("Main phase complete.");

    }





}
