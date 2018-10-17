package bymihaj.client;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.junit.client.GWTTestCase;

import bymihaj.AccountRequest;
import bymihaj.AccountResponse;
import bymihaj.AssetsRequest;
import bymihaj.AssetsResponse;
import bymihaj.Instrument;
import bymihaj.LoginRequest;
import bymihaj.LoginResponse;
import bymihaj.MessageHolder;
import bymihaj.OrderBook;
import bymihaj.OrderBookRequest;
import bymihaj.Symbol;
import bymihaj.TradeHistory;
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

public class GwtTestGwtParser extends GWTTestCase {

    protected GwtParser parser;
    protected String user = "ABCxyz";
    protected String pass = "#$@";
    
    @Override
    public String getModuleName() {
        return "bymihaj.WebClientJUnit";
    }
    
    public void gwtSetUp () {
        parser = new GwtParser();
    }
    
    public void testMessageHolderTo() {
        MessageHolder holder = new MessageHolder();
        holder.setName("someName");
        holder.setObject("someObject");
        GwtParser parser = new GwtParser();
        String json = parser.toJson(holder);
        
        JSONValue res = JSONParser.parseStrict(json);
        assertEquals(holder.getName(), res.isObject().get("name").isString().stringValue());
        assertEquals(holder.getObject(), res.isObject().get("object").isString().stringValue());
    }
    
    public void testMessageHolderFrom() {
        
        MessageHolder holder = new MessageHolder();
        holder.setName("nameT");
        holder.setObject("objT");
        
        GwtParser parser = new GwtParser();
        String json = parser.toJson(holder);
        
        MessageHolder res = parser.fromJson(json, MessageHolder.class);
        assertEquals(holder.getName(), res.getName());
        assertEquals(holder.getObject(), res.getObject());
    }
    
    public void testAccountRequestTo() {
        AccountRequest request = new AccountRequest();
        GwtParser parser = new GwtParser();
        String json = parser.toJson(request);
        assertEquals("{}", json);
    }
    
    public void testAccountResponsetFrom() {
        String user = "ABCxyz";
        String pass = "xyz";
        JSONObject obj = new JSONObject();
        obj.put("user", new JSONString(user));
        obj.put("pass", new JSONString(pass));
        
        GwtParser parser = new GwtParser();
        AccountResponse res = parser.fromJson(obj.toString(), AccountResponse.class);
        assertEquals(user, res.getUser());
        assertEquals(pass, res.getPass());
    }
    
    public void testLoginRequestTo() {
        LoginRequest login = new LoginRequest();
        login.setUser(user);
        login.setPass(pass);
        
        String json = parser.toJson(login);
        JSONObject jo = JSONParser.parseStrict(json).isObject();
        assertEquals(user, jo.get("user").isString().stringValue());
        assertEquals(pass, jo.get("pass").isString().stringValue());
    }
    
    public void testLoginResponseFrom() {
        JSONObject jo = new JSONObject();
        jo.put("status", new JSONString(LoginResponse.Status.OK.name()));
        
        LoginResponse resp = parser.fromJson(jo.toString(), LoginResponse.class);
        assertEquals(LoginResponse.Status.OK, resp.getStatus());
    }
    
    public void testAssetsRequestTo() {
        AssetsRequest ar = new AssetsRequest();
        String json = parser.toJson(ar);
        assertEquals("{}", json);
    }

    public void testAssetsResponseFrom() {
        double monAmount = 555;
        JSONObject monJo = new JSONObject();
        monJo.put("name", new JSONString(Symbol.MON.name()));
        monJo.put("amount", new JSONNumber(monAmount));
        
        double stkAmount = 777;
        JSONObject stkJo = new JSONObject();
        stkJo.put("name", new JSONString(Symbol.STK.name()));
        stkJo.put("amount", new JSONNumber(stkAmount));
        
        JSONObject map = new JSONObject();
        map.put(Symbol.MON.name(), monJo);
        map.put(Symbol.STK.name(), stkJo);
        
        JSONObject jo = new JSONObject();
        jo.put("propertyMap", map);
        
        AssetsResponse ar = parser.fromJson(jo.toString(), AssetsResponse.class);
        assertEquals(monAmount, ar.getProperties().get(Symbol.MON).getAmount().doubleValue());
        assertEquals(stkAmount, ar.getProperties().get(Symbol.STK).getAmount().doubleValue());
    }
    
    public void testRejectMarketOrderFrom() {
        String reason = "some reason";
        RejectOrderType type = RejectOrderType.NO_LIQUIDITY;
        JSONObject jo = new JSONObject();
        jo.put("reason", new JSONString(reason));
        jo.put("rejectType", new JSONString(type.name()));
        
        RejectOrderResponse resp = parser.fromJson(jo.toString(), RejectOrderResponse.class);
        assertEquals(reason, resp.getReason());
        assertEquals(type, resp.getRejectType());
    }
    
    public void testLimitOrderRequestTo() {
        LimitOrderRequest order = new LimitOrderRequest();
        order.setAmount(10);
        order.setPrice(3.3);
        order.setSide(OrderSide.BUY);
        order.setInstrument(Instrument.STKMON);
        
        String json = parser.toJson(order);
        JSONObject jo = JSONParser.parseStrict(json).isObject();
        assertEquals(order.getAmount(), jo.get("amount").isNumber().doubleValue());
        assertEquals(order.getPrice(), jo.get("price").isNumber().doubleValue());
        assertEquals(order.getSide(), OrderSide.valueOf(jo.get("side").isString().stringValue()));
        assertEquals(order.getInstrument(), Instrument.valueOf(jo.get("instrument").isString().stringValue()));
    }
    
    public void testLimitOrderResponseFrom() {
        double price = 0.1;
        double filledAmount = 0.2;
        double filledPrice = 0.3;
        double amount = 0.4;
        Instrument instrument = Instrument.STKMON;
        long id = 33;
        OrderSide side = OrderSide.BUY;
        
        JSONObject jo = new JSONObject();
        jo.put("price", new JSONNumber(price));
        jo.put("filledAmount", new JSONNumber(filledAmount));
        jo.put("filledPrice", new JSONNumber(filledPrice));
        jo.put("amount", new JSONNumber(amount));
        jo.put("instrument", new JSONString(instrument.name()));
        jo.put("id", new JSONNumber(id));
        jo.put("side", new JSONString(side.name()));
        
        LimitOrderResponse order = parser.fromJson(jo.toString(), LimitOrderResponse.class);
        assertEquals(price, order.getPrice());
        assertEquals(filledAmount, order.getFilledAmount());
        assertEquals(filledPrice, order.getFilledPrice());
        assertEquals(amount, order.getAmount());
        assertEquals(instrument, order.getInstrument());
        assertEquals(id, order.getId());
        assertEquals(side, order.getSide());
        
    }
    
    public void testMarketOrderRequestTo() {
        double amount = 11;
        Instrument inst = Instrument.STKMON;
        OrderSide side = OrderSide.BUY;
        MarketOrderRequest order = new MarketOrderRequest();
        order.setAmount(amount);
        order.setInstrument(inst);
        order.setSide(side);
        
        String json = parser.toJson(order);
        JSONObject jo = JSONParser.parseStrict(json).isObject();
        assertEquals(order.getAmount(), jo.get("amount").isNumber().doubleValue());
        assertEquals(order.getSide(), OrderSide.valueOf(jo.get("side").isString().stringValue()));
        assertEquals(order.getInstrument(), Instrument.valueOf(jo.get("instrument").isString().stringValue()));
    }
    
    public void testMarketOrderResponseFrom() {
        double filledAmount = 0.2;
        double filledPrice = 0.3;
        double amount = 0.4;
        Instrument instrument = Instrument.STKMON;
        long id = 33;
        OrderSide side = OrderSide.BUY;
        
        JSONObject jo = new JSONObject();
        jo.put("filledAmount", new JSONNumber(filledAmount));
        jo.put("filledPrice", new JSONNumber(filledPrice));
        jo.put("amount", new JSONNumber(amount));
        jo.put("instrument", new JSONString(instrument.name()));
        jo.put("id", new JSONNumber(id));
        jo.put("side", new JSONString(side.name()));
        
        MarketOrderResponse order = parser.fromJson(jo.toString(), MarketOrderResponse.class);
        assertEquals(filledAmount, order.getFilledAmount());
        assertEquals(filledPrice, order.getFilledPrice());
        assertEquals(amount, order.getAmount());
        assertEquals(instrument, order.getInstrument());
        assertEquals(id, order.getId());
        assertEquals(side, order.getSide());
    }
    
    public void testOrderStatusRequestTo() {
        OrderStatusRequest osr = new OrderStatusRequest();
        String json = parser.toJson(osr);
        assertEquals("{}", json);
    }
    
    public void testOrderStatusResponseFrom() {
        double price = 0.1;
        double filledAmount = 0.2;
        double filledPrice = 0.3;
        double amount = 0.4;
        Instrument instrument = Instrument.STKMON;
        long id = 33;
        OrderSide side = OrderSide.BUY;
        
        JSONObject jo = new JSONObject();
        jo.put("price", new JSONNumber(price));
        jo.put("filledAmount", new JSONNumber(filledAmount));
        jo.put("filledPrice", new JSONNumber(filledPrice));
        jo.put("amount", new JSONNumber(amount));
        jo.put("instrument", new JSONString(instrument.name()));
        jo.put("id", new JSONNumber(id));
        jo.put("side", new JSONString(side.name()));
        
        JSONArray array = new JSONArray();
        array.set(0, jo);
        
        JSONObject joStat = new JSONObject();
        joStat.put("orders", array);
        
        OrderStatusResponse stat = parser.fromJson(joStat.toString(), OrderStatusResponse.class);
        assertFalse(stat.getOrders().isEmpty());
        assertEquals(id, stat.getOrders().get(0).getId());
    }
    
    public void testCancelOrderRequestTo() {
        long id = 123;
        CancelOrderRequest request = new CancelOrderRequest();
        request.setId(id);
        
        String json = parser.toJson(request);
        JSONObject jo = JSONParser.parseStrict(json).isObject();
        assertEquals(id, (long)jo.get("id").isNumber().doubleValue());
    }
    
    public void testCancelOrderResponseFrom() {
        long id = 456;
        JSONObject jo = new JSONObject();
        jo.put("id", new JSONNumber(id));
        
        CancelOrderResponse resp = parser.fromJson(jo.toString(), CancelOrderResponse.class);
        assertEquals(id, resp.getId());
    }
    
    public void testOrderBookRequestTo() {
        OrderBookRequest req = new OrderBookRequest();
        String json = parser.toJson(req);
        assertEquals("{}", json);
    }
    
    public void testOrderBookFrom() {
        String json = "{\"buyLevels\":{\"5.0\":30.0},\"sellLevels\":{\"20.0\":10.0}}";
        OrderBook book = parser.fromJson(json, OrderBook.class);
        assertEquals(30.0, book.getBuyLevels().get(5.0));
        assertEquals(10.0, book.getSellLevels().get(20.0));
    }
    
    public void testHistoryFrom() {
        String json = "{\"dateTime\":\"Wed Oct 17 10:33:46 EEST 2018\",\"amount\":10.0,\"price\":3.3,\"side\":\"BUY\"}";
        
        TradeHistory trade = parser.fromJson(json, TradeHistory.class);
        
        assertEquals("Wed Oct 17 10:33:46 EEST 2018", trade.getDateTime());
        assertEquals(10.0, trade.getAmount());
        assertEquals(3.3, trade.getPrice());
        assertEquals(OrderSide.BUY, trade.getSide());
    }
}
