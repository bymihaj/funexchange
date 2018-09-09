package bymihaj;

import java.net.InetSocketAddress;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AccountGenerateTest {

    protected Server server;
    protected SocketEmulation client;
    
    @Before
    public void before() {
        server = new Server(new InetSocketAddress(0));
        client = new SocketEmulation(server);
    }
    
    @Test
    public void accountGenerateTest() {
        AccountRequest request = new AccountRequest();
        client.send(request);
        
        Assert.assertFalse(client.filter(AccountResponse.class).isEmpty());
    }
    
    @Test
    public void loginFailedTest() {
        LoginRequest request = new LoginRequest();
        request.setUser("wrong");
        request.setPass("wrong");
        client.send(request);
        
        List<LoginResponse> list = client.filter(LoginResponse.class);
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(list.get(0).getStatus() == LoginResponse.Status.FAILED);
    }
    
    @Test
    public void loginOkTest() {
        AccountRequest accReq = new AccountRequest();
        client.send(accReq);
        
        AccountResponse accResp = client.filter(AccountResponse.class).get(0);
        LoginRequest login = new LoginRequest();
        login.setUser(accResp.getUser());
        login.setPass(accResp.getPass());
        client.send(login);
        
        List<LoginResponse> list = client.filter(LoginResponse.class);
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(list.get(0).getStatus() == LoginResponse.Status.OK);
    }
    
    @Test
    public void getBankOkTest() {
        AccountRequest accReq = new AccountRequest();
        client.send(accReq);
        
        AccountResponse accResp = client.filter(AccountResponse.class).get(0);
        LoginRequest login = new LoginRequest();
        login.setUser(accResp.getUser());
        login.setPass(accResp.getPass());
        client.send(login);
        
        AssetsRequest assetsRequest = new AssetsRequest();
        client.send(assetsRequest);
        
        AssetsResponse result = client.filter(AssetsResponse.class).get(0);
        Assert.assertEquals(result.getProperties().size(), 2);
    }
}
