package bymihaj;

import bymihaj.data.order.OrderSide;

public class TradeHistory {
    
    protected long dateTime;
    protected double amount;
    protected double price;
    private OrderSide side;
    
    public long getDateTime() {
        return dateTime;
    }
    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
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
    public OrderSide getSide() {
        return side;
    }
    public void setSide(OrderSide side) {
        this.side = side;
    }
}
