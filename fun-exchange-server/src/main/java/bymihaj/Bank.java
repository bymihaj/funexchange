package bymihaj;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Bank {

    public final static BigDecimal DEF_AMOUNT = new BigDecimal(1000);
    
    protected Set<Property> assets;

    public Bank() {
        assets = ConcurrentHashMap.newKeySet();
        
        Property mon = new Property();
        mon.setName(Symbol.MON);
        mon.setAmount(DEF_AMOUNT);
        assets.add(mon);
        
        Property stk = new Property();
        stk.setName(Symbol.STK);
        stk.setAmount(DEF_AMOUNT);
        assets.add(stk);
    }

    public List<Property> getProperties() {
        return new ArrayList<>(assets);
    }
}
