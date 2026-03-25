package edu.neu.cs6650.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class DBWriter {
//    consumer thread -> enqueue -> writeQueue -> db writer thread
    //(server - v2, chatsockethandler.java)

    //spring's JDBC tool to op sql and batch insert
    private final JdbcTemplate jdbcTemplate;
    //using blocking queue to let consumer thread to passin msg.
    // writer thread will tak msg out and multi write to batch
    //decouple the receive msg & write to db
    private final BlockingQueue<String> writeQueue = new LinkedBlockingQueue<>();
    // Jackson's JSON deparse tool
    // avoid every msg a new variable
    private final ObjectMapper objectMapper = new ObjectMapper();
    //max batch size for now
    private static final int BATCH_SIZE = 500;
    //max waiting time for flush
    private static final int FLUSH_INTERVAL_MS = 500;

    //constructor auto when set up
    public DBWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //method to be able put msg in quene for rabbit mq consumer
    public void enqueue(String message) {
        writeQueue.offer(message);
    }

    // Spring auto op after set up bean
    //back thread for take msg from queue and write to db
    @PostConstruct
    public void start() {
        Thread writerThread = new Thread(this::batchWriteLoop);
        writerThread.setDaemon(true); // protect, no conjes jvm shutdown when exit app
        writerThread.start();
        System.out.println("DatabaseWriter started");
    }

    //keep taking msg from queue --> aggrete to batch --> to db
    private void batchWriteLoop() {
        List<String> batch = new ArrayList<>();

        while (true) {
            try {
                //wait most flush time to take one msg, if no data, return null
                String msg = writeQueue.poll(FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS);

                if (msg != null) {
                    // add the first msg to batch
                    batch.add(msg);
                    //take more msg and put to batch
                    writeQueue.drainTo(batch, BATCH_SIZE - batch.size());
                }

                //timeout or batch is full
                if (batch.size() >= BATCH_SIZE || (msg == null && !batch.isEmpty())) {
                    writeBatch(batch);
                    //after write, clear for the next round
                    batch.clear();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Batch write error: " + e.getMessage());
                batch.clear();
            }
        }

    }

    // batch insert method
    private void writeBatch(List<String> messages) {
        // INSERT IGNORE:
        //If message_id already exists (primary key conflict), ignore this one
        //to handle possible repeated deliveries in RabbitMQ to achieve idempotent writes
        String sql = "INSERT IGNORE INTO messages (message_id, room_id, user_id, content, ts) VALUES (?, ?, ?, ?, ?)";

        // for JdbcTemplate.batchUpdate
        List<Object[]> params = new ArrayList<>();

        for (String raw : messages) {
            try {
                // JSON parse to JsonNode
                JsonNode node = objectMapper.readTree(raw);

                //rabbit mq msg:messageId, roomId, userId, username, message, timestamp, messageType, serverId, clientIp
                params.add(new Object[]{
                        node.path("messageId").asText(), // PK
                        node.path("roomId").asText(),
                        node.path("userId").asText(),
                        node.path("message").asText(),
                        Instant.parse(node.path("timestamp").asText()).toEpochMilli()

                });
            } catch (Exception e) {
                // when single msg failed, only skip this one not the whole batch
                System.err.println("Parse error for message: " + raw + ", error: " + e.getMessage());
            }
        }

        // when there's data then batch insert
        if (!params.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, params);
            System.out.println("Wrote batch of " + params.size());
            updateUserRoomActivity(messages);

        }
    }

    //update user room table
    // to maintain pre-computed statistics to avoid expensive GROUP BY queries on the messages table
    private void updateUserRoomActivity(List<String> messages) {

        // upsert statement:
        // if (user_id, room_id) does not exist -->  insert with message_count = 1
        // if exists -->  increment message_count and update last_ts to the latest value
        String sql = "INSERT INTO user_room_activity (user_id, room_id, message_count, last_ts) VALUES (?, ?, 1, ?) " +
                "ON DUPLICATE KEY UPDATE message_count = message_count + 1, last_ts = GREATEST(last_ts, VALUES(last_ts))";

        // each obj corresponds to one row insert/update
        List<Object[]> params = new ArrayList<>();

        //iterate msg through queue
        for (String raw : messages) {
            try {
                JsonNode node = objectMapper.readTree(raw);
                params.add(new Object[]{
                        // extract required fields:
                        // userId + roomId (composite PK)
                        // ts --> latest activity timestamp
                        node.path("userId").asText(),
                        node.path("roomId").asText(),
                        Instant.parse(node.path("timestamp").asText()).toEpochMilli()
                });
            } catch (Exception e) {
                System.err.println("Parse error in updateUserRoomActivity: " + raw);
            }
        }

        //  only execute batch update if we have valid records
        if (!params.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, params);
        }
    }

}
