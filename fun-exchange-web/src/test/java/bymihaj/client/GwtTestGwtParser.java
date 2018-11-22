package bymihaj.client;

import java.io.IOException;
import java.io.Writer;

import com.google.gwt.dev.json.JsonArray;
import com.google.gwt.dev.json.JsonBoolean;
import com.google.gwt.dev.json.JsonNumber;
import com.google.gwt.dev.json.JsonObject;
import com.google.gwt.dev.json.JsonString;
import com.google.gwt.dev.json.JsonValue;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
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
import bymihaj.LobbyRequest;
import bymihaj.LobbyResponse;
import bymihaj.LoginRequest;
import bymihaj.LoginResponse;
import bymihaj.MessageHolder;
import bymihaj.OrderBook;
import bymihaj.OrderBookRequest;
import bymihaj.RoundRegisterRequest;
import bymihaj.RoundStatus;
import bymihaj.RoundStatusRequest;
import bymihaj.Symbol;
import bymihaj.Team;
import bymihaj.TradeHistory;
import bymihaj.data.game.PlayedRoundRequest;
import bymihaj.data.game.PlayedRoundResponse;
import bymihaj.data.game.RoundTableRequest;
import bymihaj.data.game.RoundTableResponse;
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
        jo.put("amount", new JSONNumber(amount));
        jo.put("instrument", new JSONString(instrument.name()));
        jo.put("id", new JSONNumber(id));
        jo.put("side", new JSONString(side.name()));
        
        JSONArray trades = new JSONArray();
        JSONObject to = new JSONObject();
        to.put("tid", new JSONNumber(1));
        to.put("amount", new JSONNumber(filledAmount));
        to.put("price", new JSONNumber(filledPrice));
        trades.set(0, to);
        jo.put("trades", trades);
        
        LimitOrderResponse order = parser.fromJson(jo.toString(), LimitOrderResponse.class);
        assertEquals(price, order.getPrice());
        assertEquals(filledAmount, order.getFilledAmount());
        assertEquals(filledPrice, order.getAveragePrice());
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
        jo.put("amount", new JSONNumber(amount));
        jo.put("instrument", new JSONString(instrument.name()));
        jo.put("id", new JSONNumber(id));
        jo.put("side", new JSONString(side.name()));
        
        JSONArray trades = new JSONArray();
        JSONObject to = new JSONObject();
        to.put("tid", new JSONNumber(1));
        to.put("amount", new JSONNumber(filledAmount));
        to.put("price", new JSONNumber(filledPrice));
        trades.set(0, to);
        jo.put("trades", trades);
        
        MarketOrderResponse order = parser.fromJson(jo.toString(), MarketOrderResponse.class);
        assertEquals(filledAmount, order.getFilledAmount());
        assertEquals(filledPrice, order.getAveragePrice());
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
        jo.put("amount", new JSONNumber(amount));
        jo.put("instrument", new JSONString(instrument.name()));
        jo.put("id", new JSONNumber(id));
        jo.put("side", new JSONString(side.name()));
        
        JSONArray trades = new JSONArray();
        JSONObject to = new JSONObject();
        to.put("tid", new JSONNumber(1));
        to.put("amount", new JSONNumber(filledAmount));
        to.put("price", new JSONNumber(filledPrice));
        trades.set(0, to);
        jo.put("trades", trades);
        
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
    
    public void testLobbyRequestTo() {
        LobbyRequest req = new LobbyRequest();
        String json = parser.toJson(req);
        assertEquals("{}", json);
    }
    
    public void testRoundRegisterTo() {
        int roundId = 5;
        RoundRegisterRequest req = new RoundRegisterRequest();
        req.setRoundId(roundId);
        req.setJoin(false);
        
        String json = parser.toJson(req);
        JSONObject jo = JSONParser.parseStrict(json).isObject();
        assertEquals(roundId, (int)jo.get("roundId").isNumber().doubleValue());
        assertEquals(false, jo.get("join").isBoolean().booleanValue());
    }
    
    public void testLobbyResponseTo() {
        int roundId = 5;
        long startTime = 100;
        long duration = 700;
        
        JSONObject round = new JSONObject();
        round.put("roundId", new JSONNumber(roundId));
        round.put("startTime", new JSONNumber(startTime));
        round.put("duration", new JSONNumber(duration));
        
        JSONObject jo = new JSONObject();
        
        JSONArray av = new JSONArray();
        av.set(0, round);
        jo.put("available", av);
        
        JSONArray pe = new JSONArray();
        pe.set(0, round);
        jo.put("pending", pe);
        
        JSONArray cur = new JSONArray();
        cur.set(0, round);
        jo.put("current", cur);
        
        LobbyResponse lobby = parser.fromJson(jo.toString(), LobbyResponse.class);
        assertEquals(1, lobby.getAvailable().size());
        assertEquals(1, lobby.getCurrent().size());
        assertEquals(1, lobby.getPending().size());
        assertEquals(roundId, lobby.getAvailable().get(0).getRoundId());
        assertEquals(startTime, lobby.getAvailable().get(0).getStartTime());
        assertEquals(duration, lobby.getAvailable().get(0).getDuration());
        
    }
    
    public void testRoundStatusFrom() {
        int roundId = 5;
        long startTime = 100;
        long duration = 700;
        
        JSONObject round = new JSONObject();
        round.put("roundId", new JSONNumber(roundId));
        round.put("startTime", new JSONNumber(startTime));
        round.put("duration", new JSONNumber(duration));
        
        JSONObject jo = new JSONObject();
        jo.put("round", round);
        jo.put("isStarted", JSONBoolean.getInstance(true));
        jo.put("team", new JSONString(Team.SPECTATOR.name()));
        
        RoundStatus status = parser.fromJson(jo.toString(), RoundStatus.class);
        assertEquals(roundId, status.getRound().getRoundId());
        assertEquals(startTime, status.getRound().getStartTime());
        assertEquals(duration, status.getRound().getDuration());
        assertTrue(status.isStarted());
        assertEquals(Team.SPECTATOR, status.getTeam());
    }
    
    public void testRoundRequestTo() {
        RoundStatusRequest req = new RoundStatusRequest();
        String json = parser.toJson(req);
        assertEquals("{}", json);
    }
    
    public void testPlayedRoundRequestTo() {
        PlayedRoundRequest req = new PlayedRoundRequest();
        String json = parser.toJson(req);
        assertEquals("{}", json);
    }
    
    public void testRoundTableRequestTo() {
        RoundTableRequest req = new RoundTableRequest();
        req.setRoundId(5);
        String json = parser.toJson(req);
        
        JSONObject jo = JSONParser.parseStrict(json).isObject();
        assertEquals(5.0, jo.get("roundId").isNumber().doubleValue());
    }
    
    public void testRoundTableResponseFrom() {
        long roundId = 5;
        int position = 1;
        double amount = 50.0;
        String user = "TestU";
        JSONObject rjo = new JSONObject();
        rjo.put("position", new JSONNumber(position));
        rjo.put("amount", new JSONNumber(amount));
        rjo.put("user", new JSONString(user));
        
        JSONObject jo = new JSONObject();
        jo.put("roundId", new JSONNumber(roundId));
        
        JSONArray green = new JSONArray();
        green.set(0, rjo);
        jo.put("green", green);
        
        JSONArray red = new JSONArray();
        red.set(0, rjo);
        jo.put("red", red);
        
        RoundTableResponse table = parser.fromJson(jo.toString(), RoundTableResponse.class);
        
        assertEquals(roundId, table.getRoundId());
        assertFalse(table.getGreen().isEmpty());
        assertFalse(table.getRed().isEmpty());
        
        assertEquals(position, table.getGreen().get(0).getPosition());
        assertEquals(amount, table.getGreen().get(0).getAmount());
        assertEquals(user, table.getGreen().get(0).getUser());
        
        assertEquals(position, table.getRed().get(0).getPosition());
        assertEquals(amount, table.getRed().get(0).getAmount());
        assertEquals(user, table.getRed().get(0).getUser());
    }
    
    public void testPlayedRoundResponseFrom() {
        JSONObject jo = new JSONObject();
        JSONArray array = new JSONArray();
        array.set(0, new JSONNumber(1));
        array.set(1, new JSONNumber(2));
        jo.put("roundList", array);
        
        PlayedRoundResponse resp = parser.fromJson(jo.toString(), PlayedRoundResponse.class);
        assertFalse(resp.getRoundList().isEmpty());
        assertEquals(1, (long)resp.getRoundList().get(0));
        assertEquals(2, (long)resp.getRoundList().get(1));
    }
    
}
