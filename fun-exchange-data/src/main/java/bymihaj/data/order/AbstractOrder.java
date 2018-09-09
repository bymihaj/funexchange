package bymihaj.data.order;

abstract public class AbstractOrder {
    
    protected long id;
    private OrderSide side;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public OrderSide getSide() {
        return side;
    }

    public void setSide(OrderSide side) {
        this.side = side;
    }

}
