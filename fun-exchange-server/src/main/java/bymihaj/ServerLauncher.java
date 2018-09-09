package bymihaj;

import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerLauncher {
    
    static Logger log = LoggerFactory.getLogger(ServerLauncher.class);
    
    public static void main( String[] args ) {
        log.info("Loger is enable");
        
        Server server = new Server(new InetSocketAddress(7575));
        server.start();
    }
    
    /*
    static class WSListener extends WebSocketServer {

        static Logger log = LoggerFactory.getLogger(WSListener.class);
        
        public WSListener(InetSocketAddress address) {
            super(address);
        }
        
        @Override
        public void onOpen(WebSocket paramWebSocket, ClientHandshake paramClientHandshake) {
            log.info("onOpen");
        }

        @Override
        public void onClose(WebSocket paramWebSocket, int paramInt, String paramString, boolean paramBoolean) {
            log.info("onClose");            
        }

        @Override
        public void onMessage(WebSocket paramWebSocket, String paramString) {
            log.info("onMessage: {}", paramString);      
        }

        @Override
        public void onError(WebSocket paramWebSocket, Exception paramException) {
            log.error("onError", paramException);            
        }

        @Override
        public void onStart() {
            log.info("onStart");            
        }
        
    }
    */
}
