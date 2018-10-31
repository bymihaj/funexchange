package bymihaj.data.order;

public class Trade {
    
    protected long tid;
    protected double amount;
    protected double price;
    
    public Trade(long tid, double amount, double price) {
        setTid(tid);
        setAmount(amount);
        setPrice(price);
    }
        
    public long getTid() {
        return tid;
    }
    
    public void setTid(long tid) {
        this.tid = tid;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
}
