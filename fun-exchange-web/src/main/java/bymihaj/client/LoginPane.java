package bymihaj.client;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.DockPanel.DockLayoutConstant;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
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
        
        setWidth("610px");
        setHeight("630px");
        setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
       
        
        getElement().getStyle().setBackgroundColor("white");
        getElement().getStyle().setPaddingBottom(100, Unit.PX);
        getElement().getStyle().setPaddingTop(100, Unit.PX);
        
        userTbx = new TextBox();
        userTbx.getElement().setPropertyString("placeholder", "User");
        userTbx.addStyleName("login-input");
        
        passTbx = new TextBox();
        passTbx.getElement().setPropertyString("placeholder", "Password");
        passTbx.addStyleName("login-input");
        
        setSpacing(5);
        
        
        Button enterBtn = new Button();
        enterBtn.addStyleName("login-button");
        enterBtn.addStyleName("enter-btn");
        enterBtn.setText("Enter");
        enterBtn.addClickHandler(e -> {
            LoginRequest login = new LoginRequest();
            login.setUser(userTbx.getText());
            login.setPass(passTbx.getText());
            conn.send(login);
            Window.setTitle(userTbx.getText());
            WebClient.user = userTbx.getText();
        });
        
        Button requestAccountBtn = new Button();
        requestAccountBtn.addStyleName("login-button");
        requestAccountBtn.addStyleName("request-acc");
        requestAccountBtn.setText("Request account");
        requestAccountBtn.addClickHandler(e -> {
            conn.send(new AccountRequest());
        });
        
        
        Label logo = new Label("FUN EXCHANGE");
        logo.addStyleName("logo");
        add(logo);
        
        Label text = new Label("No real assets required, just sandbox for learning and playing how to work exchange.");
        text.addStyleName("welcome-text");
        add(text);
        
        add(userTbx);
        add(passTbx);
        
        
        DockPanel buttonPane = new DockPanel();
        buttonPane.setWidth("404px");
        buttonPane.add(requestAccountBtn, DockPanel.WEST);
        buttonPane.setCellHorizontalAlignment(requestAccountBtn, HasHorizontalAlignment.ALIGN_LEFT);
        buttonPane.add(enterBtn, DockPanel.EAST);
        buttonPane.setCellHorizontalAlignment(enterBtn, HasHorizontalAlignment.ALIGN_RIGHT);
        add(buttonPane);
        
        
        
        conn.subscribe(AccountResponse.class, this::onAccountResponse);
    }
    
    public void onAccountResponse(AccountResponse accountResponse) {
        userTbx.setText(accountResponse.getUser());
        passTbx.setText(accountResponse.getPass());
    }

}
