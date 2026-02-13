package edu.neu.cs6650.client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ChatWebSocketClient extends WebSocketClient {
    // onoepn, onmsg, onclose, onerr

    private final String roomId;
    // if the connect established once, onopen() invoke, then can client.connect(),
    // countdown in onppen, await in connect and wait()
    private final CountDownLatch connectLatch = new CountDownLatch(1);
    // !: volatile for visibility
    // send and wait will ini a new latch(1), then await
    // get reg in on msg then countdown.
    private volatile CountDownLatch responseLatch;

    public ChatWebSocketClient(String serverURL, String roomId) {
        //server websocket endpoint be /chat/{roomId}
        //“you should use the same room id in the message as you established your connection with.”
        super(URI.create(serverURL + "/chat/" + roomId));
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        connectLatch.countDown();

    }

    @Override
    public void onMessage(String s) {
        if (responseLatch != null) {
            responseLatch.countDown();
        }

    }

    @Override
    public void onClose(int i, String s, boolean b) {
        connectLatch.countDown();
        if (responseLatch != null) {
            responseLatch.countDown();
        }

    }

    @Override
    public void onError(Exception e) {
        System.err.println("WebSocket error: " + e.getMessage());
        connectLatch.countDown();
        if (responseLatch != null) {
            responseLatch.countDown();
        }

    }

    public boolean connectAndWait() throws InterruptedException {
        this.connect();
        // block and waiting for connect
        return connectLatch.await(5, TimeUnit.SECONDS) && this.isOpen();
    }

    public boolean sendAndWait(String msg) throws InterruptedException {
        responseLatch = new CountDownLatch(1);
        this.send(msg);
        // send msg synchronized and waiting for response
        return responseLatch.await(5, TimeUnit.SECONDS);
    }

}
