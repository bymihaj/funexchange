package bymihaj;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bymihaj.data.game.PlayedRecord;
import bymihaj.data.game.PlayedRoundRequest;
import bymihaj.data.game.PlayedRoundResponse;
import bymihaj.data.game.RoundTableRequest;
import bymihaj.data.game.RoundTableResponse;

public class RoundController {
    
    static Logger log = LoggerFactory.getLogger(RoundController.class);
    
    protected AtomicLong idCounter;
    protected RoundConfig config;
    protected Map<Long, Round> roundMap;
    protected Map<User, List<Round>> userMap;
    protected LoginController loginController;
    protected TradeController tradeController;
    protected long currentCount;
    protected long availableCount;
    protected Round currentRound;
    protected Random random = new Random();
    protected Set<User> greenTeam;
    protected Set<User> redTeam;
    protected Map<Long, RoundTableResponse> resultMap;
    
    public RoundController(RoundConfig config, LoginController loginController, TradeController tradeController) {
        this.config = config;
        this.loginController = loginController;
        this.tradeController = tradeController;
        idCounter = new AtomicLong(0);
        roundMap = new ConcurrentHashMap<>();
        userMap = new ConcurrentHashMap<>();
        greenTeam = new HashSet<>();
        redTeam = new HashSet<>();
        resultMap = new ConcurrentHashMap<Long, RoundTableResponse>();
        
        // TODO check every second that start some round and broadcast 
        // generate new rounds
        TimerTask task = new TimerTask() {
            
            @Override
            public void run() {
                boolean needBroadcast = false;
                
                if(getAvailable().size() < config.getAvailableCount()) {
                    long tick = System.currentTimeMillis();
                    long nextMinute;
                    if(getAvailable().isEmpty()) {
                        nextMinute = TimeUnit.MINUTES.convert(tick, TimeUnit.MILLISECONDS) + 1;
                    } else {
                        Round last = getAvailable().get(getAvailable().size()-1);
                        nextMinute = TimeUnit.MINUTES.convert(last.getStartTime()+last.getDuration(), TimeUnit.MILLISECONDS);
                    }
                    
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
                
                // change round code
                if(getCurrent().isEmpty()) {
                    return;
                }
                
                if(currentRound != getCurrent().get(0)) {
                    
                    tradeController.stopTrading();
                    
                    if(currentRound != null) {
                        log.info("Round {} finished", currentRound.getRoundId());
                        RoundStatus oldStatus = new RoundStatus();
                        oldStatus.setStarted(false);
                        oldStatus.setRound(currentRound);
                        oldStatus.setTeam(Team.SPECTATOR);
                        for(User user : loginController.getAllLoginedUser()) {
                            user.send(oldStatus);
                        }
                        
                        generateRoundResult();
                        
                    }
                    tradeController.reset();
                    loginController.resetPendingOrder();
                    
                    Round nextRound = getCurrent().get(0);
                    log.info("Round {} started", nextRound.getRoundId());
                    for(User user : userMap.keySet()) {
                        if(userMap.get(user).contains(nextRound)) {
                            if(random.nextBoolean()) {
                                greenTeam.add(user);
                                user.getBank().getProperties().get(Symbol.MON).setAmount(Bank.DEF_AMOUNT);
                                user.getBank().getProperties().get(Symbol.STK).setAmount(BigDecimal.ZERO);
                                log.info("User {} goto green team", user.getUserName());
                            } else {
                                redTeam.add(user);
                                user.getBank().getProperties().get(Symbol.STK).setAmount(Bank.DEF_AMOUNT);
                                user.getBank().getProperties().get(Symbol.MON).setAmount(BigDecimal.ZERO);
                                log.info("User {} goto red team", user.getUserName());
                            }
                        } else {
                            user.getBank().getProperties().get(Symbol.MON).setAmount(BigDecimal.ZERO);
                            user.getBank().getProperties().get(Symbol.STK).setAmount(BigDecimal.ZERO);
                        }
                    }
                    
                    for(User user : loginController.getAllLoginedUser()) {
                        RoundStatus nextStatus = new RoundStatus();
                        nextStatus.setStarted(true);
                        nextStatus.setRound(nextRound);
                        if(greenTeam.contains(user)){
                            nextStatus.setTeam(Team.GREEN);
                        } else if(redTeam.contains(user)) {
                            nextStatus.setTeam(Team.RED);
                        } else {
                            nextStatus.setTeam(Team.SPECTATOR);
                        }
                        user.send(nextStatus);
                    }
                    
                    tradeController.startTrading();
                }
                
                currentRound = getCurrent().get(0);
            }
        };
        new Timer().schedule(task, 0, 1000);
    }
    
    
    public void onLobby(User user, LobbyRequest request) {
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
            if(!userMap.get(user).contains(round)) {
                userMap.get(user).add(round);
            } else {
                log.error("Registered to round {} previously", register.getRoundId());
            }
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
        long tick = System.currentTimeMillis();
        LobbyResponse lobby = new LobbyResponse();
        List<Round> allAvailable = getAvailable();
        lobby.setAvailable(allAvailable.subList(0, Math.min(config.getAvailableCount(), allAvailable.size())));
        if(userMap.containsKey(user)) {
            lobby.setPending(userMap.get(user).stream().filter( r -> r.getStartTime()  > tick).collect(Collectors.toList()));
            lobby.setCurrent(getCurrent().stream().filter( r -> userMap.get(user).contains(r)).collect(Collectors.toList()));
        } else {
            lobby.setPending(Collections.emptyList());
            lobby.setCurrent(Collections.emptyList());
        }
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
    
    public void onRoundStatusRequest(User user, RoundStatusRequest roundRequest) {
        if(getCurrent().isEmpty()) {
            return;
        }
        
        RoundStatus resp = new RoundStatus();
        resp.setStarted(true);
        resp.setRound(getCurrent().get(0));
        if(greenTeam.contains(user)){
            resp.setTeam(Team.GREEN);
        } else if(redTeam.contains(user)) {
            resp.setTeam(Team.RED);
        } else {
            resp.setTeam(Team.SPECTATOR);
        }
        user.send(resp);
    }
    
    public void onPlayedRound(User user, PlayedRoundRequest req) {
        PlayedRoundResponse resp = new PlayedRoundResponse();
        resp.setRoundList(new ArrayList<>(resultMap.keySet()));
        user.send(resp);
    }
    
    public void onRoundTable(User user, RoundTableRequest req) {
        if(resultMap.containsKey(req.getRoundId())) {
            user.send(resultMap.get(req.getRoundId()));
        } else {
            log.error("No result for round {}", req.getRoundId());
        }
    }
    
    protected void generateRoundResult() {
        RoundTableResponse table = new RoundTableResponse();
        table.setRoundId(currentRound.getRoundId());
        for(User user : greenTeam) {
            String mon = user.getBank().getProperties().get(Symbol.MON).getAmount().toPlainString();
            String stk = user.getBank().getProperties().get(Symbol.STK).getAmount().toPlainString();
            log.info("Round {} user {} from green team has  assets MON:{} , STK {}", currentRound.getRoundId(), user.getUserName(), mon, stk);
            PlayedRecord record = new PlayedRecord();
            record.setUser(user.getUserName());
            record.setAmount(user.getBank().getProperties().get(Symbol.STK).getAmount().doubleValue());
            table.getGreen().add(record);
        }
        for(User user : redTeam) {
            String mon = user.getBank().getProperties().get(Symbol.MON).getAmount().toPlainString();
            String stk = user.getBank().getProperties().get(Symbol.STK).getAmount().toPlainString();
            log.info("Round {} user {} from red team has  assets MON:{} , STK {}", currentRound.getRoundId(), user.getUserName(), mon, stk);
            PlayedRecord record = new PlayedRecord();
            record.setUser(user.getUserName());
            record.setAmount(user.getBank().getProperties().get(Symbol.MON).getAmount().doubleValue());
            table.getRed().add(record);
        }
        
        
        
        Collections.sort(table.getGreen(), new RecordComparator());
        Collections.sort(table.getRed(), new RecordComparator());
        
        for(int i = 0; i < table.getGreen().size(); i++) {
            table.getGreen().get(i).setPosition(i+1);
        }
        
        for(int i = 0; i < table.getRed().size(); i++) {
            table.getRed().get(i).setPosition(i+1);
        }
        
        
        greenTeam.clear();
        redTeam.clear();
        
        resultMap.put(table.getRoundId(), table);
    }
    
    static class RecordComparator implements Comparator<PlayedRecord> {
        @Override
        public int compare(PlayedRecord o1, PlayedRecord o2) {
            return Double.compare(o2.getAmount(), o1.getAmount());
        }
    }
    

}
