package bymihaj.bot;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotLauncher {
    
    static Logger log = LoggerFactory.getLogger(BotLauncher.class);
    
    public static void main(String...args) {
        run(10);
    }
    
    public static void run(int count) {
        try {
            URI uri = new URI("ws://159.89.0.62:7575");
            //URI uri = new URI("ws://127.0.0.1:7575");
            
            
            for(int i = 0 ; i< count; i++) {
                Thread.sleep(100);
                new RandomBot(uri);
                new LimitBot(uri);
            }
        } catch (URISyntaxException e) {
            log.error("URISyntaxException", e);
        } catch (InterruptedException ei ) {
            log.error("InterruptedException", ei);
        } 
    }

}
