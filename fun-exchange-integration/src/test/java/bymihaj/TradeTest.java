package bymihaj;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bymihaj.data.order.CancelOrderRequest;
import bymihaj.data.order.CancelOrderResponse;
import bymihaj.data.order.LimitOrderRequest;
import bymihaj.data.order.LimitOrderResponse;
import bymihaj.data.order.MarketOrderRequest;
import bymihaj.data.order.MarketOrderResponse;
import bymihaj.data.order.OrderSide;
import bymihaj.data.order.OrderStatusRequest;
import bymihaj.data.order.OrderStatusResponse;
import bymihaj.data.order.RejectOrderResponse;
import bymihaj.data.order.RejectOrderType;
import bymihaj.data.order.Trade;

public class TradeTest {
    
    final long ping = 100;

    protected Server server;
    protected SocketEmulation client;

    @Before
    public void before() {
        server = new Server(new InetSocketAddress(0));
        client = new SocketEmulation(server);
        IntegrationHelper.login(server, client);
    }

    @Test(timeout=ping)
    public void rejectMarketOrderTest() {
        client.marketBuy(1.0);
        client.wait(RejectOrderResponse.class);

        Assert.assertFalse(client.filter(RejectOrderResponse.class).isEmpty());
        Assert.assertEquals(RejectOrderType.NO_LIQUIDITY, client.last(RejectOrderResponse.class).getRejectType());
    }

    @Test(timeout=ping)
    public void registerLimitOrderTest() {
        client.limitBuy(10.0, 0.1);
        client.wait(LimitOrderResponse.class);
        
        LimitOrderResponse result = client.filter(LimitOrderResponse.class).get(0);
        Assert.assertEquals(0, result.getFilledAmount(), 0);
    }

    public void orderStatusEmptyTest() {
        OrderStatusRequest req = new OrderStatusRequest();
        client.send(req);

        OrderStatusResponse result = client.filter(OrderStatusResponse.class).get(0);
        Assert.assertTrue(result.getOrders().isEmpty());
    }

    @Test(timeout=ping)
    public void orderStatus1Test() {
        LimitOrderRequest order = client.limitBuy(1.0, 0.1);
        
        client.wait(AssetsResponse.class);
        OrderStatusRequest req = new OrderStatusRequest();
        client.send(req);
        
        OrderStatusResponse result = client.filter(OrderStatusResponse.class).get(0);
        Assert.assertEquals(1, result.getOrders().size());
        Assert.assertEquals(order.getPrice(), result.getOrders().get(0).getPrice(), 0);
    }

    @Test(timeout=ping)
    public void orderStatus2Test() {
        client.limitBuy(1.0, 0.1);
        client.wait(AssetsResponse.class);
        client.limitBuy(1.0, 0.1);
        client.wait(AssetsResponse.class);
        
        OrderStatusRequest req = new OrderStatusRequest();
        client.send(req);

        OrderStatusResponse result = client.filter(OrderStatusResponse.class).get(0);
        Assert.assertEquals(2, result.getOrders().size());
        Assert.assertNotEquals(result.getOrders().get(0), result.getOrders().get(1));
    }

    @Test(timeout=ping)
    public void reloginStatusTest() {
        client.limitBuy(1.0, 0.1);
        client.wait(AssetsResponse.class);
        
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

    @Test(timeout=ping)
    public void firstTradeTest() {
        LimitOrderRequest limit = client.limitSell(10.0, 5.0);
        client.wait(AssetsResponse.class);
        MarketOrderRequest market = client.marketBuy(10.0);
        client.wait(MarketOrderResponse.class);

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

    @Test(timeout=ping)
    public void limitPartialFillBeforePoolTest() {
        // #1
        LimitOrderRequest counterParty = client.limitSell(5.0, 1.0);
        client.wait(AssetsResponse.class);
        // #2
        LimitOrderRequest order = client.limitBuy(10.0, 1.0);
        client.wait(AssetsResponse.class);
        client.wait(AssetsResponse.class);

        
        
        List<LimitOrderResponse> all = client.filter(LimitOrderResponse.class);
        List<LimitOrderResponse> counterExe = all.stream().filter(c -> c.getId() == 1).collect(Collectors.toList());
        List<LimitOrderResponse> orderExe = all.stream().filter(c -> c.getId() == 2).collect(Collectors.toList());

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

    @Test(timeout=ping)
    public void simpleChargeBankTest() {
        double amount = 10.0;
        client.limitSell(amount, 1.0);
        client.wait(AssetsResponse.class);

        AssetsResponse assets = client.last(AssetsResponse.class);
        double stkBank = assets.getProperties().get(SocketEmulation.DEF_INST.getPrimary()).getAmount().doubleValue();
        Assert.assertEquals(amount, Bank.DEF_AMOUNT.doubleValue() - stkBank, 0);
    }

    @Test(timeout=ping)
    public void selfTradeBankTest() {
        double amount = 10.0;
        client.limitSell(amount, 3.0);
        client.wait(AssetsResponse.class);
        client.marketBuy(amount);
        client.wait(MarketOrderResponse.class);

        AssetsResponse assets = client.last(AssetsResponse.class);
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue(),
                assets.getProperties().get(Symbol.MON).getAmount().doubleValue(), 0);
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue(),
                assets.getProperties().get(Symbol.STK).getAmount().doubleValue(), 0);
    }

    @Test(timeout=ping)
    public void realTradeBankTest() {
        SocketEmulation second = new SocketEmulation(server);
        IntegrationHelper.login(server, second);

        double startBank = Bank.DEF_AMOUNT.doubleValue();
        double amount = 10.0;
        client.limitSell(amount, 1.0);
        client.wait(AssetsResponse.class);
        second.marketBuy(amount);
        second.wait(AssetsResponse.class);
        
        AssetsResponse clientAssets = client.last(AssetsResponse.class);
        Assert.assertEquals(startBank - amount, clientAssets.getProperties().get(Symbol.STK).getAmount().doubleValue(),
                0);
        Assert.assertEquals(startBank + amount, clientAssets.getProperties().get(Symbol.MON).getAmount().doubleValue(),
                0);

        AssetsResponse secondAssets = second.last(AssetsResponse.class);
        Assert.assertEquals(startBank + amount, secondAssets.getProperties().get(Symbol.STK).getAmount().doubleValue(),
                0);
        Assert.assertEquals(startBank - amount, secondAssets.getProperties().get(Symbol.MON).getAmount().doubleValue(),
                0);
    }

    @Test(timeout=ping)
    public void rejectLimitOrderByBankTest() {
        client.limitSell(Bank.DEF_AMOUNT.doubleValue() * 10, 1.0);
        client.wait(RejectOrderResponse.class);
        Assert.assertTrue(client.filter(LimitOrderResponse.class).isEmpty());
        Assert.assertEquals(RejectOrderType.NO_ASSET, client.last(RejectOrderResponse.class).getRejectType());
    }

    @Test(timeout=ping)
    public void rejectMarketOrderByBankTest() {
        client.marketSell(Bank.DEF_AMOUNT.doubleValue() * 10);
        client.wait(RejectOrderResponse.class);
        Assert.assertTrue(client.filter(LimitOrderResponse.class).isEmpty());
        Assert.assertEquals(RejectOrderType.NO_ASSET, client.last(RejectOrderResponse.class).getRejectType());
    }

    @Test(timeout=ping)
    public void rejectNoLiqiudity2Test() {
        double amount = 10.0;
        client.limitSell(amount, 1.0);
        client.marketBuy(amount);
        client.marketBuy(amount);
        client.wait(RejectOrderResponse.class);
        
        RejectOrderResponse reject = client.last(RejectOrderResponse.class);
        Assert.assertNotNull(reject);
        Assert.assertEquals(RejectOrderType.NO_LIQUIDITY, reject.getRejectType());

        AssetsResponse assets = client.last(AssetsResponse.class);
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue(),
                assets.getProperties().get(Symbol.MON).getAmount().doubleValue(), 0);
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue(),
                assets.getProperties().get(Symbol.STK).getAmount().doubleValue(), 0);
    }

    @Test
    public void noPendingOrderTest() {
        double amount = 10.0;
        client.limitSell(amount, 1.0);
        client.marketBuy(amount);

        client.send(new OrderStatusRequest());

        OrderStatusResponse status = client.last(OrderStatusResponse.class);
        Assert.assertTrue(status.getOrders().isEmpty());
    }

    @Test(timeout=ping)
    public void checkAssetsOnPartialFillTest() {
        double limit = 33;
        double market = 100;
        client.limitSell(limit, 1.0);
        client.marketBuy(market);
        client.wait(MarketOrderResponse.class);
        
        AssetsResponse assets = client.last(AssetsResponse.class);
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue(),
                assets.getProperties().get(Symbol.MON).getAmount().doubleValue(), 0);
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue(),
                assets.getProperties().get(Symbol.STK).getAmount().doubleValue(), 0);
    }

    @Test(timeout=ping)
    public void historyTest() {
        double amount = 10;
        double price = 3.3;
        client.limitSell(amount, price);
        client.marketBuy(amount);
        client.wait(TradeHistory.class);
        
        TradeHistory trade = client.last(TradeHistory.class);
        Assert.assertEquals(amount, trade.getAmount(), 0);
        Assert.assertEquals(price, trade.getPrice(), 0);
        Assert.assertEquals(OrderSide.BUY, trade.getSide());
    }

    @Test(timeout=ping)
    public void historyForAllTest() {
        SocketEmulation secondClient = new SocketEmulation(server);
        IntegrationHelper.login(server, secondClient);

        double amount = 10;
        double price = 3.3;
        client.limitSell(amount, price);
        client.marketBuy(amount);
        secondClient.wait(TradeHistory.class);
        
        TradeHistory trade = secondClient.last(TradeHistory.class);
        Assert.assertEquals(amount, trade.getAmount(), 0);
        Assert.assertEquals(price, trade.getPrice(), 0);
        Assert.assertEquals(OrderSide.BUY, trade.getSide());
    }

    @Test(timeout=ping)
    public void checkPriсeOfTwoTraderTest() {
        SocketEmulation secondClient = new SocketEmulation(server);
        IntegrationHelper.login(server, secondClient);

        double amount = 10.0;
        double price = 35.0;
        double base = Bank.DEF_AMOUNT.doubleValue();

        client.limitSell(amount, price);
        secondClient.marketBuy(amount);
        
        client.wait(TradeHistory.class);
        secondClient.wait(AssetsResponse.class);

        AssetsResponse sellerResponse = client.last(AssetsResponse.class);
        Map<Symbol, Property> seller = sellerResponse.getProperties();
        AssetsResponse buyerResponse = secondClient.last(AssetsResponse.class);
        Map<Symbol, Property> buyer = buyerResponse.getProperties();

        Assert.assertEquals(base - amount, seller.get(Symbol.STK).getAmount().doubleValue(), 0);
        Assert.assertEquals(base + amount * price, seller.get(Symbol.MON).getAmount().doubleValue(), 0);

        Assert.assertEquals(base + amount, buyer.get(Symbol.STK).getAmount().doubleValue(), 0);
        Assert.assertEquals(base - amount * price, buyer.get(Symbol.MON).getAmount().doubleValue(), 0);
    }

    @Test(timeout=ping)
    public void checkPriсeOfTwoTraderReverTest() {
        SocketEmulation secondClient = new SocketEmulation(server);
        IntegrationHelper.login(server, secondClient);

        double amount = 10.0;
        double price = 35.0;
        double base = Bank.DEF_AMOUNT.doubleValue();

        client.limitBuy(amount, price);
        secondClient.marketSell(amount);

        client.wait(TradeHistory.class);
        secondClient.wait(AssetsResponse.class);
        
        AssetsResponse sellerResponse = client.last(AssetsResponse.class);
        Map<Symbol, Property> seller = sellerResponse.getProperties();
        AssetsResponse buyerResponse = secondClient.last(AssetsResponse.class);
        Map<Symbol, Property> buyer = buyerResponse.getProperties();

        Assert.assertEquals(base + amount, seller.get(Symbol.STK).getAmount().doubleValue(), 0);
        Assert.assertEquals(base - amount * price, seller.get(Symbol.MON).getAmount().doubleValue(), 0);

        Assert.assertEquals(base - amount, buyer.get(Symbol.STK).getAmount().doubleValue(), 0);
        Assert.assertEquals(base + amount * price, buyer.get(Symbol.MON).getAmount().doubleValue(), 0);
    }

    @Test(timeout=ping)
    public void cancelOrderInStatusTest() {
        client.limitSell(10, 10);
        client.wait(AssetsResponse.class);
        LimitOrderResponse order = client.last(LimitOrderResponse.class);
        
        CancelOrderRequest msg = new CancelOrderRequest();
        msg.setId(order.getId());
        client.send(msg);
        client.wait(CancelOrderResponse.class);
        client.wait(OrderBook.class);
        
        CancelOrderResponse response = client.last(CancelOrderResponse.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(order.getId(), response.getId());

        OrderStatusResponse status = client.last(OrderStatusResponse.class);
        Assert.assertTrue(status.getOrders().isEmpty());
    }

    @Test(timeout=ping)
    public void cancelOrderInLiquidityTest() {
        client.limitSell(10, 10);
        client.wait(AssetsResponse.class);
        LimitOrderResponse order = client.last(LimitOrderResponse.class);

        CancelOrderRequest msg = new CancelOrderRequest();
        msg.setId(order.getId());
        client.send(msg);

        client.marketBuy(10);
        client.wait(RejectOrderResponse.class);
        
        RejectOrderResponse reject = client.last(RejectOrderResponse.class);
        Assert.assertNotNull(reject);
        Assert.assertEquals(RejectOrderType.NO_LIQUIDITY, reject.getRejectType());
    }

    @Test(timeout=ping)
    public void tryToCancelNoOwnedOrderTest() {
        SocketEmulation secondClient = new SocketEmulation(server);
        IntegrationHelper.login(server, secondClient);

        client.limitSell(10, 10);
        client.wait(LimitOrderResponse.class);
        LimitOrderResponse order = client.last(LimitOrderResponse.class);

        CancelOrderRequest msg = new CancelOrderRequest();
        msg.setId(order.getId());
        secondClient.send(msg);

        RejectOrderResponse reject = secondClient.last(RejectOrderResponse.class);
        Assert.assertNotNull(reject);
        Assert.assertEquals(RejectOrderType.NO_ID, reject.getRejectType());
    }

    @Test(timeout=ping)
    public void orderbookBothSideTest() {
        double sellAmount = 10;
        double sellPrice = 20;
        double buyAmount = 30;
        double buyPrice = 5;

        client.limitSell(sellAmount, sellPrice);
        client.limitBuy(buyAmount, buyPrice);
        client.wait(OrderBook.class);
        client.wait(OrderBook.class);
        
        OrderBook book = client.last(OrderBook.class);
        Assert.assertFalse(book.getSellLevels().isEmpty());
        Assert.assertTrue(book.getSellLevels().containsKey(sellPrice));
        Assert.assertEquals(sellAmount, book.getSellLevels().get(sellPrice), 0);
        Assert.assertFalse(book.getBuyLevels().isEmpty());
        Assert.assertTrue(book.getBuyLevels().containsKey(buyPrice));
        Assert.assertEquals(buyAmount, book.getBuyLevels().get(buyPrice), 0);

    }

    @Test(timeout=ping)
    public void orderbookLevelAgregationTest() {
        double a1 = 5;
        double a2 = 3;
        double price = 1.0;

        client.limitSell(a1, price);
        client.limitSell(a2, price);
        client.wait(OrderBook.class);
        client.wait(OrderBook.class);

        OrderBook book = client.last(OrderBook.class);
        Assert.assertTrue(book.getSellLevels().containsKey(price));
        Assert.assertEquals(a1 + a2, book.getSellLevels().get(price), 0);

    }

    @Test(timeout=ping)
    public void orderbookPartialFillTest() {
        double startAmount = 10;
        double buyAmount = 3;
        double price = 1.0;

        client.limitSell(startAmount, price);
        client.marketBuy(buyAmount);
        client.wait(OrderBook.class);
        client.wait(OrderBook.class);
        
        OrderBook book = client.last(OrderBook.class);
        Assert.assertTrue(book.getSellLevels().containsKey(price));
        Assert.assertEquals(startAmount - buyAmount, book.getSellLevels().get(price), 0);
    }

    @Test(timeout=ping)
    public void coinTradeTest() {
        SocketEmulation secondClient = new SocketEmulation(server);
        IntegrationHelper.login(server, secondClient);

        double amountSell = 10.0;
        double amountBuy = 0.6;
        double price = 0.33;
        double base = Bank.DEF_AMOUNT.doubleValue();

        client.limitSell(amountSell, price);
        secondClient.marketBuy(amountBuy);
        
        client.wait(OrderBook.class);
        client.wait(OrderBook.class);
        
        AssetsResponse sellerResponse = client.last(AssetsResponse.class);
        Map<Symbol, Property> seller = sellerResponse.getProperties();
        AssetsResponse buyerResponse = secondClient.last(AssetsResponse.class);
        Map<Symbol, Property> buyer = buyerResponse.getProperties();

        double monAmount = new BigDecimal(amountBuy * price).setScale(Symbol.MON.getCoin().scale(), RoundingMode.HALF_DOWN).doubleValue();
        Assert.assertEquals(base - amountSell, seller.get(Symbol.STK).getAmount().doubleValue(), 0);
        Assert.assertEquals(base + monAmount, seller.get(Symbol.MON).getAmount().doubleValue(), 0);

        Assert.assertEquals(base + amountBuy, buyer.get(Symbol.STK).getAmount().doubleValue(), 0);
        Assert.assertEquals(base - monAmount, buyer.get(Symbol.MON).getAmount().doubleValue(), 0);
    }

    @Test(timeout=ping)
    public void zeroAmountInOrderbookTest() {
        double amount = 10;
        double price = 1;
        client.limitSell(amount, price);
        client.limitBuy(amount, price);
        
        client.wait(OrderBook.class);
        client.wait(OrderBook.class);

        OrderBook orderbook = client.last(OrderBook.class);
        Assert.assertTrue(orderbook.getBuyLevels().isEmpty());
        Assert.assertTrue(orderbook.getSellLevels().isEmpty());

        List<LimitOrderResponse> orders = client.filter(LimitOrderResponse.class);
        Assert.assertEquals(price, orders.get(orders.size() - 2).getAveragePrice(), 0);
        Assert.assertEquals(price, orders.get(orders.size() - 1).getAveragePrice(), 0);

        AssetsResponse assets = client.last(AssetsResponse.class);
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue(),
                assets.getProperties().get(Symbol.STK).getAmount().doubleValue(), 0);
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue(),
                assets.getProperties().get(Symbol.MON).getAmount().doubleValue(), 0);
    }

    @Test(timeout=ping)
    public void noOrderbookAfterCancelTest() {
        client.limitBuy(10.0, 1.0);
        client.wait(AssetsResponse.class);
        LimitOrderResponse registered = client.last(LimitOrderResponse.class);
        
        CancelOrderRequest cancel = new CancelOrderRequest();
        cancel.setId(registered.getId());
        client.send(cancel);
        client.wait(CancelOrderResponse.class);
        client.wait(OrderBook.class);
        
        OrderBook orderbook = client.last(OrderBook.class);
        Assert.assertTrue(orderbook.getBuyLevels().isEmpty());
    }

    @Test(timeout=ping)
    public void multiLevelMarketExectionTest() {
        SocketEmulation second = new SocketEmulation(server);
        IntegrationHelper.login(server, second);

        double amount = 10.0;
        double price = 3.0;
        double extraPrice = 200.0;
        client.limitSell(amount, extraPrice);
        client.limitSell(amount, price);
        second.marketBuy(amount * 2);

        client.wait(OrderBook.class);
        client.wait(OrderBook.class);
        client.wait(OrderBook.class);
        
        double bank = Bank.DEF_AMOUNT.doubleValue();
        AssetsResponse assets = second.last(AssetsResponse.class);
        Assert.assertEquals(bank + amount + (bank - amount * price) / extraPrice,
                assets.getProperties().get(Symbol.STK).getAmount().doubleValue(), 0);
        Assert.assertEquals(0, assets.getProperties().get(Symbol.MON).getAmount().doubleValue(), 0);
    }

    @Test(timeout=ping)
    public void multiLevelLimitExectionTest() {
        double price = 200.0;
        double amount = 10.0;
        client.limitSell(amount, price);
        client.limitSell(amount, 3.0);
        client.limitBuy(amount * 2, price);
        
        client.wait(OrderBook.class);
        client.wait(OrderBook.class);
        client.wait(OrderBook.class);

        OrderBook book = client.last(OrderBook.class);
        Assert.assertEquals(amount / 2.0, book.getBuyLevels().get(price), 0);
        Assert.assertEquals(amount / 2.0, book.getSellLevels().get(price), 0);
    }

    @Test(timeout=ping)
    public void overAssetsMarketBuyTest() {
        SocketEmulation second = new SocketEmulation(server);
        IntegrationHelper.login(server, second);

        double amount = 10;
        double price = 10000;
        client.limitSell(amount, price);
        second.marketBuy(amount);
        
        client.wait(OrderBook.class);
        client.wait(OrderBook.class);

        double base = Bank.DEF_AMOUNT.doubleValue();
        double expectedAmount = Bank.DEF_AMOUNT.doubleValue() / price;
        AssetsResponse assets = client.last(AssetsResponse.class);
        Assert.assertEquals(base - amount, assets.getProperties().get(Symbol.STK).getAmount().doubleValue(), 0);
        Assert.assertEquals(base + expectedAmount * price,
                assets.getProperties().get(Symbol.MON).getAmount().doubleValue(), 0);

        AssetsResponse assetsSecond = second.last(AssetsResponse.class);
        Assert.assertEquals(base + expectedAmount,
                assetsSecond.getProperties().get(Symbol.STK).getAmount().doubleValue(), 0);
        Assert.assertEquals(base - expectedAmount * price,
                assetsSecond.getProperties().get(Symbol.MON).getAmount().doubleValue(), 0);
    }

    @Test(timeout=ping)
    public void overAssetsMarketSellTest() {
        SocketEmulation second = new SocketEmulation(server);
        IntegrationHelper.login(server, second);

        double amount = 10;
        double price = 10000;
        client.limitBuy(amount, price);
        client.wait(AssetsResponse.class);

        RejectOrderResponse reject = client.last(RejectOrderResponse.class);
        Assert.assertNotNull(reject);
        Assert.assertEquals(RejectOrderType.NO_ASSET, reject.getRejectType());

        // TODO REF check starting balance
        AssetsResponse assets = client.last(AssetsResponse.class);
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue(),
                assets.getProperties().get(Symbol.STK).getAmount().doubleValue(), 0);
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue(),
                assets.getProperties().get(Symbol.MON).getAmount().doubleValue(), 0);
    }

    @Test(timeout=ping)
    public void overAssetsLimitBuyPartialFillTest() {
        SocketEmulation second = new SocketEmulation(server);
        IntegrationHelper.login(server, second);

        double amount = 10.0;
        double price = 5.0;
        double extraPrice = 10000;
        second.limitSell(amount, price);
        client.limitBuy(amount * 2, extraPrice);

        client.wait(OrderBook.class);
        client.wait(OrderBook.class);
        
        double base = Bank.DEF_AMOUNT.doubleValue();
        AssetsResponse assets = client.last(AssetsResponse.class);
        Assert.assertEquals(base + amount, assets.getProperties().get(Symbol.STK).getAmount().doubleValue(), 0);
        Assert.assertEquals(base - amount * price, assets.getProperties().get(Symbol.MON).getAmount().doubleValue(), 0);

        RejectOrderResponse reject = client.last(RejectOrderResponse.class);
        Assert.assertNotNull(reject);
        Assert.assertEquals(RejectOrderType.NO_ASSET, reject.getRejectType());
    }

    @Test(timeout=ping)
    public void cancelPartialFilledOrderTest() {

        client.limitSell(10.0, 1.0);
        client.wait(LimitOrderResponse.class);
        LimitOrderResponse registered = client.last(LimitOrderResponse.class);
        
        client.marketBuy(5.0);
        client.wait(MarketOrderResponse.class);
        
        CancelOrderRequest cancel = new CancelOrderRequest();
        cancel.setId(registered.getId());
        client.send(cancel);
        client.wait(CancelOrderResponse.class);
        client.wait(OrderBook.class);
        
        OrderBook orderbook = client.last(OrderBook.class);
        Assert.assertTrue(orderbook.getSellLevels().isEmpty());

        // TODO REF check zero balance
        AssetsResponse assets = client.last(AssetsResponse.class);
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue(),
                assets.getProperties().get(Symbol.STK).getAmount().doubleValue(), 0);
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue(),
                assets.getProperties().get(Symbol.MON).getAmount().doubleValue(), 0);

    }

    @Test(timeout=ping)
    public void twoMarketOrderResponseOnMultiLevelExectutionTest() {
        double l1 = 2.0;
        double l2 = 3.0;
        double amount = 10.0;
        client.limitSell(amount, l1);
        client.limitSell(amount, l2);
        client.marketBuy(amount*2);
        
        client.wait(OrderBook.class);
        client.wait(OrderBook.class);
        client.wait(OrderBook.class);
        
        MarketOrderResponse order = client.last(MarketOrderResponse.class);
        List<Trade> trades = order.getTrades();
        Assert.assertEquals(l1, trades.get(0).getPrice(), 0);
        Assert.assertEquals(amount, trades.get(0).getAmount(), 0);
        Assert.assertEquals(l2, trades.get(1).getPrice(), 0);
        Assert.assertEquals(amount, trades.get(1).getAmount(), 0);
    }

    @Test(timeout=ping)
    public void splitLimitMarketOrderTest() {
        SocketEmulation second = new SocketEmulation(server);
        IntegrationHelper.login(server, second);

        double amount = 10;
        double price = 3.0;
        second.limitSell(amount, price);
        client.limitBuy(amount * 2, price);

        client.wait(OrderBook.class);
        client.wait(OrderBook.class);
        
        OrderBook orderbook = client.last(OrderBook.class);
        Assert.assertEquals(10.0, orderbook.getBuyLevels().get(price), 0);
    }
    
    @Test(timeout=ping)
    public void bug1BuyTest() {
        SocketEmulation second = new SocketEmulation(server);
        IntegrationHelper.login(server, second);
        second.limitSell(999.9, 999.9);
        
        client.marketBuy(1.5);
        
        client.wait(OrderBook.class);
        client.wait(OrderBook.class);
        
        MarketOrderResponse order = client.last(MarketOrderResponse.class);
        Assert.assertEquals(1.0, order.getFilledAmount(), 0);
    }
    
    @Test(timeout=ping)
    public void bug2BuyTest() {
        client.limitSell(0.999, 7.848);
        client.marketBuy(0.999);
        
        client.wait(OrderBook.class);
        client.wait(OrderBook.class);
        
        AssetsResponse assets = client.last(AssetsResponse.class);
        Assert.assertEquals("1000.00", assets.getProperties().get(Symbol.MON).getAmount().toPlainString());
    }
    
    @Test(timeout=ping)
    public void bug3BuyTest() {
        client.limitBuy(0.999, 1.075);
        client.wait(OrderBook.class);
        
        AssetsResponse assets = client.last(AssetsResponse.class);
        Assert.assertEquals("999.04", assets.getProperties().get(Symbol.MON).getAmount().toPlainString());
    }
    
    @Test(timeout=ping)
    public void bug4Test() {
        SocketEmulation second = new SocketEmulation(server);
        IntegrationHelper.login(server, second);
        second.limitSell(5.8, 5.3);
        second.limitSell(4.1, 4.45);
        second.limitSell(0.999, 5.58);
        
        client.marketBuy(9.939);
        
        client.wait(OrderBook.class);
        client.wait(OrderBook.class);
        client.wait(OrderBook.class);
        client.wait(OrderBook.class);
        
        AssetsResponse assets = client.last(AssetsResponse.class);
        Assert.assertEquals("951.10", assets.getProperties().get(Symbol.MON).getAmount().toPlainString());
        Assert.assertEquals("1009.9", assets.getProperties().get(Symbol.STK).getAmount().toPlainString());
    }
}
