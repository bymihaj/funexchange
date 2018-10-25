package bymihaj.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.realityforge.gwt.websockets.client.WebSocket;
import org.realityforge.gwt.websockets.client.WebSocketListener;

import com.google.gwt.typedarrays.shared.ArrayBuffer;

import bymihaj.MessageListener;
import bymihaj.MessageResolver;

public class Connection implements WebSocketListener {

    protected MessageResolver resolver;
    protected Map<Class<?>, List<MessageListener<?>>> subscirbers;
    protected WebSocket ws;
    
    public Connection(String wsUrl) {
        resolver = new MessageResolver(new GwtParser());
        subscirbers = new HashMap<>();
        ws = WebSocket.newWebSocketIfSupported();
        ws.setListener(this);
        ws.connect(wsUrl);
    }
    
    public void send(Object msg) {
        ws.send(resolver.pack(msg));
    }
    
    @Override
    public void onMessage(WebSocket webSocket, String json) {
        Object obj = resolver.resolve(json);
        if( subscirbers.containsKey(obj.getClass())) {
            for (MessageListener<? extends Object> listener : subscirbers.get(obj.getClass())) {
                listener.onMessage(resolver.resolve(json));
            }
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ArrayBuffer data) {}

    @Override
    public void onError(WebSocket webSocket) {
        System.out.println("WebScoket errr");
    }
    
    public <T> void subscribe(Class<?> type, MessageListener<T> listener) {
        if (!subscirbers.containsKey(type)) {
            subscirbers.put(type, new ArrayList<>());
        }
        subscirbers.get(type).add(listener);
    }
    
    @Override
    public void onOpen(WebSocket webSocket) {}

    @Override
    public void onClose(WebSocket webSocket, boolean wasClean, int code, String reason) {}


}
