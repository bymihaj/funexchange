package bymihaj;

import java.math.BigDecimal;

public enum Symbol {

    MON(new BigDecimal("0.01")),
    STK(new BigDecimal("0.1"));
    
    protected BigDecimal coin;
    
    private Symbol(BigDecimal coin){
        this.coin = coin;
    }
    
    public BigDecimal getCoin() {
        return coin;
    }
}
