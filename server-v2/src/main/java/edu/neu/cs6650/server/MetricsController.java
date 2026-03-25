package edu.neu.cs6650.server;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//for metrics API

//Create an API on your server which returns a JSON with the results of core queries and analytics queries.
//Call this API after the test has ended from your client.
//
//Log the results on your client.
//
//Attach the screenshot of that log in the report.

//need:
//- room-based message retrieval (range query)
//- active user count (distinct aggregation)
//- top-k user and room analysis (group-by queries)
//- total message count (data integrity validation)
@RestController
@RequestMapping("/metrics")
public class MetricsController {

    // use to execute SQL queries directly against the database.
    private final JdbcTemplate jdbcTemplate;

    // Constructor injection
    // auto provide a configured JdbcTemplate bean
    public MetricsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public Map<String, Object> getMetrics(
            // startTime and endTime --> the time window
            // "0 --- 99999..." make the endpoint usable without query para
            @RequestParam(defaultValue = "0") long startTime,
            @RequestParam(defaultValue = "9999999999999") long endTime,

            @RequestParam(defaultValue = "room.1") String roomId) {

        Map<String, Object> result = new HashMap<>();

        // echo the request paras back in the res
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("roomId", roomId);

        // Query 1:
        // Retrieve a sample of msgs from one room within the given time range
        List<Map<String, Object>> roomMessages = jdbcTemplate.queryForList(
                "SELECT message_id, user_id, content, ts " +
                        "FROM messages " +
                        "WHERE room_id = ? AND ts BETWEEN ? AND ? " +
                        "ORDER BY ts LIMIT 100",
                //avoids returning too much data in one API call
                roomId, startTime, endTime
        );
        result.put("sampleRoomMessages", roomMessages);

        // Query 2:
        // Count distinct active users in the selected time window
        Long activeUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT user_id) " +
                        "FROM messages " +
                        "WHERE ts BETWEEN ? AND ?",
                Long.class, startTime, endTime
        );
        result.put("activeUserCount", activeUsers);

        // Query 3:
        // Find the top 10 most active users in the given time window
        List<Map<String, Object>> topUsers = jdbcTemplate.queryForList(
                "SELECT user_id, COUNT(*) AS message_count " +
                        "FROM messages " +
                        "WHERE ts BETWEEN ? AND ? " +
                        "GROUP BY user_id " +
                        "ORDER BY message_count DESC " +
                        "LIMIT 10",
                startTime, endTime
        );
        result.put("topActiveUsers", topUsers);

        // Query 4:
        // Find the top 10 most active rooms in the given time window
        // - measures which rooms received the most traffic
        List<Map<String, Object>> topRooms = jdbcTemplate.queryForList(
                "SELECT room_id, COUNT(*) AS message_count " +
                        "FROM messages " +
                        "WHERE ts BETWEEN ? AND ? " +
                        "GROUP BY room_id " +
                        "ORDER BY message_count DESC " +
                        "LIMIT 10",
                startTime, endTime
        );
        result.put("topActiveRooms", topRooms);

        // Query 5:
        // Read from the pre-aggregated summary table user_room_activity
        List<Map<String, Object>> userRooms = jdbcTemplate.queryForList(
                "SELECT user_id, room_id, message_count, last_ts " +
                        "FROM user_room_activity " +
                        "ORDER BY message_count DESC " +
                        "LIMIT 20"
        );
        result.put("topUserRoomActivityAllTime", userRooms);

        // Query 6:
        // Count total messages stored in the database across all time
        // can compare producer/client-side totals
        Long totalMessages = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM messages",
                Long.class
        );
        result.put("totalMessages", totalMessages);

        return result;
    }
}