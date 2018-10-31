package bymihaj.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import bymihaj.AccountRequest;
import bymihaj.AccountResponse;
import bymihaj.LoginRequest;

public class LoginPane extends VerticalPanel {
    
    protected Connection conn;
    protected TextBox userTbx;
    protected TextBox passTbx;
    
    public LoginPane(Connection conn) {
        this.conn = conn;
        
        userTbx = new TextBox();
        userTbx.setWidth("192px");
        passTbx = new TextBox();
        passTbx.setWidth("192px");
        
        setSpacing(5);
        
        
        Button enterBtn = new Button();
        enterBtn.setWidth("200px");
        enterBtn.setText("Enter");
        enterBtn.addClickHandler(e -> {
            LoginRequest login = new LoginRequest();
            login.setUser(userTbx.getText());
            login.setPass(passTbx.getText());
            conn.send(login);
            Window.setTitle(userTbx.getText());
        });
        
        Button requestAccountBtn = new Button();
        requestAccountBtn.setWidth("200px");
        requestAccountBtn.setText("Request account");
        requestAccountBtn.addClickHandler(e -> {
            conn.send(new AccountRequest());
        });
        
        add(new Label(" * Entering"));
        add(new Label("User"));
        add(userTbx);
        add(new Label("Password"));
        add(passTbx);
        add(enterBtn);
        add(new HTML("<hr  style=\"width:100%;\" />"));
        add(requestAccountBtn);
        
        conn.subscribe(AccountResponse.class, this::onAccountResponse);
    }
    
    public void onAccountResponse(AccountResponse accountResponse) {
        userTbx.setText(accountResponse.getUser());
        passTbx.setText(accountResponse.getPass());
    }

}
