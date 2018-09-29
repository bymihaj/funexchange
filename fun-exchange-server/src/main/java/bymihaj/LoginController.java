package bymihaj;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;

import bymihaj.LoginResponse.Status;

// TODO totally rework
public class LoginController {
    
    protected Random random;
    protected MessageResolver resolver;
    protected Map<WebSocket, User> loginedUser;
    protected Map<String, AccountResponse> allowedAccount;
    protected Map<String, Bank> bankStorage;
    protected Map<String, User> userStorage;
    
    public LoginController() {
        resolver = new MessageResolver();
        loginedUser = new ConcurrentHashMap<>();
        allowedAccount = new ConcurrentHashMap<>();
        bankStorage = new ConcurrentHashMap<>();
        userStorage = new ConcurrentHashMap<>();
        random = new Random();
    }
    
    public User getUser(WebSocket webSocket) {
        if (loginedUser.containsKey(webSocket)) {
            return loginedUser.get(webSocket);
        } else {
            return new User(webSocket, resolver);
        }
    }
    
    public void onAccountRequest(User user, AccountRequest accountReques) {
        AccountResponse response = generateAccount();
        allowedAccount.put(response.getUser(), response);
        userStorage.put(response.getUser(),  new User(null, resolver));
        user.send(response);
    }
    
    public void onLoginRequest(User user, LoginRequest loginRequest) {
        AccountResponse allowed = allowedAccount.get(loginRequest.getUser());
        if ( allowed != null && allowed.getPass().equals(loginRequest.getPass())) {
            User realUser = userStorage.get(allowed.getUser());
        	loginedUser.put(user.getGuestSocket(), realUser);
        	realUser.setLogined(true);
        	realUser.addSession(user.getGuestSocket());
        	realUser.setBank(getBank(loginRequest.getUser()));
            LoginResponse resp = new LoginResponse();
            resp.setStatus(Status.OK);
            realUser.send(resp);
        } else {
            LoginResponse resp = new LoginResponse();
            resp.setStatus(Status.FAILED);
            user.send(resp);
        }
    }
    
    public void onAssetsRequest(User user, AssetsRequest assetsRequest) {
        AssetsResponse response = new AssetsResponse(user.getBank().getProperties());
        user.send(response);
    }
    
    public AccountResponse generateAccount() {
        String user = "";
        String pass = "";
        for(int i = 0; i<3; i++) {
            char u = (char) (random.nextInt(26) + 'A');
            user = user + u;
            char p = (char) (random.nextInt(26) + 'a');
            pass = pass + p;
        }
        
        AccountResponse response = new AccountResponse();
        response.setUser(user+pass);
        response.setPass(pass);
        return response;
    }
    
    // TODO move into user
    public Bank getBank(String user) {
        if (bankStorage.containsKey(user)) {
            return bankStorage.get(user);
        } else {
            Bank bank = new Bank();
            bankStorage.put(user, bank);
            return bank;
        }
    }
    
}
