package bymihaj;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bymihaj.bot.BotLauncher;

public class ServerLauncher {
    
    static Logger log = LoggerFactory.getLogger(ServerLauncher.class);
    
    public static void main( String[] args ) {
        log.info("Loger is enable");
        
        Server server = new Server(new InetSocketAddress(7575));
        server.start();
        
        BotLauncher.run(10);
    }
    
}
