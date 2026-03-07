package edu.neu.cs6650.server.messageQueue;

//config properties, will put application.properties yml to it
import org.springframework.boot.context.properties.ConfigurationProperties;

//add rabbitmq.* to it
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMQProperties {

    //docker
    private String host = "localhost";
    private int port = 5672;
    private String username = "guest";
    private String password = "guest";
    //virtual host
    private String virtualHost = "/";

    //Create a topic exchange named chat.exchange
    //exchangeDeclare(props.getExchange(), "topic", true)
    //rabbitmq.exchange=chat.exchange
    private String exchange = "chat.exchange";

    //routing key=queue name
    //rabbitmq.room-routing-prefix=room.
    private String roomRoutingPrefix = "room.";

    //rabbitmq.channel-pool-size=20 && rabbitmq.room-count=20
    private int channelPoolSize = 20;
    private int roomCount = 20;

    public String getHost() {
        return host;
    }

    public void setHost(String h) {
        this.host = h;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int p) {
        this.port = p;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String u) {
        this.username = u;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String p) {
        this.password = p;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String v) {
        this.virtualHost = v;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String e) {
        this.exchange = e;
    }

    public String getRoomRoutingPrefix() {
        return roomRoutingPrefix;
    }

    public void setRoomRoutingPrefix(String r) {
        this.roomRoutingPrefix = r;
    }

    public int getChannelPoolSize() {
        return channelPoolSize;
    }

    public void setChannelPoolSize(int s) {
        this.channelPoolSize = s;
    }

    public int getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(int c) {
        this.roomCount = c;

    }

}