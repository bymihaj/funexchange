package bymihaj.client;



import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import bymihaj.AccountRequest;
import bymihaj.AccountResponse;
import bymihaj.AssetsRequest;
import bymihaj.AssetsResponse;
import bymihaj.IJsonParser;
import bymihaj.Instrument;
import bymihaj.LobbyRequest;
import bymihaj.LobbyResponse;
import bymihaj.LoginRequest;
import bymihaj.LoginResponse;
import bymihaj.MessageHolder;
import bymihaj.OrderBook;
import bymihaj.OrderBookRequest;
import bymihaj.Property;
import bymihaj.Round;
import bymihaj.RoundRegisterRequest;
import bymihaj.RoundStatus;
import bymihaj.RoundStatusRequest;
import bymihaj.Symbol;
import bymihaj.Team;
import bymihaj.TradeHistory;
import bymihaj.data.game.PlayedRecord;
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
import bymihaj.data.order.Trade;

public class GwtParser implements IJsonParser{

    protected Logger log = Logger.getLogger(GwtParser.class.getName());
    
    @Override
    public <T> T fromJson(String json, Class<?> classOfT) {
        if(MessageHolder.class.equals(classOfT)) {
            JSONValue jv = JSONParser.parseStrict(json);
            MessageHolder holder = new MessageHolder();
            holder.setName(jv.isObject().get("name").isString().stringValue());
            holder.setObject(jv.isObject().get("object").isString().stringValue());
            return (T) holder;
        } else if(AccountResponse.class.equals(classOfT)) {
            JSONValue jv = JSONParser.parseStrict(json);
            AccountResponse ar = new AccountResponse();
            ar.setUser(jv.isObject().get("user").isString().stringValue());
            ar.setPass(jv.isObject().get("pass").isString().stringValue());
            return (T) ar;
        } else if(LoginResponse.class.equals(classOfT)) {
            JSONObject jo = JSONParser.parseStrict(json).isObject();
            LoginResponse lr = new LoginResponse();
            lr.setStatus(LoginResponse.Status.valueOf(jo.get("status").isString().stringValue()));
            return (T) lr;
        } else if(AssetsResponse.class.equals(classOfT)) {
            return (T) assetsFronJson(json);
        } else if(RejectOrderResponse.class.equals(classOfT)) {
            JSONObject jo = JSONParser.parseStrict(json).isObject();
            RejectOrderResponse reject = new RejectOrderResponse(jo.get("reason").isString().stringValue());
            reject.setRejectType(RejectOrderType.valueOf(jo.get("rejectType").isString().stringValue()));
            return (T) reject;
        } else if(LimitOrderResponse.class.equals(classOfT)) {
            JSONObject jo = JSONParser.parseStrict(json).isObject();
            return (T) limitOrderFromJson(jo);
        } else if(MarketOrderResponse.class.equals(classOfT)) {
            return (T) marketOrderFromJson(json);
        } else if(OrderStatusResponse.class.equals(classOfT)) {
            JSONObject joStat = JSONParser.parseStrict(json).isObject();
            JSONArray array = joStat.get("orders").isArray();
            OrderStatusResponse stat = new OrderStatusResponse();
            for(int i=0; i < array.size(); i++) {
                JSONObject jo = array.get(i).isObject();
                LimitOrderResponse order = limitOrderFromJson(jo);
                stat.getOrders().add(order);
            }
            return (T) stat;
        } else if(CancelOrderResponse.class.equals(classOfT)){
            JSONObject jo = JSONParser.parseStrict(json).isObject();
            CancelOrderResponse resp = new CancelOrderResponse();
            resp.setId((long) jo.get("id").isNumber().doubleValue());
            return (T) resp;
        } else if(OrderBook.class.equals(classOfT)) {
            return (T) orderBookFromJson(json);
        } else if(TradeHistory.class.equals(classOfT)) {
            TradeHistory trade = new TradeHistory();
            JSONObject jo = JSONParser.parseStrict(json).isObject();
            trade.setDateTime(jo.get("dateTime").isString().stringValue());
            trade.setAmount(jo.get("amount").isNumber().doubleValue());
            trade.setPrice(jo.get("price").isNumber().doubleValue());
            trade.setSide(OrderSide.valueOf(jo.get("side").isString().stringValue()));
            return (T) trade;
        } else if(LobbyResponse.class.equals(classOfT)){
            JSONObject jo = JSONParser.parseStrict(json).isObject();
            LobbyResponse lobby = new LobbyResponse();
            lobby.setAvailable(parseRound(jo.get("available").isArray()));
            lobby.setPending(parseRound(jo.get("pending").isArray()));
            lobby.setCurrent(parseRound(jo.get("current").isArray()));
            return (T) lobby;
        } else if(RoundStatus.class.equals(classOfT)) {
            JSONObject jo = JSONParser.parseStrict(json).isObject();
            RoundStatus status = new RoundStatus();
            JSONObject rjo = jo.get("round").isObject();
            Round round = new Round();
            round.setRoundId((long)rjo.get("roundId").isNumber().doubleValue());
            round.setStartTime((long)rjo.get("startTime").isNumber().doubleValue());
            round.setDuration((long)rjo.get("duration").isNumber().doubleValue());
            status.setRound(round);
            status.setStarted(jo.get("isStarted").isBoolean().booleanValue());
            status.setTeam(Team.valueOf(jo.get("team").isString().stringValue()));
            return (T) status;
        } else if(RoundTableResponse.class.equals(classOfT)) {
            JSONObject jo = JSONParser.parseStrict(json).isObject();
            RoundTableResponse resp = new RoundTableResponse();
            resp.setRoundId((long)jo.get("roundId").isNumber().doubleValue());
            JSONArray green = jo.get("green").isArray();
            for(int  i = 0; i < green.size(); i++) {
                JSONObject rjo = green.get(i).isObject();
                PlayedRecord record = new PlayedRecord();
                record.setUser(rjo.get("user").isString().stringValue());
                record.setPosition((int)rjo.get("position").isNumber().doubleValue());
                record.setAmount(rjo.get("amount").isNumber().doubleValue());
                resp.getGreen().add(record);
            }
            JSONArray red = jo.get("red").isArray();
            for(int  i = 0; i < red.size(); i++) {
                JSONObject rjo = red.get(i).isObject();
                PlayedRecord record = new PlayedRecord();
                record.setUser(rjo.get("user").isString().stringValue());
                record.setPosition((int)rjo.get("position").isNumber().doubleValue());
                record.setAmount(rjo.get("amount").isNumber().doubleValue());
                resp.getRed().add(record);
            }
            return (T) resp;
        } else if(PlayedRoundResponse.class.equals(classOfT)) {
            JSONObject jo = JSONParser.parseStrict(json).isObject();
            PlayedRoundResponse resp = new PlayedRoundResponse();
            JSONArray array = jo.get("roundList").isArray();
            for(int i = 0; i < array.size(); i++) {
                JSONNumber number = array.get(i).isNumber();
                resp.getRoundList().add((long)number.doubleValue());
            }
            return (T) resp;
        }
        
        log.info("fromJson failed of class: "+classOfT.getName());
        return null;
    }

    @Override
    public String toJson(Object src) {
        if(src instanceof MessageHolder) {
            MessageHolder obj = (MessageHolder) src;
            JSONObject jo = new JSONObject();
            jo.put("name", new JSONString(obj.getName()));
            jo.put("object", new JSONString(obj.getObject()));
            return jo.toString();
        } else if(src instanceof AccountRequest) {
            return new JSONObject().toString();
        } else if(src instanceof LoginRequest) {
            LoginRequest lr = (LoginRequest) src;
            JSONObject jo = new JSONObject();
            jo.put("user", new JSONString(lr.getUser()));
            jo.put("pass", new JSONString(lr.getPass()));
            return jo.toString();
        } else if(src instanceof AssetsRequest) {
            return new JSONObject().toString();
        } else if(src instanceof LimitOrderRequest) {
            LimitOrderRequest order = (LimitOrderRequest) src;
            JSONObject jo = new JSONObject();
            jo.put("amount", new JSONNumber(order.getAmount()));
            jo.put("price", new JSONNumber(order.getPrice()));
            jo.put("side", new JSONString(order.getSide().name()));
            jo.put("instrument", new JSONString(order.getInstrument().name()));
            return jo.toString();
        } else if(src instanceof MarketOrderRequest) {
            MarketOrderRequest order = (MarketOrderRequest) src;
            JSONObject jo = new JSONObject();
            jo.put("amount", new JSONNumber(order.getAmount()));
            jo.put("side", new JSONString(order.getSide().name()));
            jo.put("instrument", new JSONString(order.getInstrument().name()));
            return jo.toString();
        } else if(src instanceof OrderStatusRequest) {
            return new JSONObject().toString();
        } else if(src instanceof CancelOrderRequest) {
            CancelOrderRequest request = (CancelOrderRequest) src;
            JSONObject jo = new JSONObject();
            jo.put("id", new JSONNumber(request.getId()));
            return jo.toString();
        } else if(src instanceof OrderBookRequest) {
            return new JSONObject().toString();
        } else if(src instanceof LobbyRequest) {
            return new JSONObject().toString();
        } else if(src instanceof RoundRegisterRequest){
            RoundRegisterRequest req = (RoundRegisterRequest) src;
            JSONObject jo = new JSONObject();
            jo.put("roundId", new JSONNumber(req.getRoundId()));
            jo.put("join", JSONBoolean.getInstance(req.isJoin()));
            return jo.toString();
        } else if(src instanceof RoundStatusRequest) {
            return new JSONObject().toString();
        } else if(src instanceof PlayedRoundRequest){
            return new JSONObject().toString();
        } else if(src instanceof RoundTableRequest) {
            RoundTableRequest req = (RoundTableRequest) src;
            JSONObject jo = new JSONObject();
            jo.put("roundId", new JSONNumber(req.getRoundId()));
            return jo.toString();
        }
        
        log.info("toJson failed of class: " + src.getClass().getName());
        return null;
    }
    
    protected AssetsResponse assetsFronJson(String json) {
        Map<Symbol, Property> propertyMap = new HashMap<>();
        AssetsResponse ar = new AssetsResponse(propertyMap);
        JSONObject root = JSONParser.parseStrict(json).isObject();
        JSONObject map = root.get("propertyMap").isObject();
        for(String key : map.keySet()) {
            Property prop = new Property();
            JSONObject propJo = map.get(key).isObject();
            Symbol symbol = Symbol.valueOf(propJo.get("name").isString().stringValue());
            prop.setName(symbol);
            prop.setAmount(BigDecimal.valueOf(propJo.get("amount").isNumber().doubleValue()));
            propertyMap.put(symbol, prop);
        }
        return ar;
    }
    
    protected LimitOrderResponse limitOrderFromJson(JSONObject jo) {
        LimitOrderResponse order = new LimitOrderResponse();
        order.setPrice(jo.get("price").isNumber().doubleValue());
        order.setAmount(jo.get("amount").isNumber().doubleValue());
        order.setInstrument(Instrument.valueOf(jo.get("instrument").isString().stringValue()));
        order.setId((long)jo.get("id").isNumber().doubleValue());
        order.setSide(OrderSide.valueOf(jo.get("side").isString().stringValue()));
        parseTrades(order, jo);
        return order;
    }
    
    protected MarketOrderResponse marketOrderFromJson(String json) {
        MarketOrderResponse order = new MarketOrderResponse();
        JSONObject jo = JSONParser.parseStrict(json).isObject();
        order.setAmount(jo.get("amount").isNumber().doubleValue());
        order.setInstrument(Instrument.valueOf(jo.get("instrument").isString().stringValue()));
        order.setId((long)jo.get("id").isNumber().doubleValue());
        order.setSide(OrderSide.valueOf(jo.get("side").isString().stringValue()));
        parseTrades(order, jo);
        return order;
    }
    
    protected OrderBook orderBookFromJson(String json) {
        OrderBook book = new OrderBook();
        JSONObject jo = JSONParser.parseStrict(json).isObject();
        JSONObject buy = jo.get("buyLevels").isObject();
        for(String key : buy.keySet()) {
            book.getBuyLevels().put(Double.valueOf(key), buy.get(key).isNumber().doubleValue());
        }
        JSONObject sell = jo.get("sellLevels").isObject();
        for(String key : sell.keySet()) {
            book.getSellLevels().put(Double.valueOf(key), sell.get(key).isNumber().doubleValue());
        }
        return book;
    }
    
    protected void parseTrades(MarketOrderResponse order, JSONObject jo) {
        JSONArray trades = jo.get("trades").isArray();
        for(int i = 0; i < trades.size(); i++) {
            JSONObject to = trades.get(i).isObject();
            long tid = (long) to.get("tid").isNumber().doubleValue();
            double amount = to.get("amount").isNumber().doubleValue();
            double price = to.get("price").isNumber().doubleValue();
            Trade trade = new Trade(tid, amount, price);
            order.addTrade(trade);
        }
    }
    
    protected List<Round> parseRound(JSONArray array) {
        List<Round> result = new ArrayList<>();
        for(int i = 0; i < array.size(); i++) {
            Round round = new Round();
            JSONObject jo = array.get(i).isObject();
            round.setRoundId((long)jo.get("roundId").isNumber().doubleValue());
            round.setStartTime((long)jo.get("startTime").isNumber().doubleValue());
            round.setDuration((long)jo.get("duration").isNumber().doubleValue());
            result.add(round);
        }
        return result;
    }

}
