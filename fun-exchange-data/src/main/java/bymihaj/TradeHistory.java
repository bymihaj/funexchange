package bymihaj;

import bymihaj.data.order.OrderSide;

public class TradeHistory {
    
    protected String dateTime;
    protected double amount;
    protected double price;
    private OrderSide side;
    
    public String getDateTime() {
        return dateTime;
    }
    public void setDateTime(String dateTime) {
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
