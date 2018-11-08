package bymihaj;

import java.net.InetSocketAddress;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bymihaj.data.order.LimitOrderRequest;
import bymihaj.data.order.LimitOrderResponse;
import bymihaj.data.order.MarketOrderRequest;
import bymihaj.data.order.MarketOrderResponse;
import bymihaj.data.order.OrderSide;
import bymihaj.data.order.RejectOrderResponse;
import bymihaj.data.order.RejectOrderType;

public class ValidationTest {

    final long ping = 100;
    
    protected Server server;
    protected SocketEmulation client;
    protected MarketOrderRequest goodMarket;
    protected LimitOrderRequest goodLimit;

    @Before
    public void before() {
        server = new Server(new InetSocketAddress(0));
        client = new SocketEmulation(server);
        IntegrationHelper.login(server, client);
        
        goodMarket = new MarketOrderRequest();
        goodMarket.setAmount(10.0);
        goodMarket.setSide(OrderSide.BUY);
        goodMarket.setInstrument(Instrument.STKMON);
        
        goodLimit = new LimitOrderRequest();
        goodLimit.setAmount(10.0);
        goodLimit.setPrice(3.3);
        goodLimit.setSide(OrderSide.BUY);
        goodLimit.setInstrument(Instrument.STKMON);
    }
    
    @Test
    public void marketAmountInvalidTest() {
        goodMarket.setAmount(Symbol.STK.getCoin().doubleValue() / 2.0);
        client.send(goodMarket);
        
        RejectOrderResponse reject = client.last(RejectOrderResponse.class);
        Assert.assertEquals(RejectOrderType.INVALID_CONDITION, reject.getRejectType());
    }
    
    @Test
    public void marketAmountRoundedTest() {
        client.limitSell(10.0, 1.0);
        
        goodMarket.setAmount(Symbol.STK.getCoin().doubleValue() * 1.5);
        client.send(goodMarket);
        client.wait(MarketOrderResponse.class);
        
        MarketOrderResponse order = client.last(MarketOrderResponse.class);
        Assert.assertEquals(Symbol.STK.getCoin().doubleValue(), order.getFilledAmount(), 0);
    }
    
    @Test
    public void limitAmountInvalidTest() {
        goodLimit.setAmount(Symbol.STK.getCoin().doubleValue() / 2.0);
        client.send(goodLimit);
        
        RejectOrderResponse reject = client.last(RejectOrderResponse.class);
        Assert.assertEquals(RejectOrderType.INVALID_CONDITION, reject.getRejectType());
    }
    
    @Test
    public void limitAmountRoundedTest() {
        goodLimit.setAmount(Symbol.STK.getCoin().doubleValue() * 1.5);
        client.send(goodLimit);
        client.wait(LimitOrderResponse.class);
        
        LimitOrderResponse order = client.last(LimitOrderResponse.class);
        Assert.assertEquals(Symbol.STK.getCoin().doubleValue(), order.getAmount(), 0);
    }
    
    @Test
    public void limitPriceInvalidTest() {
        goodLimit.setPrice(Symbol.MON.getCoin().doubleValue() / 2.0);
        client.send(goodLimit);
        
        RejectOrderResponse reject = client.last(RejectOrderResponse.class);
        Assert.assertEquals(RejectOrderType.INVALID_CONDITION, reject.getRejectType());
    }
    
    @Test
    public void limitPriceRoundedTest() {
        goodLimit.setPrice(Symbol.MON.getCoin().doubleValue() * 1.5);
        client.send(goodLimit);
        client.wait(LimitOrderResponse.class);
        
        LimitOrderResponse order = client.last(LimitOrderResponse.class);
        Assert.assertEquals(Symbol.MON.getCoin().doubleValue(), order.getPrice(), 0);
    }
    
    @Test
    public void marketNoInstrumentTest() {
        goodMarket.setInstrument(null);
        client.send(goodMarket);
        
        RejectOrderResponse reject = client.last(RejectOrderResponse.class);
        Assert.assertEquals(RejectOrderType.INVALID_CONDITION, reject.getRejectType());
    }
    
    @Test
    public void marketNoSideTest() {
        goodMarket.setSide(null);
        client.send(goodMarket);
        
        RejectOrderResponse reject = client.last(RejectOrderResponse.class);
        Assert.assertEquals(RejectOrderType.INVALID_CONDITION, reject.getRejectType());
    }
    
    @Test
    public void limitNoInstrumentTest() {
        goodLimit.setInstrument(null);
        client.send(goodLimit);
        
        RejectOrderResponse reject = client.last(RejectOrderResponse.class);
        Assert.assertEquals(RejectOrderType.INVALID_CONDITION, reject.getRejectType());
    }
    
    @Test
    public void limitNoSideTest() {
        goodLimit.setSide(null);
        client.send(goodLimit);
        
        RejectOrderResponse reject = client.last(RejectOrderResponse.class);
        Assert.assertEquals(RejectOrderType.INVALID_CONDITION, reject.getRejectType());
    }
    
    @Test
    public void marketNegativeAmountTest() {
        goodMarket.setAmount(-10.0);
        client.send(goodMarket);
        
        RejectOrderResponse reject = client.last(RejectOrderResponse.class);
        Assert.assertEquals(RejectOrderType.INVALID_CONDITION, reject.getRejectType());
    }
    
    @Test
    public void limitNegativeAmountTest() {
        goodLimit.setAmount(-10.0);
        client.send(goodLimit);
        
        RejectOrderResponse reject = client.last(RejectOrderResponse.class);
        Assert.assertEquals(RejectOrderType.INVALID_CONDITION, reject.getRejectType());
    }
    
    @Test
    public void limitNegativePriceTest() {
        goodLimit.setPrice(-3.3);
        client.send(goodLimit);
        
        RejectOrderResponse reject = client.last(RejectOrderResponse.class);
        Assert.assertEquals(RejectOrderType.INVALID_CONDITION, reject.getRejectType());
    }
}
