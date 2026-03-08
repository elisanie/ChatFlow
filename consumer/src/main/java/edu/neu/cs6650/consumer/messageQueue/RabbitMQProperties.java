package edu.neu.cs6650.consumer.messageQueue;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMQProperties {
    //MQ set ups
    private String host = "localhost";
    private int port = 5672;
    private String username = "guest";
    private String password = "guest";
    private String virtualHost = "/";
    private String exchange = "chat.exchange";
    private String roomRoutingPrefix = "room.";
    private int roomCount = 20;
    private int prefetchCount = 50;
    private int consumerThreads = 8;

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getVirtualHost() { return virtualHost; }
    public void setVirtualHost(String virtualHost) { this.virtualHost = virtualHost; }
    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    public String getRoomRoutingPrefix() { return roomRoutingPrefix; }
    public void setRoomRoutingPrefix(String roomRoutingPrefix) { this.roomRoutingPrefix = roomRoutingPrefix; }
    public int getRoomCount() { return roomCount; }
    public void setRoomCount(int roomCount) { this.roomCount = roomCount; }
    public int getPrefetchCount() { return prefetchCount; }
    public void setPrefetchCount(int prefetchCount) { this.prefetchCount = prefetchCount; }
    public int getConsumerThreads() { return consumerThreads; }
    public void setConsumerThreads(int consumerThreads) { this.consumerThreads = consumerThreads; }
}
