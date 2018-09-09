package bymihaj;

public interface ClientMessageListener<T> {
    
    void onMessage(User user, T msg);
}
