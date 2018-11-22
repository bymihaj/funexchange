package bymihaj;

public class RoundStatus {
    
    private Round round;
    private boolean isStarted;
    private Team team;
    
    public boolean isStarted() {
        return isStarted;
    }
    
    public void setStarted(boolean isStarted) {
        this.isStarted = isStarted;
    }

    public Round getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

}
