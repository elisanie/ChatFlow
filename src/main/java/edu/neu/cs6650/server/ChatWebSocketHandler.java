package edu.neu.cs6650.server;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    //3 way handshake, connect
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connected: " + session.getId());
    }

    //echo the msg
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        session.sendMessage(message);
    }

    //break connect
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Disconnected: " + session.getId());
    }
}