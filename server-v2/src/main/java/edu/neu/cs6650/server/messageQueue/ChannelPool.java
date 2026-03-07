package edu.neu.cs6650.server.messageQueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

//bean auto create and in container
@Component
public class ChannelPool {
    private final Connection connection;
    //take() && put()
    private final BlockingQueue<Channel> pool;
    //for all close when shutdown
    private final List<Channel> allChannels = new ArrayList<>();

    //container init bean -- put connection && MQproperties
    public ChannelPool(Connection connection, RabbitMQProperties props) throws Exception {
        this.connection = connection;
        //from rabbitmq.channel-pool-size
        int poolSize = props.getChannelPoolSize();
        //q size == pool size
        this.pool = new ArrayBlockingQueue<>(poolSize);

        for (int i = 0; i < poolSize; i++) {
            Channel ch = connection.createChannel();
            pool.offer(ch);
            allChannels.add(ch);
        }
        System.out.println("ChannelPool init with " + poolSize + " channels");
    }

    public Channel borrow() throws InterruptedException {
        //if have channel, return one immediately, if emmpty, block til return happens
        return pool.take();
    }

    public void release(Channel ch) {
        if (ch == null) return;
        //if channel shutdown/broke -- replace
        if (!ch.isOpen()) {
            replaceOne();
            return;
        }
        try {
            //block til back q successed
            pool.put(ch);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            //if interrupt during put()
            try { ch.close(); } catch (Exception ignored) {}
        }
    }

    // in case pool --
    private void replaceOne() {
        try {
            if (connection != null && connection.isOpen()) {
                Channel newCh = connection.createChannel();
                pool.offer(newCh);
                allChannels.add(newCh);
            }
        } catch (Exception ignored) {}
    }

    //release when shutdown **
    @PreDestroy
    public void shutdown() {
        for (Channel ch : allChannels) {
            try {
                if (ch != null && ch.isOpen()) ch.close();
            } catch (IOException | TimeoutException ignored) {}
        }
    }
}
