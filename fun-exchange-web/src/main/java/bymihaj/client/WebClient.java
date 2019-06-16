package bymihaj.client;

import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import bymihaj.LoginResponse;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WebClient implements EntryPoint {
  
  static Logger log = Logger.getGlobal();
  public static String user;
  
  protected Connection conn;
  protected RootPanel mainScreen;
  protected LoginPane loginPane;
  protected VerticalPanel loginHolder;
  static protected Widget current;
  

  public void onModuleLoad() {
      
      conn = new Connection("ws://127.0.0.1:7575");
            
      switchPane(new LoginPane(conn), false);
      
      conn.subscribe(LoginResponse.class, this::onLoginResponse);
  }
  
  public void onLoginResponse(LoginResponse loginResponse) {
      if(LoginResponse.Status.OK.equals(loginResponse.getStatus())) {
          RootPanel.getBodyElement().getStyle().setBackgroundImage("none");
          switchPane(new LobbyPane(conn), true);
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
  
}
