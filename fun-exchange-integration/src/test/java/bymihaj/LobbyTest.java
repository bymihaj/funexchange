package bymihaj;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LobbyTest {
    
    protected Server server;
    protected SocketEmulation client;
    
    @Before
    public void before() {
        server = new Server(new InetSocketAddress(0));
        client = new SocketEmulation(server);
        IntegrationHelper.login(server, client);
    }
    
    @Test(timeout=3000)
    public void availableRoundTest() {
        long tick = System.currentTimeMillis();
        
        client.send(new LobbyRequest());
        
        LobbyResponse lobby = client.last(LobbyResponse.class);
        List<Round> available = lobby.getAvailable();
        
        long nextHour = TimeUnit.MINUTES.convert(tick, TimeUnit.MILLISECONDS) + 1;
        Assert.assertEquals(server.getRoundConfig().getAvailableCount(), available.size());
        for(int i = 0; i < available.size(); i++) {
            Round round = available.get(i);
            Assert.assertEquals(i, round.getRoundId());
            Assert.assertEquals(server.getRoundConfig().getDuration(), round.getDuration());
            Assert.assertEquals(TimeUnit.MINUTES.toMillis(nextHour)+i*server.getRoundConfig().getDuration(), round.getStartTime());
        }
    }
    
    @Test
    public void availableRoundReuseTest() {
        client.send(new LobbyRequest());
        client.send(new LobbyRequest());
        
        LobbyResponse lobby = client.last(LobbyResponse.class);
        Assert.assertEquals(server.getRoundConfig().getAvailableCount(), lobby.getAvailable().size());
    }
    
    @Test(timeout=3000)
    public void availableRoundBroadcastTest() {
        client.send(new LobbyRequest());
        
        SocketEmulation second = new SocketEmulation(server);
        IntegrationHelper.login(server, second);
        
        server.getRoundConfig().setAvailableCount(server.getRoundConfig().getAvailableCount()+2);
        second.send(new LobbyRequest());
        
        client.wait(LobbyResponse.class);
        
        LobbyResponse result = client.last(LobbyResponse.class);
        Assert.assertEquals(server.getRoundConfig().getAvailableCount(), result.getAvailable().size());

    }
    
    @Test
    public void registerRoundTest() {
        client.send(new LobbyRequest());
        LobbyResponse lobby = client.last(LobbyResponse.class);
        
        RoundRegisterRequest reg = new RoundRegisterRequest();
        reg.setJoin(true);
        reg.setRoundId(lobby.getAvailable().get(0).getRoundId());
        client.send(reg);
        
        LobbyResponse result = client.last(LobbyResponse.class);
        Assert.assertEquals(1, result.getPending().size());
        Assert.assertEquals(reg.getRoundId(), result.getPending().get(0).getRoundId());
    }
    
        
    @Test
    public void unregisterRoundTest() {
        client.send(new LobbyRequest());
        LobbyResponse lobby = client.last(LobbyResponse.class);
        
        RoundRegisterRequest reg = new RoundRegisterRequest();
        reg.setJoin(true);
        reg.setRoundId(lobby.getAvailable().get(0).getRoundId());
        client.send(reg);
        
        RoundRegisterRequest unreg = new RoundRegisterRequest();
        unreg.setJoin(false);
        unreg.setRoundId(lobby.getAvailable().get(0).getRoundId());
        client.send(unreg);
        
        LobbyResponse result = client.last(LobbyResponse.class);
        Assert.assertTrue(result.getPending().isEmpty());
    }
    @Test
    public void currentRoundEmptyTest() {
        client.send(new LobbyRequest());
        
        LobbyResponse result = client.last(LobbyResponse.class);
        Assert.assertTrue(result.getCurrent().isEmpty());
    }

}
