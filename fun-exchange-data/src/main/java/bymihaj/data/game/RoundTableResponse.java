package bymihaj.data.game;

import java.util.ArrayList;
import java.util.List;

public class RoundTableResponse {

    protected long roundId;
    protected List<PlayedRecord> green;
    protected List<PlayedRecord> red;
    
    public RoundTableResponse() {
        green = new ArrayList<>();
        red = new ArrayList<>();
    }
    
    public long getRoundId() {
        return roundId;
    }
    
    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }
    
    public List<PlayedRecord> getGreen() {
        return green;
    }
    
    public void setGreen(List<PlayedRecord> green) {
        this.green = green;
    }
    
    public List<PlayedRecord> getRed() {
        return red;
    }
    
    public void setRed(List<PlayedRecord> red) {
        this.red = red;
    }
    
}
