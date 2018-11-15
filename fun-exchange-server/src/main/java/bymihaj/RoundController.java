package bymihaj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoundController {
    
    static Logger log = LoggerFactory.getLogger(RoundController.class);
    
    protected AtomicLong idCounter;
    protected RoundConfig config;
    protected Map<Long, Round> roundMap;
    protected Map<User, List<Round>> userMap;
    protected LoginController loginController;
    protected long currentCount;
    protected long availableCount;
    
    public RoundController(RoundConfig config, LoginController loginController) {
        this.config = config;
        this.loginController = loginController;
        idCounter = new AtomicLong(0);
        roundMap = new ConcurrentHashMap<>();
        userMap = new ConcurrentHashMap<>();
        
        // TODO check every second that start some round and broadcast 
        // generate new rounds
        TimerTask task = new TimerTask() {
            
            @Override
            public void run() {
                boolean needBroadcast = false;
                
                // TODO REF
                if(getAvailable().size() < config.getAvailableCount()) {
                    long tick = System.currentTimeMillis();
                    long nextMinute = TimeUnit.MINUTES.convert(tick, TimeUnit.MILLISECONDS) + 1;
                    List<Round> nextRound = roundMap.values().stream().filter(r -> r.getStartTime() > tick).collect(Collectors.toList());
                    for(int i = 0; i < (config.getAvailableCount() - nextRound.size()); i++) {
                        Round round = new Round();
                        round.setRoundId(idCounter.getAndIncrement());
                        round.setDuration(config.getDuration());
                        round.setStartTime(TimeUnit.MINUTES.toMillis(nextMinute)+i*config.getDuration());
                        roundMap.put(round.getRoundId(), round);
                        log.info("Create round {} that will start at {}", round.getRoundId(), new Date(round.getStartTime()));
                    }
                    needBroadcast = true;
                }
                
                if(currentCount != getCurrent().size()) {
                    log.info("Current round number changed from {} to {}", currentCount, getCurrent().size());
                    needBroadcast = true;
                }
                currentCount = getCurrent().size();
                
                if(availableCount != getAvailable().size()) {
                    log.info("Available round number changed from {} to {}", availableCount, getAvailable().size());
                    needBroadcast = true;
                }
                availableCount = getAvailable().size();
                
                if(needBroadcast) {
                    loginController.getAllLoginedUser().forEach( u -> sendLobby(u));
                }
            }
        };
        new Timer().schedule(task, 0, 1000);
    }
    
    
    public void onLobby(User user, LobbyRequest request) {
        
        // TODO REF
        long tick = System.currentTimeMillis();
        long nextMinute = TimeUnit.MINUTES.convert(tick, TimeUnit.MILLISECONDS) + 1;
        List<Round> nextRound = roundMap.values().stream().filter(r -> r.getStartTime() > tick).collect(Collectors.toList());
        for(int i = 0; i < (config.getAvailableCount() - nextRound.size()); i++) {
            Round round = new Round();
            round.setRoundId(idCounter.getAndIncrement());
            round.setDuration(config.getDuration());
            round.setStartTime(TimeUnit.MINUTES.toMillis(nextMinute)+i*config.getDuration());
            roundMap.put(round.getRoundId(), round);
            log.info("Create round {} that will start at {}", round.getRoundId(), new Date(round.getStartTime()));
        }
        
        
        sendLobby(user);
        
    }
    
    public void onRegister(User user, RoundRegisterRequest register) {
        
        if(!roundMap.containsKey(register.getRoundId())){
            log.error("Round {} not found");
            return;
        }
        
        if(!userMap.containsKey(user)) {
            userMap.put(user, Collections.synchronizedList(new ArrayList<>()));
        }
        
        Round round = roundMap.get(register.getRoundId());
        if(register.isJoin()) {
            userMap.get(user).add(round);
            sendLobby(user);
        } else {
            if(userMap.get(user).contains(round)) {
                userMap.get(user).remove(round);
                sendLobby(user);
            } else {
                log.error("Unregistration failed for Round {}", register.getRoundId());
            }
        }
    }
    
    protected void sendLobby(User user) {
        LobbyResponse lobby = new LobbyResponse();
        List<Round> allAvailable = getAvailable();
        lobby.setAvailable(allAvailable.subList(0, Math.min(config.getAvailableCount(), allAvailable.size())));
        lobby.setPending(userMap.get(user));
        lobby.setCurrent(getCurrent().stream().filter( r -> userMap.get(user).contains(r)).collect(Collectors.toList()));
        user.send(lobby);
    }
    
    protected List<Round> getAvailable() {
        long tick = System.currentTimeMillis();
        return roundMap.values().stream().filter(r -> r.getStartTime() > tick).collect(Collectors.toList());
    }
    
    protected List<Round> getCurrent() {
        long tick = System.currentTimeMillis();
        return roundMap.values().stream()
                .filter(r -> r.getStartTime() <= tick && r.getStartTime() + r.getDuration() > tick)
                .collect(Collectors.toList());
    }

}
