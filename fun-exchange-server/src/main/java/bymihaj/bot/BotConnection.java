package bymihaj.bot;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bymihaj.MessageListener;
import bymihaj.MessageResolver;
import bymihaj.jvm.GsonParser;

public class BotConnection extends WebSocketClient {

    static Logger log = LoggerFactory.getLogger(BotConnection.class);
    
    protected MessageResolver resolver;
    protected Map<Class<?>, List<MessageListener<?>>> subscirbers;
    protected Runnable openHandler;
    
    public BotConnection(URI serverUri) {
        super(serverUri);
        resolver = new MessageResolver(new GsonParser());
        subscirbers = new HashMap<>();
    }
    
    public void send(Object msg) {
        send(resolver.pack(msg));
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("{} connected", this.getClass().getName());
        if(openHandler != null) {
            openHandler.run();
        }
    }

    @Override
    public void onMessage(String message) {
        Object obj = resolver.resolve(message);
        if( subscirbers.containsKey(obj.getClass())) {
            for (MessageListener<? extends Object> listener : subscirbers.get(obj.getClass())) {
                listener.onMessage(resolver.resolve(message));
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("{} disconnected", this.getClass().getName());
        
    }

    @Override
    public void onError(Exception ex) {
        log.info("{} get error {}", this.getClass().getName(), ex);
    }
    
    public void setOpenHandler(Runnable openHandler) {
        this.openHandler = openHandler;
    }
    
    public <T> void subscribe(Class<?> type, MessageListener<T> listener) {
        if (!subscirbers.containsKey(type)) {
            subscirbers.put(type, new ArrayList<>());
        }
        subscirbers.get(type).add(listener);
    }

}
