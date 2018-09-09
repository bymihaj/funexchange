package bymihaj;

import java.math.BigDecimal;

public class Property {

    private Symbol name;
    private BigDecimal amount;
    
    public Symbol getName() {
        return name;
    }
    public void setName(Symbol name) {
        this.name = name;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
}
