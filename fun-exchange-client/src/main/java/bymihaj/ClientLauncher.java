package bymihaj;

import java.net.URI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class ClientLauncher extends Application{
    
    public static void main( String[] args ) {
       
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
    	primaryStage.setOnCloseRequest( e -> System.exit(0));
        
        Connection conn = new Connection(new URI("ws://127.0.0.1:7575"));
        LoginPane loginPane = new LoginPane(conn);
        
        
        Scene scene = new Scene(loginPane, 900, 600);
        //Scene scene = new Scene(new TradePane(conn), 900, 600);
        
        primaryStage.setTitle("Fun exchange client");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        conn.connect();
        
        conn.subscribe(LoginResponse.class, new MessageListener<LoginResponse>() {

            @Override
            public void onMessage(LoginResponse msg) {
                if ( msg.getStatus() == LoginResponse.Status.OK) {
                	primaryStage.setTitle("Fun exchange client ("+loginPane.getUser()+")");
                    scene.setRoot(new TradePane(conn));
                } else {
                	Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Authorization failed");
                    alert.setHeaderText("Wrong user or password");
                    alert.showAndWait();
                }
            }
        });
        
    }
    
}
