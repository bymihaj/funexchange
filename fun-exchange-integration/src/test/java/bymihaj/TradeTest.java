package bymihaj;

import java.net.InetSocketAddress;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bymihaj.data.order.MarketOrderRequest;
import bymihaj.data.order.OrderSide;
import bymihaj.data.order.RejectOrderResponse;

public class TradeTest {
    
    protected Server server;
    protected SocketEmulation client;
    
    @Before
    public void before() {
        server = new Server(new InetSocketAddress(0));
        client = new SocketEmulation(server);
    }

    @Test
    public void rejectMarketOrderTest() {
        IntegrationHelper.login(server, client);
        
        MarketOrderRequest req = new MarketOrderRequest();
        req.setAmount(1.0);
        req.setSide(OrderSide.BUY);
        client.send(req);
        
        Assert.assertFalse(client.filter(RejectOrderResponse.class).isEmpty());
    }
    
}
