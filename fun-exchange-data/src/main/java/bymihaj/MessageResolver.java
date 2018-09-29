package bymihaj;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import bymihaj.data.order.LimitOrderRequest;
import bymihaj.data.order.LimitOrderResponse;
import bymihaj.data.order.MarketOrderRequest;
import bymihaj.data.order.MarketOrderResponse;
import bymihaj.data.order.OrderStatusRequest;
import bymihaj.data.order.OrderStatusResponse;
import bymihaj.data.order.RejectOrderResponse;

public class MessageResolver {
    
    protected Gson gson;
    protected Map<String, Class<?>> supportedMessages;
    
    public MessageResolver() {
        gson = new Gson();
        supportedMessages = new HashMap<>();
        register(AccountRequest.class);
        register(AccountResponse.class);
        register(LoginRequest.class);
        register(LoginResponse.class);
        register(AssetsRequest.class);
        register(AssetsResponse.class);
        register(MarketOrderRequest.class);
        register(MarketOrderResponse.class);
        register(RejectOrderResponse.class);
        register(LimitOrderRequest.class);
        register(LimitOrderResponse.class);
        register(OrderStatusRequest.class);
        register(OrderStatusResponse.class);
    }
    
    protected void register(Class<?> clazz) {
        supportedMessages.put(clazz.getSimpleName(), clazz);
    }
    
    public <T> T resolve(String json) {
        MessageHolder holder = gson.fromJson(json, MessageHolder.class);
        String name = holder.name;
        if( supportedMessages.containsKey(name) ) {
            return gson.<T>fromJson(holder.object, supportedMessages.get(name));
        } else {
            throw new IllegalArgumentException("Unregsitered class: "+name);
        }
    }
    
    public String pack(Object object) {
        MessageHolder holder = new MessageHolder();
        holder.name = object.getClass().getSimpleName();
        holder.object = gson.toJson(object);
        return gson.toJson(holder);
    }

}
