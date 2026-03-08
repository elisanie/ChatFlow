package edu.neu.cs6650.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

@RestController
public class BroadcastController {

    private final ChatWebSocketHandler handler;
    private final ObjectMapper mapper = new ObjectMapper();

    public BroadcastController(ChatWebSocketHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/internal/broadcast")
    public ResponseEntity<String> broadcast(@RequestBody String payload) {
        try {
            Map<?, ?> msg = mapper.readValue(payload, Map.class);
            String roomId = (String) msg.get("roomId");

            CopyOnWriteArraySet<WebSocketSession> sessions =
                    handler.getRoomSessions().get(roomId);

            if (sessions != null) {
                TextMessage textMessage = new TextMessage(payload);
                for (WebSocketSession session : sessions) {
                    if (session.isOpen()) {
                        synchronized (session) {
                            session.sendMessage(textMessage);
                        }
                    }
                }
            }
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("ERROR: " + e.getMessage());
        }
    }
}