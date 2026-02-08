package edu.neu.cs6650.server;

//validate the msg
public class ChatMessage {
    private String userId;
    private String username;
    private String message;
    private String timestamp;
    private String messageType;
    private String roomId;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
}