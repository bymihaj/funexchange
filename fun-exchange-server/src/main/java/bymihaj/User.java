package bymihaj;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class User {

    static Logger log = LoggerFactory.getLogger(User.class);
    
    protected WebSocket webSocket;
    protected MessageResolver resolver;
    protected boolean isLogined;
    private Bank bank;
    
    
    public User(WebSocket webSocket, MessageResolver resolver) {
        this.webSocket = webSocket;
        this.resolver = resolver;
        isLogined = false;
    }
    
    public void send(Object msg) {
        String json = resolver.pack(msg);
        webSocket.send(json);
        log.info("Send to {} message {}", this.toString(), json);
    }
    
    public WebSocket getWebsocket() {
        return webSocket;
    }
    
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
}
