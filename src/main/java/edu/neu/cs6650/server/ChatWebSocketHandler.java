package edu.neu.cs6650.server;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    //Jackson's json tool
    //readvalue(payload, chatmsg.ckass) json -> java
    //writevalueasstring(map) java -> json
    //msg type for valid types
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Set<String> VALID_TYPES = Set.of("TEXT", "JOIN", "LEAVE");


    //3 way handshake, connect
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //get room id from url --> /chat/5 --> / 5 at +1 -- > 5
        String path = session.getUri().getPath();
        String roomId = path.substring(path.lastIndexOf("/") + 1);

        //label this link's room id
        session.getAttributes().put("roomId", roomId);
        System.out.println("Connected: " + session.getId() + " room: " + roomId);
    }

    //echo the msg
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        raw data like json: {
//            "userId": "1",
//                "username": "user1",
//                "message": "hi",
//                "timestamp": "2026-02-07T22:00:00Z",
//                "messageType": "TEXT",
//                "roomId": "5"
//        }

//        payload： "{\"userId\":\"1\",\"username\":\"user1\",\"message\":\"hi\",\"timestamp\":\"2026-02-07T22:00:00Z\",\"messageType\":\"TEXT\",\"roomId\":\"5\"}"
        String payload = message.getPayload();
        String connectionRoomId = (String) session.getAttributes().get("roomId");

        try {
            ChatMessage chat = mapper.readValue(payload, ChatMessage.class);
            String err = validate(chat, connectionRoomId);

            if (err != null) {
                Map<String, Object> errResponse = Map.of(
                        "status", "error",
                        "error", err,
                        "serverTimestamp", Instant.now().toString()
                );
                session.sendMessage(new TextMessage(mapper.writeValueAsString(errResponse)));

            }else {
                //echo if no err
                Map<String, Object> successResponse = Map.of(
                        "userId", chat.getUserId(),
                        "username", chat.getUsername(),
                        "message", chat.getMessage(),
                        "timestamp", chat.getTimestamp(),
                        "messageType", chat.getMessageType(),
                        "roomId", chat.getRoomId(),
                        "status", "OK",
                        "serverTimestamp", Instant.now().toString()
                );
                session.sendMessage(new TextMessage(mapper.writeValueAsString(successResponse)));
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "status", "ERROR",
                    "error", "Invalid JSON format",
                    "serverTimestamp", Instant.now().toString()
            );
            session.sendMessage(new TextMessage(mapper.writeValueAsString(errorResponse)));
        }
    }

    private String validate(ChatMessage chat, String connectionRoomId) {
//        Generate 500,000 chat messages total with random data:
//        userId: random between 1-100000
//        username: generate from userId (e.g., "user12345")
//        message: random from a pool of 50 pre-defined messages
//        roomId: random between 1-20
//        messageType: 90% TEXT, 5% JOIN, 5% LEAVE
//        timestamp: current time
        try {
            int id = Integer.parseInt(chat.getUserId());
            if (id < 1 || id > 100000) {
                return "user id must between 1-100000";
            }
        } catch (Exception e) {
                return "user id must be a valid number";
        }

        if (chat.getUsername() == null || !chat.getUsername().matches("^[a-zA-Z0-9]{3,20}$")) {
            return "username must be 3-20 alphanumeric characters";
        }

        if (chat.getMessage() == null || chat.getMessage().isEmpty() || chat.getMessage().length() > 500) {
            return "message must be 1-500 characters";
        }

        try {
            Instant.parse(chat.getTimestamp());
        } catch (Exception e) {
            return "timestamp must be valid ISO-8601";
        }

        if (chat.getMessageType() == null || !VALID_TYPES.contains(chat.getMessageType())) {
            return "messageType must be TEXT, JOIN, or LEAVE";
        }

        if (chat.getRoomId() == null || !chat.getRoomId().equals(connectionRoomId)) {
            return "roomId must match connection room: " + connectionRoomId;
        }

        return null;

    }


    //break connect
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Disconnected: " + session.getId());
    }
}