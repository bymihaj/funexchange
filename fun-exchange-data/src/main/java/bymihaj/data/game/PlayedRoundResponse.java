package bymihaj.data.game;

import java.util.ArrayList;
import java.util.List;

public class PlayedRoundResponse {
    
    private List<Long> roundList;
    
    public PlayedRoundResponse() {
        roundList = new ArrayList<>();
    }

    public List<Long> getRoundList() {
        return roundList;
    }

    public void setRoundList(List<Long> roundList) {
        this.roundList = roundList;
    }
    

}
