package bymihaj;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bymihaj.data.order.LimitOrderResponse;
import bymihaj.data.order.OrderSide;
import bymihaj.data.order.Trade;

public class UserTest {

    protected User user;
    
    @Before
    public void before() {
        user = new User(null, null);
        user.setBank(new Bank());
    }
    
    @Test
    public void freeAssetsEmptyTest() {
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue(), user.getFreeAsset(Symbol.STK).doubleValue(), 0);
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue(), user.getFreeAsset(Symbol.MON).doubleValue(), 0);
    }
    
    @Test
    public void freeAssetsOneBuyTest() {
        double amount = 10.0;
        double price = 5.0;
        LimitOrderResponse order = new LimitOrderResponse();
        order.setId(1);
        order.setAmount(amount);
        order.setPrice(price);
        order.setSide(OrderSide.BUY);
        user.addOrder(order.getId(), order);
        
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue() - amount * price, user.getFreeAsset(Symbol.MON).doubleValue(), 0);
    }
    
    @Test
    public void freeAssetsBuyPartialFillTest() {
        double amount = 10.0;
        double filled = 3.0;
        double price = 5.0;
        LimitOrderResponse order = new LimitOrderResponse();
        order.setId(1);
        order.setAmount(amount);
        order.setPrice(price);
        order.setSide(OrderSide.BUY);
        order.addTrade(new Trade(0, filled, price));
        user.addOrder(order.getId(), order);
        
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue() - (amount - filled) * price, user.getFreeAsset(Symbol.MON).doubleValue(), 0);
    }
    
    @Test
    public void freeAssetsTwoBuyTest() {
        double amount = 10.0;
        double price = 5.0;
        
        LimitOrderResponse o1 = new LimitOrderResponse();
        o1.setId(1);
        o1.setAmount(amount);
        o1.setPrice(price);
        o1.setSide(OrderSide.BUY);
        user.addOrder(o1.getId(), o1);
        
        LimitOrderResponse o2 = new LimitOrderResponse();
        o2.setId(2);
        o2.setAmount(amount);
        o2.setPrice(price);
        o2.setSide(OrderSide.BUY);
        user.addOrder(o2.getId(), o2);
        
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue() - amount * price *2, user.getFreeAsset(Symbol.MON).doubleValue(), 0);
    }
    
    @Test
    public void freeAssetsOneSellTest() {
        double amount = 20;
        LimitOrderResponse order = new LimitOrderResponse();
        order.setId(1);
        order.setAmount(amount);
        order.setSide(OrderSide.SELL);
        user.addOrder(order.getId(), order);
        
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue() - amount, user.getFreeAsset(Symbol.STK).doubleValue(), 0);
    }
    
    @Test
    public void freeAssetsSellPartialFilledTest() {
        double amount = 20;
        double filled = 15;
        LimitOrderResponse order = new LimitOrderResponse();
        order.setId(1);
        order.setAmount(amount);
        order.addTrade(new Trade(0, filled, 100.0));
        order.setSide(OrderSide.SELL);
        user.addOrder(order.getId(), order);
        
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue() - amount + filled, user.getFreeAsset(Symbol.STK).doubleValue(), 0);
    }
    
    @Test
    public void freeAssetsTwoSellTest() {
        double amount = 12.0;
        
        LimitOrderResponse o1 = new LimitOrderResponse();
        o1.setId(1);
        o1.setAmount(amount);
        o1.setSide(OrderSide.SELL);
        user.addOrder(o1.getId(), o1);
        
        LimitOrderResponse o2 = new LimitOrderResponse();
        o2.setId(2);
        o2.setAmount(amount);
        o2.setSide(OrderSide.SELL);
        user.addOrder(o2.getId(), o2);
        
        Assert.assertEquals(Bank.DEF_AMOUNT.doubleValue() - amount *2 , user.getFreeAsset(Symbol.STK).doubleValue(), 0);
    }
}
