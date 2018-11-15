package bymihaj;

public class RoundRegisterRequest {

    private long roundId;
    private boolean join;
    
    public long getRoundId() {
        return roundId;
    }
    
    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }
    
    public boolean isJoin() {
        return join;
    }
    
    public void setJoin(boolean join) {
        this.join = join;
    }
    
}
