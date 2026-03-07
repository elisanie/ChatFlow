package edu.neu.cs6650.server.messageQueue;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(RabbitMQProperties.class)
public class RabbitMQConfig {

    //if shutdown, auto connection.close()
    @Bean(destroyMethod = "close")
    //input binded RabbitMQProperties
    public Connection rabbitConnection(RabbitMQProperties props) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(props.getHost());
        factory.setPort(props.getPort());
        factory.setUsername(props.getUsername());
        factory.setPassword(props.getPassword());
        factory.setVirtualHost(props.getVirtualHost());
        factory.setConnectionTimeout(3000);
        //auto reconnect
        factory.setAutomaticRecoveryEnabled(true);
        //auto topo recover
        factory.setTopologyRecoveryEnabled(true);

        //tcp connection
        Connection connection = factory.newConnection("server-v2");

        //exchange & q & binding
        try (Channel setup = connection.createChannel()) {
            //set up topic exchange
            setup.exchangeDeclare(props.getExchange(), "topic", true);

            //for q declare
            Map<String, Object> args = new HashMap<>();

            //for Good Profile (Stable plateau):
            // args.put("x-message-ttl", 60000);  for q inf expanding
            // args.put("x-max-length", 20000); max msg number



            for (int i = 1; i <= props.getRoomCount(); i++) {
                //room.1 --- room.20
                String queueName = props.getRoomRoutingPrefix() + i;
                setup.queueDeclare(queueName, true, false, false, args);
                //rputing key: queueName (room.20)
                setup.queueBind(queueName, props.getExchange(), queueName);
            }
            System.out.println("RabbitMQ set up complete: exchange + " + props.getRoomCount() + " queues");
        }

        return connection;
    }
}