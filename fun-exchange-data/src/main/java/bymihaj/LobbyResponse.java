package bymihaj;

import java.util.ArrayList;
import java.util.List;

public class LobbyResponse {
    
    private List<Round> available;
    private List<Round> pending;
    private List<Round> current;
    
    public LobbyResponse() {
        setAvailable(new ArrayList<>());
    }

    public List<Round> getAvailable() {
        return available;
    }

    public void setAvailable(List<Round> available) {
        this.available = available;
    }

    public List<Round> getPending() {
        return pending;
    }

    public void setPending(List<Round> pending) {
        this.pending = pending;
    }

    public List<Round> getCurrent() {
        return current;
    }

    public void setCurrent(List<Round> current) {
        this.current = current;
    }

}
