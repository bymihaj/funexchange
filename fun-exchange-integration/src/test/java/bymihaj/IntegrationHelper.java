package bymihaj;

public class IntegrationHelper {
    
    public static void login(Server server, SocketEmulation client) {
        client.send(new AccountRequest());
        AccountResponse accResp = client.filter(AccountResponse.class).get(0);
        LoginRequest login = new LoginRequest();
        login.setUser(accResp.getUser());
        login.setPass(accResp.getPass());
        client.send(login);
    }

}
