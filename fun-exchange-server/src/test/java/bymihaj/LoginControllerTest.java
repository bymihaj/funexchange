package bymihaj;

import org.junit.Assert;
import org.junit.Test;

public class LoginControllerTest {
    
    @Test
    public void generateAccountTest() {
        LoginController lc = new LoginController();
        AccountResponse resp1 = lc.generateAccount();
        AccountResponse resp2 = lc.generateAccount();
        
        Assert.assertNotEquals(resp1.user, resp2.user);
    }
    

}
