package bymihaj.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import bymihaj.client.result.ResultPane;

public class LobbyHeader extends DockPanel {
    
    public LobbyHeader(Connection connection) {
        setSpacing(20);
        setSize("1560px", "85px");
        
        Label userLabel = new Label(WebClient.user);
        userLabel.addStyleName("user-nick-label");
        add(userLabel, DockPanel.WEST);
        
        
        Button resultButton = new Button("Round results");
        resultButton.addStyleName("result-button");
        resultButton.addClickHandler(e -> {
            //RootPanel.get("allContent").clear();
            //RootPanel.get("allContent").add(new ResultPane(connection));
            WebClient.switchPane(new ResultPane(connection), true);
        });
        add(resultButton, DockPanel.EAST);
        setCellHorizontalAlignment(resultButton, ALIGN_RIGHT);
        
    }
}
