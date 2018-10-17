package bymihaj;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.Framedata.Opcode;
import org.java_websocket.server.WebSocketServer;

import bymihaj.data.order.LimitOrderRequest;
import bymihaj.data.order.MarketOrderRequest;
import bymihaj.data.order.OrderSide;
import bymihaj.jvm.GsonParser;

public class SocketEmulation implements WebSocket {
    
	public final static Instrument DEF_INST = Instrument.STKMON;
	
    protected WebSocketServer server;
    protected MessageResolver resolver;
    protected List<Object> incomeMessageHistory;
    
    public SocketEmulation(WebSocketServer server) {
        this.server = server;
        resolver = new MessageResolver(new GsonParser());
        incomeMessageHistory = new ArrayList<>();
    }
    
    public void send(Object msg) {
        String json = resolver.pack(msg);
        server.onMessage(this, json);
    }
    
    @Override
    public void send(String text) throws NotYetConnectedException {
        incomeMessageHistory.add(resolver.resolve(text));
    }
    
    @SuppressWarnings("unchecked")
    public <T> List<T> filter(Class<T> filter) {
        List<T> list = new ArrayList<>();
        for (Object msg : incomeMessageHistory) {
            if ( msg.getClass().equals(filter)) {
                list.add((T) msg);
            }
        }
        return list;
    }
    
    public <T> T last(Class<T> filterClass) {
    	List<T> list = filter(filterClass);
    	if(list.isEmpty()) {
    		return null;
    	} else {
    		return list.get(list.size() - 1);
    	}
    }
    
    public MarketOrderRequest market(double amount, OrderSide side) {
    	MarketOrderRequest order = new MarketOrderRequest();
    	order.setAmount(amount);
    	order.setSide(side);
    	order.setInstrument(DEF_INST);
    	send(order);
    	return order;
    }
    
    public MarketOrderRequest marketSell(double amount) {
    	return market(amount, OrderSide.SELL);
    }
    
    public MarketOrderRequest marketBuy(double amount) {
    	return market(amount, OrderSide.BUY);
    }
    
    public LimitOrderRequest limit(double amount, double price, OrderSide side) {
    	LimitOrderRequest order = new LimitOrderRequest();
    	order.setAmount(amount);
    	order.setPrice(price);
    	order.setSide(side);
    	order.setInstrument(DEF_INST);
    	send(order);
    	return order;
    }
    
    public LimitOrderRequest limitSell(double amount, double price) {
    	return limit(amount, price, OrderSide.SELL);
    }
    
    public LimitOrderRequest limitBuy(double amount, double price) {
    	return limit(amount, price, OrderSide.BUY);
    }


    @Override
    public void close(int code, String message) {}

    @Override
    public void close(int code) {}

    @Override
    public void close() {}

    @Override
    public void closeConnection(int code, String message) {}

    @Override
    public void send(ByteBuffer bytes) throws IllegalArgumentException, NotYetConnectedException {}

    @Override
    public void send(byte[] bytes) throws IllegalArgumentException, NotYetConnectedException {}

    @Override
    public void sendFrame(Framedata framedata) {}

    @Override
    public void sendFrame(Collection<Framedata> frames) {}

    @Override
    public void sendPing() throws NotYetConnectedException {}

    @Override
    public void sendFragmentedFrame(Opcode op, ByteBuffer buffer, boolean fin) {}

    @Override
    public boolean hasBufferedData() {
        return false;
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress() {
        return null;
    }

    @Override
    public InetSocketAddress getLocalSocketAddress() {
        return null;
    }

    @Override
    public boolean isConnecting() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isClosing() {
        return false;
    }

    @Override
    public boolean isFlushAndClose() {
        return false;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public Draft getDraft() {
        return null;
    }

    @Override
    public READYSTATE getReadyState() {
        return null;
    }

    @Override
    public String getResourceDescriptor() {
        return null;
    }

    @Override
    public <T> void setAttachment(T attachment) {}

    @Override
    public <T> T getAttachment() {
        return null;
    }
    
}