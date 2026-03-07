package edu.neu.cs6650.server.messageQueue;


import com.rabbitmq.client.Channel;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class RabbitMQPublisher {

    private final ChannelPool channelPool;
    //from application.properties
    private final RabbitMQProperties props;

    public RabbitMQPublisher(ChannelPool channelPool, RabbitMQProperties props) {
        this.channelPool = channelPool;
        this.props = props;
    }

    public void publishToRoom(String roomId, String jsonPayload) throws Exception {
        String routingKey = props.getRoomRoutingPrefix() + roomId;
        Channel ch = channelPool.borrow();
        try {
            //basicPublish need byte[]
            ch.basicPublish(
                    props.getExchange(),
                    routingKey,
                    null,
                    jsonPayload.getBytes(StandardCharsets.UTF_8)
            );
        } finally {
            channelPool.release(ch);
        }
    }
}