package edu.neu.cs6650.client.model;

//every time we sent a msg, would be a latency obj to record all data down
public class LatencyRecord {
    private final long startTime;
    private final long endTime;
    private final long latency;
    private final String messageType;
    private final String roomId;
    private final int statusCode;

    public LatencyRecord(long startTime, long endTime, String messageType, String roomId, int statusCode) {
        this.startTime = startTime;
        this.endTime = endTime;
        //round trip time
        this.latency = endTime - startTime;
        // to cal text/join/leave difference
        this.messageType = messageType;
        this.roomId = roomId;
        //200 = ok; 400 = err; 408 = ootime;
        this.statusCode = statusCode;
    }

    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public long getLatency() { return latency; }
    public String getMessageType() { return messageType; }
    public String getRoomId() { return roomId; }
    public int getStatusCode() { return statusCode; }
}