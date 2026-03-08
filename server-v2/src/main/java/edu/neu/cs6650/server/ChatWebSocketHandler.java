package edu.neu.cs6650.server;

import edu.neu.cs6650.server.messageQueue.RabbitMQPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    //Jackson's json tool
    //readvalue(payload, chatmsg.ckass) json -> java
    //writevalueasstring(map) java -> json
    //msg type for valid types
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Set<String> VALID_TYPES = Set.of("TEXT", "JOIN", "LEAVE");

    //update for assignemnt 2:
    private final RabbitMQPublisher publisher;
    // server instances will have a random 8 size id, for monitor purpose
    private final String serverId = UUID.randomUUID().toString().substring(0, 8);


    // room → all the WebSocket Session for the room
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> roomSessions
            = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(RabbitMQPublisher publisher) {
        //decouple
        this.publisher = publisher;
    }

    // for BroadcastController invoke
    public ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> getRoomSessions() {
        return roomSessions;
    }

    //updates done

    //3 way handshake, connect
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //get room id from url --> /chat/5 --> / 5 at +1 -- > 5
        String path = session.getUri().getPath();
        String roomId = path.substring(path.lastIndexOf("/") + 1);

        //label this link's room id
        session.getAttributes().put("roomId", roomId);

        //when the user connects join the room
        roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(session);
        System.out.println("Connected: " + session.getId() + " room: " + roomId);
    }

    //updates done

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

                //updates for assignment 2:
                // validate success, ready for queue msg
                //get ip : tracking client ip
                String clientIp = "unknown";
                if (session.getRemoteAddress() != null && session.getRemoteAddress().getAddress() != null) {
                    clientIp = session.getRemoteAddress().getAddress().getHostAddress();
                }

                Map<String, Object> queueMessage = new LinkedHashMap<>();
                queueMessage.put("messageId", UUID.randomUUID().toString());
                queueMessage.put("roomId", chat.getRoomId());
                queueMessage.put("userId", chat.getUserId());
                queueMessage.put("username", chat.getUsername());
                queueMessage.put("message", chat.getMessage());
                queueMessage.put("timestamp", Instant.now().toString());
                queueMessage.put("messageType", chat.getMessageType());
                queueMessage.put("serverId", serverId);
                queueMessage.put("clientIp", clientIp);

                //send back to server ACK
                Map<String, Object> ack = Map.of(
                        "status", "OK",
                        "serverTimestamp", Instant.now().toString()
                );

                // publish fail handle individually
                try {
                    publisher.publishToRoom(chat.getRoomId(), mapper.writeValueAsString(queueMessage));
                    session.sendMessage(new TextMessage(mapper.writeValueAsString(ack)));
                } catch (Exception ex) {
                    Map<String, Object> mqErr = Map.of(
                            "status", "ERROR",
                            "reason", "MQ_PUBLISH_FAILED"
                    );
                    session.sendMessage(new TextMessage(mapper.writeValueAsString(mqErr)));
                    //updates done
                }
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
//        userId must be between 1 and 100000
//        username must be 3-20 alphanumeric characters
//        message must be 1-500 characters
//        timestamp must be valid ISO-8601
//        messageType must be one of the specified values

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
        // when user disconnects, remove them from the room
        String roomId = (String) session.getAttributes().get("roomId");
        if (roomId != null) {
            CopyOnWriteArraySet<WebSocketSession> sessions = roomSessions.get(roomId);
            if (sessions != null) sessions.remove(session);
        }
        System.out.println("Disconnected: " + session.getId());
    }
}