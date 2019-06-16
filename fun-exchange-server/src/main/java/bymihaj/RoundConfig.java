package bymihaj;

import java.util.concurrent.TimeUnit;

public class RoundConfig {

    private int availableCount;
    private long duration;
    
    public RoundConfig() {
        availableCount = 4;
        duration = TimeUnit.MINUTES.toMillis(5);
    }
    
    public int getAvailableCount() {
        return availableCount;
    }
    
    public void setAvailableCount(int  availableCount) {
        this.availableCount = availableCount;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public void setDuration(long duration) {
        this.duration = duration;
    }
    
}
