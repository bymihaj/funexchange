package bymihaj;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Bank {

    public final static BigDecimal DEF_AMOUNT = new BigDecimal(1000);
    
    protected Map<Symbol,Property> assets;

    public Bank() {
        assets = new ConcurrentHashMap<>();
        
        Property mon = new Property();
        mon.setName(Symbol.MON);
        mon.setAmount(DEF_AMOUNT);
        assets.put(Symbol.MON, mon);
        
        Property stk = new Property();
        stk.setName(Symbol.STK);
        stk.setAmount(DEF_AMOUNT);
        assets.put(Symbol.STK, stk);
    }

    public Map<Symbol,Property> getProperties() {
        return assets;
    }
}
