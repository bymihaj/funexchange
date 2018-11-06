package bymihaj.bot;

import java.math.BigDecimal;
import java.net.URI;
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
import bymihaj.LoginRequest;
import bymihaj.LoginResponse;
import bymihaj.Symbol;
import bymihaj.data.order.LimitOrderRequest;
import bymihaj.data.order.MarketOrderRequest;
import bymihaj.data.order.OrderSide;

// TODO correct stoping
public class RandomBot {

    static Logger log = LoggerFactory.getLogger(RandomBot.class);

    public static final int PERIOD = 10 * 1000;
    // public static final double BASE = 1.0;
    public static final int RANGE = 10000;
    public static final double PIP = 1e3;

    protected Random random;
    protected BotConnection connection;
    protected AssetsResponse lastAssets;

    public RandomBot(URI serverUri) {
        random = new Random();
        connection = new BotConnection(serverUri);
        connection.setOpenHandler(this::whenConnected);
        connection.subscribe(AccountResponse.class, this::onAccountResponse);
        connection.subscribe(LoginResponse.class, this::onLoginResponse);
        connection.subscribe(AssetsResponse.class, this::onAssetsResponse);
        connection.connect();
    }

    public void whenConnected() {
        connection.send(new AccountRequest());
    }

    public void onAccountResponse(AccountResponse resp) {
        LoginRequest login = new LoginRequest();
        login.setUser(resp.getUser());
        login.setPass(resp.getPass());
        connection.send(login);
    }

    public void onAssetsResponse(AssetsResponse assets) {
        lastAssets = assets;
    }

    public void onLoginResponse(LoginResponse resp) {
        connection.send(new AssetsRequest());
        Runnable task = new Runnable() {

            @Override
            public void run() {
                for (;;) {
                    log.info("Trade some");
                    if (lastAssets != null) {

                        boolean isBuy = random.nextBoolean();
                        boolean isMarket = random.nextDouble() < 0.3;

                        double amount = 0.0;
                        if (isBuy) {
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
                        OrderSide side = isBuy ? OrderSide.BUY : OrderSide.SELL;
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
                    try {
                        Thread.sleep(random.nextInt(PERIOD));
                    } catch (InterruptedException e) {
                       log.error("InterruptedException", e);
                    }
                }
            }
        };

        new Thread(task).start();
        // Timer timer = new Timer();
        // timer.scheduleAtFixedRate(task, 0, PERIOD);

    }

}
