package bymihaj.bot;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bymihaj.AccountRequest;
import bymihaj.AccountResponse;
import bymihaj.AssetsRequest;
import bymihaj.AssetsResponse;
import bymihaj.LobbyResponse;
import bymihaj.LoginRequest;
import bymihaj.Round;
import bymihaj.RoundRegisterRequest;
import bymihaj.RoundStatus;
import bymihaj.Team;

abstract public class AbstractBot {
    
    static final Logger log = LoggerFactory.getLogger(AbstractBot.class);
    
    public static final int PERIOD = 10 * 1000;
    public static final double PIP = 1e3;

    protected BotConnection connection;
    protected AssetsResponse lastAssets;
    protected List<Long> registredRounds;
    protected boolean roundStarted = false;
    protected Random random = new Random();
    protected boolean isBuyer;
    
    
    public AbstractBot(URI serverUri) {
        registredRounds = new ArrayList<>();
        connection = new BotConnection(serverUri);
        connection.setOpenHandler(this::whenConnected);
        connection.subscribe(AccountResponse.class, this::onAccountResponse);
        connection.subscribe(AssetsResponse.class, this::onAssetsResponse);
        connection.subscribe(LobbyResponse.class, this::onLobbyResponse);
        connection.subscribe(RoundStatus.class, this::onRoundStatus);
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
    
    public void onLobbyResponse(LobbyResponse lobby) {
        for(Round round : lobby.getAvailable()) {
            if(!registredRounds.contains(round.getRoundId())) {
                registredRounds.add(round.getRoundId());
                RoundRegisterRequest register = new RoundRegisterRequest();
                register.setJoin(true);
                register.setRoundId(round.getRoundId());
                connection.send(register);
            }
        }
        
        
        if(!lobby.getCurrent().isEmpty() && !roundStarted) {
            roundStarted = true;
            startTrading();
            log.info("Start trading");
        }
        
        
    }
    
    public void onRoundStatus(RoundStatus status) {
        isBuyer = Team.GREEN.equals(status.getTeam());
        log.info("I am {} team pleayer", status.getTeam());
    }
    
    protected void startTrading() {
        connection.send(new AssetsRequest());
        Runnable task = new Runnable() {

            @Override
            public void run() {
                for (;;) {
                    step();
                    try {
                        Thread.sleep(random.nextInt(PERIOD));
                    } catch (InterruptedException e) {
                       log.error("InterruptedException", e);
                    }
                }
            }
        };

        new Thread(task).start();
    }


    abstract public void step();
}
