package bymihaj;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bymihaj.data.order.LimitOrderRequest;
import bymihaj.data.order.MarketOrderRequest;

public class Server extends WebSocketServer {
    
    static Logger log = LoggerFactory.getLogger(Server.class);
    
    protected MessageResolver resolver;
    protected Random random;
    protected Map<Class<?>, List<ClientMessageListener<?>>> subscribers;
    
    protected LoginController loginController;
    protected RightController rightController;
    protected TradeController tradeController;
    
    public Server(InetSocketAddress address) {
        super(address);
        resolver = new MessageResolver();
        random = new Random();
        subscribers = new HashMap<>();
        
        loginController = new LoginController();
        subscribe(AccountRequest.class, loginController::onAccountRequest);
        subscribe(LoginRequest.class, loginController::onLoginRequest);
        subscribe(AssetsRequest.class, loginController::onAssetsRequest);
        
        tradeController = new TradeController();
        subscribe(MarketOrderRequest.class, tradeController::onMarketOrder);
        subscribe(LimitOrderRequest.class, tradeController::onLimitOrder);
        
        rightController = new RightController();
    }
    
    @Override
    public void onOpen(WebSocket paramWebSocket, ClientHandshake paramClientHandshake) {
        log.info("onOpen");
    }

    @Override
    public void onClose(WebSocket paramWebSocket, int paramInt, String paramString, boolean paramBoolean) {
        log.info("onClose");            
    }

    // RightController
    // LoginController
    @Override
    public void onMessage(WebSocket paramWebSocket, String paramString) {
        log.info("onMessage: {}", paramString);    
        Object income = resolver.resolve(paramString);
        
        User user = loginController.getUser(paramWebSocket);
        if(rightController.allowed(user, income)) {
            if(subscribers.containsKey(income.getClass())) {
                for(ClientMessageListener<? extends Object> listener : subscribers.get(income.getClass()) ) {
                    // TODO PREVENT RESOLVE TWICE !!!!!
                    /// !!!!!!!!!!!!!!!!!!!!!!!!!!!
                    listener.onMessage(user, resolver.resolve(paramString));
                }
            } else {
                log.error("No subscribers for message {}", income.getClass());
            }
        } else {
            log.error("Income message {} not allowed to user {}", income.getClass().getName(), user.toString());
        }
       
        /*
        if (income instanceof AccountRequest) {
            AccountResponse response = generateAccount();
            
            // TODO wrap and log
            paramWebSocket.send(resolver.pack(response));
        
        } else {
            log.error("Unproduced message: {}", income.getClass().getName());
        }
        */
    }

    @Override
    public void onError(WebSocket paramWebSocket, Exception paramException) {
        log.error("onError", paramException);            
    }

    @Override
    public void onStart() {
        log.info("onStart");            
    }

    public <T> void subscribe(Class<?> type, ClientMessageListener<T> listener) {
        if (!subscribers.containsKey(type)) {
            subscribers.put(type, new ArrayList<>());
        }
        subscribers.get(type).add(listener);
    }
    

}
