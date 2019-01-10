package bymihaj.client.result;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import bymihaj.client.Connection;
import bymihaj.client.LobbyPane;
import bymihaj.client.WebClient;

public class ResultHeader extends DockPanel {

    public ResultHeader(Connection connection) {
        setSpacing(20);
        setSize("1010px", "65px");
        
        Label label = new Label(WebClient.user);
        label.addStyleName("user-nick-label");
        add(label, DockPanel.WEST);
        
        Button button = new Button("Lobby");
        button.addStyleName("lobby-button");
        button.addClickHandler(e -> {
            //RootPanel.get("allContent").clear();
            //RootPanel.get("allContent").add(new LobbyPane(connection));
            WebClient.switchPane(new LobbyPane(connection), true);
        });
        add(button, DockPanel.EAST);
        setCellHorizontalAlignment(button, ALIGN_RIGHT);
    }
    
}
