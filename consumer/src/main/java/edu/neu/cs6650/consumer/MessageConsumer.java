package edu.neu.cs6650.consumer;

import com.rabbitmq.client.*;
import edu.neu.cs6650.consumer.messageQueue.BroadcasterProperties;
import edu.neu.cs6650.consumer.messageQueue.RabbitMQProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MessageConsumer {

    private final Connection connection;
    private final RabbitMQProperties rabbitProps;
    private final BroadcasterProperties broadcasterProps;
    private final ExecutorService threadPool;
    private final HttpClient httpClient;
    private final AtomicLong processedCount = new AtomicLong(0);
    private final CountDownLatch stopLatch = new CountDownLatch(1);
    private final List<Channel> channels = new CopyOnWriteArrayList<>();

    private final DBWriter dbwriter;


    public MessageConsumer(Connection connection,
                           RabbitMQProperties rabbitProps,
                           BroadcasterProperties broadcasterProps,
                            DBWriter dbwriter) {
        this.connection = connection;
        this.rabbitProps = rabbitProps;
        this.broadcasterProps = broadcasterProps;
        this.threadPool = Executors.newFixedThreadPool(rabbitProps.getConsumerThreads());
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(broadcasterProps.getTimeoutMs()))
                .build();

        this.dbwriter = dbwriter;

    }

    @PostConstruct
    public void start() throws Exception {
        int roomCount = rabbitProps.getRoomCount();
        int threads = rabbitProps.getConsumerThreads();

        for (int i = 0; i < threads; i++) {
            final int threadIndex = i;
            threadPool.submit(() -> {
                try {
                    Channel channel = connection.createChannel();
                    channel.basicQos(rabbitProps.getPrefetchCount());
                    channels.add(channel);

                    for (int roomId = threadIndex + 1; roomId <= roomCount; roomId += threads) {
                        String queueName = rabbitProps.getRoomRoutingPrefix() + roomId;
                        channel.basicConsume(queueName, false, buildConsumer(channel), consumerTag -> {});
                    }

                    // waiting for stop, not auto join
                    stopLatch.await();
                } catch (Exception e) {
                    System.err.println("Consumer thread " + threadIndex + " error: " + e.getMessage());
                }
            });
        }

        System.out.println("MessageConsumer started: " + threads + " threads consuming " + roomCount + " queues");
    }

    private DeliverCallback buildConsumer(Channel channel) {
        return (consumerTag, delivery) -> {
            String body = new String(delivery.getBody(), StandardCharsets.UTF_8);
            long tag = delivery.getEnvelope().getDeliveryTag();

            try {
                long count = processedCount.incrementAndGet();
                if (count % 1000 == 0) {
                    System.out.println("Processed: " + count + " messages");
                }

                if (broadcasterProps.isEnabled()) {
                    broadcast(body);
                }

                dbwriter.enqueue(body);


                // if its pass through，ACK
                channel.basicAck(tag, false);

            } catch (Exception e) {
                System.err.println("Failed to process: " + e.getMessage());
                // requeue=true， for at-least-once
                channel.basicNack(tag, false, true);
            }
        };
    }

    private void broadcast(String jsonPayload) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(broadcasterProps.getBaseUrl() + "/internal/broadcast"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMillis(broadcasterProps.getTimeoutMs()))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Broadcast failed: HTTP " + response.statusCode());
        }
    }

    @PreDestroy
    public void shutdown() {
        stopLatch.countDown();
        for (Channel ch : channels) {
            try {
                if (ch != null && ch.isOpen()) ch.close();
            } catch (Exception ignored) {}
        }
        threadPool.shutdownNow();
        System.out.println("Consumer shutdown. Total processed: " + processedCount.get());
    }
}