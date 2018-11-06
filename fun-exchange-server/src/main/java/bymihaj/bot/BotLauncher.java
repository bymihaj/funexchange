package bymihaj.bot;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotLauncher {
    
    static Logger log = LoggerFactory.getLogger(BotLauncher.class);
    
    public static void main(String...args) {
        run(1);
    }
    
    public static void run(int count) {
        try {
            URI uri = new URI("ws://159.89.0.62:7575");
            
            for(int i = 0 ; i< count; i++) {
                Thread.sleep(50);
                new RandomBot(uri);
            }
        } catch (URISyntaxException e) {
            log.error("URISyntaxException", e);
        } catch (InterruptedException ei ) {
            log.error("InterruptedException", ei);
        } 
    }

}