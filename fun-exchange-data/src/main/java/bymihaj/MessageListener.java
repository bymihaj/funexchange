package bymihaj;

public interface MessageListener<T> {
    
    void onMessage(T msg);

}
