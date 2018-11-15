package bymihaj.client;

import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;

public class LobbyPane extends DockPanel {
    
    public LobbyPane() {
        
        Label profilePane = new Label("Profile pane there");
        DecoratorPanel profileDec = new DecoratorPanel();
        profileDec.setWidget(profilePane);
        add(profileDec, DockPanel.NORTH);
        
        Label availablePane = new Label("Available rounds");
        DecoratorPanel availableDec = new DecoratorPanel();
        availableDec.setWidget(availablePane);
        add(availableDec, DockPanel.WEST);
        
        Label registeredPane = new Label("Registered rounds");
        DecoratorPanel registeredDec = new DecoratorPanel();
        registeredDec.setWidget(registeredPane);
        add(registeredDec, DockPanel.CENTER);
        
        Label currentPane = new Label("Current pane");
        DecoratorPanel currentDec = new DecoratorPanel();
        currentDec.setWidget(currentPane);
        add(currentDec, DockPanel.EAST);
        
    }

}
