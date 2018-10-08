package bymihaj.data.order;

public class RejectOrderResponse {

    protected String reason;
    private RejectOrderType rejectType;
    
    public RejectOrderResponse(String reason) {
        this.reason = reason;
    }
    
    public String getReason() {
        return reason;
    }

    public RejectOrderType getRejectType() {
        return rejectType;
    }

    public void setRejectType(RejectOrderType rejectType) {
        this.rejectType = rejectType;
    }
}
