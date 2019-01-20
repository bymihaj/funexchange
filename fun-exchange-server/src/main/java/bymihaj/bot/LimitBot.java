package bymihaj.bot;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bymihaj.Instrument;
import bymihaj.OrderBook;
import bymihaj.OrderBookRequest;
import bymihaj.Symbol;
import bymihaj.data.order.LimitOrderRequest;
import bymihaj.data.order.OrderSide;

public class LimitBot extends AbstractBot {

    static final Logger log = LoggerFactory.getLogger(LimitBot.class);
    
    protected OrderBook book;
    
    public LimitBot(URI serverUri) {
        super(serverUri);
        connection.subscribe(OrderBook.class, this::onOrderBook);
    }

    @Override
    public void step() {
        log.info("Try to do step");
        
        if(lastAssets == null) {
            log.info("{} has null assets", userName);
            return;
        }
        
        if(book == null) {
            connection.send(new OrderBookRequest());
            log.info("{} has null order book", userName);
            return;
        }
        
        double amount = 0.0;
        if (isBuyer) {
            double mon = lastAssets.getProperties().get(Symbol.MON).getAmount().doubleValue();
            amount = mon;
        } else {
            double stk = lastAssets.getProperties().get(Symbol.STK).getAmount().doubleValue();
            amount = stk;
        }
        
        if(amount < 10.0) {
            log.info("{} has no money MON:{} STK:{}", userName, 
                    lastAssets.getProperties().get(Symbol.MON).getAmount().toPlainString(), 
                    lastAssets.getProperties().get(Symbol.STK).getAmount().toPlainString());
            return;
        }
        
        if (random.nextDouble() > 0.1) {
            amount = amount / 1000.0;
        } else {
            amount = amount / 100.0;
        }
        
        
        amount = Integer.valueOf((int) (amount * PIP)) / PIP;
        
        OrderSide side = isBuyer ? OrderSide.BUY : OrderSide.SELL;
        
        double basePrice = 0.0;
        if(isBuyer) {
            if(book.getBuyLevels().isEmpty()) {
                basePrice = 0.01;
            } else {
                List<Double> list = new ArrayList<>(book.getBuyLevels().keySet());
                Collections.sort(list);
                Collections.reverse(list);
                basePrice = list.get(0);
            }
        } else {
            if(book.getSellLevels().isEmpty()) {
                basePrice = 9999.99;
            } else {
                List<Double> list = new ArrayList<>(book.getSellLevels().keySet());
                Collections.sort(list);
                basePrice = list.get(0);
            }
        }
        
        double mod = random.nextInt(10)/100.0d;
        if(random.nextBoolean()) {
            basePrice = basePrice + mod;
        } else {
            basePrice = basePrice - mod;
        }
        
        LimitOrderRequest limit = new LimitOrderRequest();
        limit.setPrice(basePrice);
        limit.setAmount(amount);
        limit.setSide(side);
        limit.setInstrument(Instrument.STKMON);
        connection.send(limit);
    }
    
    public void onOrderBook(OrderBook book) {
        this.book = book;
    }
    
}
