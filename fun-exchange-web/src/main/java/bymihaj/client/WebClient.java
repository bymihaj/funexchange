package bymihaj.client;

import bymihaj.AccountRequest;
import bymihaj.AccountResponse;
import bymihaj.LoginResponse;
import bymihaj.MessageListener;
import bymihaj.MessageResolver;
import bymihaj.shared.FieldVerifier;


import java.util.logging.Logger;

import org.realityforge.gwt.websockets.client.WebSocket;
import org.realityforge.gwt.websockets.client.WebSocketListener;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WebClient implements EntryPoint {
  /**
   * The message displayed to the user when the server cannot be reached or
   * returns an error.
   */
  private static final String SERVER_ERROR = "An error occurred while "
      + "attempting to contact the server. Please check your network "
      + "connection and try again.";

  /**
   * Create a remote service proxy to talk to the server-side Greeting service.
   */
  private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);

  private final Messages messages = GWT.create(Messages.class);
  
  static Logger log = Logger.getGlobal();
  public static String user;
  
  protected Connection conn;
  protected RootPanel mainScreen;
  protected LoginPane loginPane;
  protected VerticalPanel loginHolder;
  static protected Widget current;
  

  public void onModuleLoad() {
      
      //conn = new Connection("ws://127.0.0.1:7575");
      conn = new Connection("ws://159.89.0.62:7575");
            
      //loginPane = new LoginPane(conn);
      //mainScreen = RootPanel.get("allContent");
      
      /*
      loginHolder = new VerticalPanel();
      loginHolder.setWidth("100%");
      loginHolder.setHeight("100%");
      loginHolder.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
      loginHolder.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
      loginHolder.add(loginPane);
      */
      //mainScreen.add(loginHolder);
      
      switchPane(new LoginPane(conn), false);
      
      conn.subscribe(LoginResponse.class, this::onLoginResponse);
  }
  
  public void onLoginResponse(LoginResponse loginResponse) {
      if(LoginResponse.Status.OK.equals(loginResponse.getStatus())) {
          RootPanel.getBodyElement().getStyle().setBackgroundImage("none");
          switchPane(new LobbyPane(conn), true);
          
          //mainScreen.remove(loginHolder);
          //if(RootPanel.get("allContent").getWidgetCount() == 0) { 
              /*
              MainPane mainPane = new MainPane(conn);
              mainScreen.add(mainPane);
              */
              
              
              //RootPanel.getBodyElement().getStyle().setBackgroundImage("none");
              /*
              // TODO re-work as component
              LobbyPane lobby = new LobbyPane(conn);
              VerticalPanel lobbyHolder = new VerticalPanel();
              lobbyHolder.setWidth("100%");
              lobbyHolder.setHeight("100%");
              lobbyHolder.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
              lobbyHolder.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
              lobbyHolder.add(lobby);
              
              mainScreen.add(lobbyHolder);
              */
              switchPane(new LobbyPane(conn), true);
              
          //}
         
      } else {
          Window.alert("Login rejected, wrong user/pass");
      }
  }
  
  static public void switchPane(Widget widget, boolean top) {
      RootPanel mainScreen = RootPanel.get("allContent");
      if(current != null) {
          mainScreen.remove(current); 
      }
      
      VerticalPanel holder = new VerticalPanel();
      holder.setWidth("100%");
      holder.setHeight("100%");
      holder.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
      if(top) {
          holder.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
      } else {
          holder.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
      }
      holder.add(widget);
      mainScreen.add(holder);
      current = holder;
  }
  
  /**
   * This is the entry point method.
   */
  public void onModuleLoad_Remove() {
    final Button sendButton = new Button( messages.sendButton() );
    final TextBox nameField = new TextBox();
    nameField.setText( messages.nameField() );
    final Label errorLabel = new Label();

    // We can add style names to widgets
    sendButton.addStyleName("sendButton");

    // Add the nameField and sendButton to the RootPanel
    // Use RootPanel.get() to get the entire body element
    RootPanel.get("nameFieldContainer").add(nameField);
    RootPanel.get("sendButtonContainer").add(sendButton);
    RootPanel.get("errorLabelContainer").add(errorLabel);

    // Focus the cursor on the name field when the app loads
    nameField.setFocus(true);
    nameField.selectAll();

    // Create the popup dialog box
    final DialogBox dialogBox = new DialogBox();
    dialogBox.setText("Remote Procedure Call");
    dialogBox.setAnimationEnabled(true);
    final Button closeButton = new Button("Close");
    // We can set the id of a widget by accessing its Element
    closeButton.getElement().setId("closeButton");
    final Label textToServerLabel = new Label();
    final HTML serverResponseLabel = new HTML();
    VerticalPanel dialogVPanel = new VerticalPanel();
    dialogVPanel.addStyleName("dialogVPanel");
    dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
    dialogVPanel.add(textToServerLabel);
    dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
    dialogVPanel.add(serverResponseLabel);
    dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
    dialogVPanel.add(closeButton);
    dialogBox.setWidget(dialogVPanel);

    // Add a handler to close the DialogBox
    closeButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        sendButton.setEnabled(true);
        sendButton.setFocus(true);
      }
    });

    // Create a handler for the sendButton and nameField
    class MyHandler implements ClickHandler, KeyUpHandler {
      /**
       * Fired when the user clicks on the sendButton.
       */
      public void onClick(ClickEvent event) {
        sendNameToServer();
      }

      /**
       * Fired when the user types in the nameField.
       */
      public void onKeyUp(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          sendNameToServer();
        }
      }

      /**
       * Send the name from the nameField to the server and wait for a response.
       */
      private void sendNameToServer() {
        // First, we validate the input.
        errorLabel.setText("");
        String textToServer = nameField.getText();
        if (!FieldVerifier.isValidName(textToServer)) {
          errorLabel.setText("Please enter at least four characters");
          return;
        }

        // Then, we send the input to the server.
        sendButton.setEnabled(false);
        textToServerLabel.setText(textToServer);
        serverResponseLabel.setText("");
        greetingService.greetServer(textToServer, new AsyncCallback<String>() {
          public void onFailure(Throwable caught) {
            // Show the RPC error message to the user
            dialogBox.setText("Remote Procedure Call - Failure");
            serverResponseLabel.addStyleName("serverResponseLabelError");
            serverResponseLabel.setHTML(SERVER_ERROR);
            dialogBox.center();
            closeButton.setFocus(true);
          }

          public void onSuccess(String result) {
            dialogBox.setText("Remote Procedure Call");
            serverResponseLabel.removeStyleName("serverResponseLabelError");
            serverResponseLabel.setHTML(result);
            dialogBox.center();
            closeButton.setFocus(true);
          }
        });
      }
    }

    // Add a handler to send the name to the server
    MyHandler handler = new MyHandler();
    sendButton.addClickHandler(handler);
    nameField.addKeyUpHandler(handler);
  }
}
