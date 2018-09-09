package bymihaj;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ClientLauncher extends Application{
    
    public static void main( String[] args ) {
       
        /*
        JFrame frame = new  JFrame("Fun exchange client");
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
        JPanel panel = new JPanel(new GridBagLayout());
        frame.setContentPane(panel);
        
        add(panel, new JLabel("* Create account"), 0, 0);
        add(panel, new JLabel("Login"), 0, 1);
        add(panel, new JLabel("xy123"), 0, 2);
        add(panel, new JLabel("Password"), 0, 3);
        add(panel, new JLabel("***"), 0, 4);
        add(panel, new JButton("Request account"), 0, 5);
        
        
        add(panel, new JLabel("* Login"), 1, 0);
        add(panel, new JLabel("User"), 1, 1);
        add(panel, new JTextField(), 1, 2);
        add(panel, new JLabel("Password"), 1, 3);
        add(panel, new JTextField(), 1, 4);
        add(panel, new JButton("Enter"), 1, 5);
        
        panel.revalidate();
        
        try {
            WSClient wsClient = new WSClient(new URI("ws://127.0.0.1:7575"));
            wsClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        */
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        /*
        final WSClient wsClient = new WSClient(new URI("ws://127.0.0.1:7575"));
        MessageResolver resolver = new MessageResolver();
       
        
        Button reqestAccountBtn = new Button("Request account");
        reqestAccountBtn.setOnAction(event -> {
            wsClient.send(resolver.pack(new AccountRequest()));
        });
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(25));
        
        
        grid.add(new Label("* Create account"), 0, 0);
        grid.add(new Label("Login"), 0, 1);
        grid.add(new Label("xy123"), 0, 2);
        grid.add(new Label("Password"), 0, 3);
        grid.add(new Label("***"), 0, 4);
        grid.add(reqestAccountBtn, 0, 5);
        
        grid.add(new Label("* Login"), 1, 0);
        grid.add(new Label("User"), 1, 1);
        grid.add(new TextField(), 1, 2);
        grid.add(new Label("Password"), 1, 3);
        grid.add(new TextField(), 1, 4);
        grid.add(new Button("Enter"), 1, 5);
        
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(grid);
        
        Scene scene = new Scene(borderPane, 900, 600);
        primaryStage.setTitle("Fun exchange client");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        wsClient.connect();
        */
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
                    scene.setRoot(new TradePane(conn));
                }
            }
        });
        
    }
    
    /*
    // TODO remove
    public static void add(JPanel panel, JComponent component, int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
    }
    
    
    static class WSClient extends WebSocketClient {

        public WSClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            System.out.println("onOpen");
            
        }

        @Override
        public void onMessage(String message) {
            System.out.println("onMessage "+message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            System.out.println("onClose");
            
        }

        @Override
        public void onError(Exception ex) {
            System.out.println("onError");
            ex.printStackTrace();
        }
        
    }
    */

    
}
