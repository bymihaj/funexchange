package bymihaj;

public class LoginResponse {

    public enum Status {
        OK,
        FAILED
    }
    
    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
  
}
