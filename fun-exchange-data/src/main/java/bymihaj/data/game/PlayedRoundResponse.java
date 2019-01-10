package bymihaj.data.game;

import java.util.ArrayList;
import java.util.List;

import bymihaj.Round;

public class PlayedRoundResponse {
    
    private List<Round> roundList;
    
    public PlayedRoundResponse() {
        roundList = new ArrayList<>();
    }

    public List<Round> getRoundList() {
        return roundList;
    }

    public void setRoundList(List<Round> roundList) {
        this.roundList = roundList;
    }
    

}
