package bymihaj;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

public class LoginPane extends BorderPane {

    protected Connection connection;
    
    protected Label userLabel;
    protected Label passLabel;
    protected TextField userField;
    protected TextField passField;
    
    
    public LoginPane(Connection connection) {
        this.connection = connection;
        connection.subscribe(AccountResponse.class, this::onMessage);
        
        Button reqestAccountBtn = new Button("Request account");
        reqestAccountBtn.setOnAction(this::requestAccountAction);
        
        Button enterButton = new Button("Enter");
        enterButton.setOnAction(this::enterAction);
        
        userLabel = new Label("XYZabc");
        passLabel = new Label("***");
        userField = new TextField();
        passField = new TextField();
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(25));
        
        grid.add(new Label("* Create account"), 0, 0);
        grid.add(new Label("Login"), 0, 1);
        grid.add(userLabel, 0, 2);
        grid.add(new Label("Password"), 0, 3);
        grid.add(passLabel, 0, 4);
        grid.add(reqestAccountBtn, 0, 5);
        
        grid.add(new Label("* Login"), 1, 0);
        grid.add(new Label("User"), 1, 1);
        grid.add(userField, 1, 2);
        grid.add(new Label("Password"), 1, 3);
        grid.add(passField, 1, 4);
        grid.add(enterButton, 1, 5);
        
        setCenter(grid);
    }
    
    public void requestAccountAction(ActionEvent event) {
        connection.send(new AccountRequest());
    }
    
    public void enterAction(ActionEvent event) {
        LoginRequest req = new LoginRequest();
        req.setUser(userField.getText());
        req.setPass(passField.getText());
        connection.send(req);
    }
    
    public void onMessage(AccountResponse msg) {
        userLabel.setText(msg.getUser());
        passLabel.setText(msg.getPass());
        userField.setText(msg.getUser());
        passField.setText(msg.getPass());
    }
    
    public String getUser() {
    	return userField.getText();
    }
}
