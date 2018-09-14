package bymihaj.data.order;

public class RejectOrderResponse {

    protected String reason;
    
    public RejectOrderResponse(String reason) {
        this.reason = reason;
    }
    
    public String getReason() {
        return reason;
    }
}
