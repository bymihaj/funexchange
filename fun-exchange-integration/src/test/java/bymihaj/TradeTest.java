package bymihaj;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bymihaj.data.order.LimitOrderRequest;
import bymihaj.data.order.LimitOrderResponse;
import bymihaj.data.order.MarketOrderRequest;
import bymihaj.data.order.MarketOrderResponse;
import bymihaj.data.order.OrderSide;
import bymihaj.data.order.OrderStatusRequest;
import bymihaj.data.order.OrderStatusResponse;
import bymihaj.data.order.RejectOrderResponse;

public class TradeTest {
    
    protected Server server;
    protected SocketEmulation client;
    
    @Before
    public void before() {
        server = new Server(new InetSocketAddress(0));
        client = new SocketEmulation(server);
        IntegrationHelper.login(server, client);
    }

    @Test
    public void rejectMarketOrderTest() {
        MarketOrderRequest req = new MarketOrderRequest();
        req.setAmount(1.0);
        req.setSide(OrderSide.BUY);
        client.send(req);
        
        Assert.assertFalse(client.filter(RejectOrderResponse.class).isEmpty());
    }
    
    @Test
    public void registerLimitOrderTest() {
    	client.limitBuy(10.0, 0.1);
    	
    	LimitOrderResponse result = client.filter(LimitOrderResponse.class).get(0);
    	Assert.assertEquals(0, result.getFilledAmount(), 0);
    }
    
    @Test
    public void orderStatusEmptyTest() {
    	OrderStatusRequest req = new OrderStatusRequest();
    	client.send(req);
    	
    	OrderStatusResponse result = client.filter(OrderStatusResponse.class).get(0);
    	Assert.assertTrue(result.getOrders().isEmpty());
    }
    
    @Test
    public void orderStatus1Test() {
    	LimitOrderRequest order = client.limitBuy(1.0, 0.1);
    	
    	OrderStatusRequest req = new OrderStatusRequest();
    	client.send(req);
    	
    	OrderStatusResponse result = client.filter(OrderStatusResponse.class).get(0);
    	Assert.assertEquals(1, result.getOrders().size());
    	Assert.assertEquals(order.getPrice(), result.getOrders().get(0).getPrice(), 0);
    }
    
    @Test
    public void orderStatus2Test() {
    	client.limitBuy(1.0, 0.1);
    	client.limitBuy(1.0, 0.1);
    	
    	OrderStatusRequest req = new OrderStatusRequest();
    	client.send(req);
    	
    	OrderStatusResponse result = client.filter(OrderStatusResponse.class).get(0);
    	Assert.assertEquals(2, result.getOrders().size());
    	Assert.assertNotEquals(result.getOrders().get(0), result.getOrders().get(1));
    }
    
    @Test
    public void reloginStatusTest() {
    	client.limitBuy(1.0, 0.1);
    	
    	SocketEmulation reloginedClient = new SocketEmulation(server);
    	AccountResponse clientAcc = client.filter(AccountResponse.class).get(0);
    	LoginRequest reloginRequest = new LoginRequest();
    	reloginRequest.setUser(clientAcc.getUser());
    	reloginRequest.setPass(clientAcc.getPass());
    	reloginedClient.send(reloginRequest);
    	
    	OrderStatusRequest req = new OrderStatusRequest();
    	reloginedClient.send(req);
    	
    	OrderStatusResponse result = reloginedClient.filter(OrderStatusResponse.class).get(0);
    	Assert.assertEquals(1, result.getOrders().size());
    }
    
    @Test
    public void firstTradeTest() {
    	LimitOrderRequest limit = client.limitSell(10.0, 5.0);
    	MarketOrderRequest market = client.marketBuy(10.0);
    	
    	List<LimitOrderResponse> limitChain = client.filter(LimitOrderResponse.class);
    	Assert.assertEquals(2, limitChain.size());
    	LimitOrderResponse limitInPool = limitChain.get(0);
    	LimitOrderResponse limitFilled = limitChain.get(1);
    	Assert.assertEquals(0, limitInPool.getFilledAmount(), 0);
    	Assert.assertEquals(limit.getAmount(), limitFilled.getFilledAmount(), 0);
    	
    	MarketOrderResponse marketFilled = client.filter(MarketOrderResponse.class).get(0);
    	Assert.assertEquals(market.getAmount(), marketFilled.getFilledAmount(), 0);
    	
    	OrderStatusRequest statusRequest = new OrderStatusRequest();
    	client.send(statusRequest);
    	
    	OrderStatusResponse statusResponse = client.filter(OrderStatusResponse.class).get(0);
    	Assert.assertTrue(statusResponse.getOrders().isEmpty());
    }
    
    @Test
    public void limitPartialFillBeforePoolTest() {
    	// #1
    	LimitOrderRequest counterParty = client.limitSell(5.0, 1.0);
    	// #2
    	LimitOrderRequest order = client.limitBuy(10.0, 1.0);
    	
    	List<LimitOrderResponse> all = client.filter(LimitOrderResponse.class);
    	List<LimitOrderResponse> counterExe = all.stream().filter( c -> c.getId() == 1).collect(Collectors.toList());
    	List<LimitOrderResponse> orderExe = all.stream().filter( c -> c.getId() == 2).collect(Collectors.toList());
    	
    	Assert.assertEquals(2, counterExe.size());
    	Assert.assertEquals(0, counterExe.get(0).getFilledAmount(), 0);
    	Assert.assertEquals(counterParty.getAmount(), counterExe.get(1).getFilledAmount(), 0);
    	
    	Assert.assertEquals(2, orderExe.size());
    	Assert.assertEquals(0, orderExe.get(0).getFilledAmount(), 0);
    	Assert.assertEquals(counterParty.getAmount(), orderExe.get(1).getFilledAmount(), 0);
    	
    	OrderStatusRequest statusRequest = new OrderStatusRequest();
    	client.send(statusRequest);
    	
    	OrderStatusResponse statusResponse = client.filter(OrderStatusResponse.class).get(0);
    	Assert.assertEquals(1, statusResponse.getOrders().size());
    	Assert.assertEquals(2, statusResponse.getOrders().get(0).getId());
    }
    
    @Test
    public void simpleChargeBankTest() {
    	double amount = 10.0;
    	client.limitSell(amount, 1.0);
    	
    	AssetsResponse assets = client.filter(AssetsResponse.class).get(0);
    	double stkBank = assets.getProperties().get(SocketEmulation.DEF_INST.getPrimary()).getAmount().doubleValue();
    	Assert.assertEquals(amount, Bank.DEF_AMOUNT.doubleValue() - stkBank, 0);
    }
    
    @Test
    public void selfTradeBankTest() {
    	double amount = 10.0;
    	client.limitSell(amount, 3.0);
    	client.marketBuy(amount);
    	
    	AssetsResponse assets = client.filter(AssetsResponse.class).get(2);
    	Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue(), assets.getProperties().get(Symbol.MON).getAmount().doubleValue(), 0);
    	Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue(), assets.getProperties().get(Symbol.STK).getAmount().doubleValue(), 0);
    }
    
    @Test
    public void realTradeBankTest() {
    	SocketEmulation second = new SocketEmulation(server);
    	IntegrationHelper.login(server, second);
    	
    	double startBank = Bank.DEF_AMOUNT.doubleValue();
    	double amount = 10.0;
    	client.limitSell(amount, 1.0);
    	second.marketBuy(amount);
    	
    	AssetsResponse clientAssets = client.last(AssetsResponse.class);
    	Assert.assertEquals(startBank - amount, clientAssets.getProperties().get(Symbol.STK).getAmount().doubleValue(), 0);
    	Assert.assertEquals(startBank + amount, clientAssets.getProperties().get(Symbol.MON).getAmount().doubleValue(), 0);
    	
    	AssetsResponse secondAssets = second.last(AssetsResponse.class);
    	Assert.assertEquals(startBank + amount, secondAssets.getProperties().get(Symbol.STK).getAmount().doubleValue(), 0);
    	Assert.assertEquals(startBank - amount, secondAssets.getProperties().get(Symbol.MON).getAmount().doubleValue(), 0);
    }
    
    @Test
    public void rejectOrderByBankTest() {
    	// TODO
    }
    
}
