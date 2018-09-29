package bymihaj;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bymihaj.data.order.LimitOrderResponse;

// TODO split to guest and real user
public class User {

    static Logger log = LoggerFactory.getLogger(User.class);
    
    protected List<WebSocket> webSockets;
    protected MessageResolver resolver;
    protected boolean isLogined;
    protected Bank bank;
    protected Map<Long, LimitOrderResponse> orderMap;
    protected WebSocket guestSocket;
    
    
    public User(WebSocket guestSocket, MessageResolver resolver) {
        this.guestSocket = guestSocket;
        this.resolver = resolver;
        webSockets = Collections.synchronizedList(new ArrayList<>());
        isLogined = false;
        orderMap = new ConcurrentHashMap<>();
    }
    
    public void send(Object msg) {
        String json = resolver.pack(msg);
        if(!isLogined) {
        	guestSocket.send(json);
        }
        for(WebSocket webSocket : webSockets) {
        	if(webSocket.isOpen()) {
        		webSocket.send(json);
            }
        }
        log.info("Send to {} message {}", this.toString(), json);
    }
    
    // TODO add remove on disconnect
    public void addSession(WebSocket webSocket) {
    	webSockets.add(webSocket);
    }
    
    public WebSocket getGuestSocket() {
    	return guestSocket;
    }
    
    
    // TODO check status by socket session available
    public boolean isLogined() {
        return isLogined;
    }
    
    public void setLogined(boolean b) {
        isLogined = b;
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }
    
    public void addOrder(Long id, LimitOrderResponse order) {
        orderMap.put(id, order);
    }
    
    public void removeOrder(Long id) {
    	orderMap.remove(id);
    }
    
    public List<LimitOrderResponse> getAllOrders() {
    	return new ArrayList<>(orderMap.values());
    }
    
    public void increase(Symbol symbol, double amount) {
    	Property prop = bank.getProperties().get(symbol);
    	prop.setAmount(prop.getAmount().add(new BigDecimal(amount)));
    }
    
    public void descrease(Symbol symbol, double amount) {
    	Property prop = bank.getProperties().get(symbol);
    	prop.setAmount(prop.getAmount().subtract(new BigDecimal(amount)));
    }
}
