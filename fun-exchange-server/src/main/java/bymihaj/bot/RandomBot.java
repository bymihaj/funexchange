package bymihaj.bot;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bymihaj.AccountRequest;
import bymihaj.AccountResponse;
import bymihaj.AssetsRequest;
import bymihaj.AssetsResponse;
import bymihaj.Instrument;
import bymihaj.LobbyResponse;
import bymihaj.LoginRequest;
import bymihaj.LoginResponse;
import bymihaj.Round;
import bymihaj.RoundRegisterRequest;
import bymihaj.RoundStatus;
import bymihaj.Symbol;
import bymihaj.Team;
import bymihaj.data.order.LimitOrderRequest;
import bymihaj.data.order.MarketOrderRequest;
import bymihaj.data.order.OrderSide;

public class RandomBot extends AbstractBot {

    

    static Logger log = LoggerFactory.getLogger(RandomBot.class);
    
    public static final int RANGE = 10000;
    

    
    public RandomBot(URI serverUri) {
        super(serverUri);
    }
    
    /*
    public void onRoundStatus(RoundStatus status) {
        isBuyer = Team.GREEN.equals(status.getTeam());
        log.info("I am {} team pleayer", status.getTeam());
    }*/
    
    public void step() {
        log.info("Trade some");
        if (lastAssets != null) {

            
            boolean isMarket = random.nextDouble() < 0.3;

            double amount = 0.0;
            if (isBuyer) {
                double mon = lastAssets.getProperties().get(Symbol.MON).getAmount().doubleValue();
                amount = mon;
            } else {
                double stk = lastAssets.getProperties().get(Symbol.STK).getAmount().doubleValue();
                amount = stk;
            }
            
            if(amount < 10.0) {
                log.info("No money");
                return;
            }
            
            if (random.nextDouble() > 0.1) {
                amount = amount / 1000.0;
            } else {
                amount = amount / 100.0;
            }
            
            
            amount = Integer.valueOf((int) (amount * PIP)) / PIP;
            OrderSide side = isBuyer ? OrderSide.BUY : OrderSide.SELL;
            if (isMarket) {
                MarketOrderRequest market = new MarketOrderRequest();
                market.setAmount(amount);
                market.setSide(side);
                market.setInstrument(Instrument.STKMON);
                connection.send(market);
            } else {
                double price = random.nextInt(RANGE) / PIP;
                price = Integer.valueOf((int) (price * PIP)) / PIP;
                if (price < 0.0001) {
                    price = 0.0001;
                }
                LimitOrderRequest limit = new LimitOrderRequest();
                limit.setPrice(price);
                limit.setAmount(amount);
                limit.setSide(side);
                limit.setInstrument(Instrument.STKMON);
                connection.send(limit);
            }
        }
                
        connection.send(new AssetsRequest());
    }

}
