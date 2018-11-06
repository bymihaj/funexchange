package bymihaj;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bymihaj.data.order.LimitOrderResponse;
import bymihaj.data.order.MarketOrderResponse;
import bymihaj.data.order.OrderSide;

// TODO split to guest and real user
public class User {

    static Logger log = LoggerFactory.getLogger(User.class);

    protected List<WebSocket> webSockets;
    protected MessageResolver resolver;
    protected boolean isLogined;
    protected Bank bank;
    protected Map<Long, LimitOrderResponse> orderMap;
    protected WebSocket guestSocket;
    protected String userName;

    public User(WebSocket guestSocket, MessageResolver resolver) {
        this.guestSocket = guestSocket;
        this.resolver = resolver;
        webSockets = Collections.synchronizedList(new ArrayList<>());
        isLogined = false;
        orderMap = new ConcurrentHashMap<>();
    }

    public void send(Object msg) {
        String json = resolver.pack(msg);
        String jsonLog = json.replace("\\", "");
        if (!isLogined) {
            guestSocket.send(json);
            log.info("  {}({}:{}) -- {}", identity(),
                    guestSocket.getRemoteSocketAddress().getAddress().getHostAddress(),
                    guestSocket.getRemoteSocketAddress().getPort(), jsonLog);

        }
        for (WebSocket webSocket : webSockets) {
            if (webSocket.isOpen()) {
                webSocket.send(json);
                log.info("  {}({}:{}) -- {}", identity(),
                        webSocket.getRemoteSocketAddress().getAddress().getHostAddress(),
                        webSocket.getRemoteSocketAddress().getPort(), jsonLog);

            }
        }
    }

    // TODO add remove on disconnect
    public void addSession(WebSocket webSocket) {
        webSockets.add(webSocket);
    }

    public void removeSession(WebSocket webSocket) {
        webSockets.remove(webSocket);
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
        prop.setAmount(prop.getAmount().add(BigDecimal.valueOf(amount)));
    }

    public void descrease(Symbol symbol, double amount) {
        Property prop = bank.getProperties().get(symbol);
        prop.setAmount(prop.getAmount().subtract(BigDecimal.valueOf(amount)));
    }

    public String identity() {
        if (isLogined()) {
            return getUserName();
        } else {
            return "Guest";
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    // TODO rework
    public BigDecimal getFreeAsset(Symbol symbol) {
        BigDecimal reserv = BigDecimal.ZERO;
        if (Symbol.MON == symbol) {
            List<LimitOrderResponse> buys = orderMap.values().stream().filter(l -> OrderSide.BUY == l.getSide())
                    .collect(Collectors.toList());
            for (LimitOrderResponse limit : buys) {
                reserv = reserv.add(BigDecimal.valueOf(limit.getRequiredAmount() * limit.getPrice()));
            }
            return getBank().getProperties().get(Symbol.MON).getAmount().subtract(reserv);
        } else {
            List<LimitOrderResponse> sells = orderMap.values().stream().filter(l -> OrderSide.SELL == l.getSide())
                    .collect(Collectors.toList());
            for (LimitOrderResponse limit : sells) {
                reserv = reserv.add(BigDecimal.valueOf(limit.getRequiredAmount()));
            }
            return getBank().getProperties().get(Symbol.STK).getAmount().subtract(reserv);
        }
    }

    public void sendAssests() {
        Map<Symbol, Property> freeMap = new HashMap<>();
        for (Symbol sym : Symbol.values()) {
            Property prop = new Property();
            prop.setName(sym);
            prop.setAmount(getFreeAsset(sym));
            freeMap.put(sym, prop);
        }
        send(new AssetsResponse(freeMap));
    }
    
    public void sendRawString(String raw) {
        for (WebSocket webSocket : webSockets) {
            if (webSocket.isOpen()) {
                webSocket.send(raw);
            }
        }
    }
}
