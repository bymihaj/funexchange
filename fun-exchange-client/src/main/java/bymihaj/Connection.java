package bymihaj;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import bymihaj.jvm.GsonParser;
import javafx.application.Platform;

public class Connection extends WebSocketClient {
    
    protected MessageResolver resolver;
    
    // TODO make one for client and server
    protected Map<Class<?>, List<MessageListener<?>>> subscirbers;
    
    public Connection(URI serverUri) {
        super(serverUri);
        resolver = new MessageResolver(new GsonParser());
        subscirbers = new HashMap<>();
    }
    
    public void send(Object msg) {
        send(resolver.pack(msg));
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("onOpen");
        
    }

    @Override
    public void onMessage(String message) {
        System.out.println("onMessage "+message);
        Object obj = resolver.resolve(message);
        if( subscirbers.containsKey(obj.getClass())) {
            for (MessageListener<? extends Object> listener : subscirbers.get(obj.getClass())) {
                Platform.runLater(() -> {
                    listener.onMessage(resolver.resolve(message));
                });
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("onClose");
        
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("onError");
        ex.printStackTrace();
    }
    
    public <T> void subscribe(Class<?> type, MessageListener<T> listener) {
        if (!subscirbers.containsKey(type)) {
            subscirbers.put(type, new ArrayList<>());
        }
        subscirbers.get(type).add(listener);
    }

}
