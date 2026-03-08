package edu.neu.cs6650.consumer.messageQueue;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({RabbitMQProperties.class, BroadcasterProperties.class})
public class RabbitMQConfig {

    @Bean(destroyMethod = "close")
    public Connection rabbitConnection(RabbitMQProperties props) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(props.getHost());
        factory.setPort(props.getPort());
        factory.setUsername(props.getUsername());
        factory.setPassword(props.getPassword());
        factory.setVirtualHost(props.getVirtualHost());
        factory.setConnectionTimeout(3000);
        factory.setAutomaticRecoveryEnabled(true);
        factory.setTopologyRecoveryEnabled(true);
        //connection name
        return factory.newConnection("consumer");
    }
}
