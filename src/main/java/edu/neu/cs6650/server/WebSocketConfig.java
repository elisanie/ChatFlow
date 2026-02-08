package edu.neu.cs6650.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    //any websocket req connect to /chat/roomid, give to ChatWebSocketHandler
    //long term connnect give pth to handler obj
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ChatWebSocketHandler(), "/chat/{roomId}")
                .setAllowedOrigins("*");
    }
}