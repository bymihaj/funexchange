package bymihaj.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import bymihaj.LobbyRequest;
import bymihaj.LobbyResponse;
import bymihaj.Round;
import bymihaj.RoundRegisterRequest;

public class LobbyPane extends DockPanel {
    
    protected Connection conneciton;
    protected RoundHolder availablePane;
    protected RoundHolder registeredPane;
    protected RoundHolder currentPane;
    
    public LobbyPane(Connection connection) {
        this.conneciton = connection;
        LobbyHeader profilePane = new LobbyHeader(connection);
        DecoratorPanel profileDec = new DecoratorPanel();
        profileDec.setWidget(profilePane);
        add(profileDec, DockPanel.NORTH);
        
        availablePane = new RoundHolder("Available rounds");
        DecoratorPanel availableDec = new DecoratorPanel();
        availableDec.setWidget(availablePane);
        add(availableDec, DockPanel.WEST);
        
        registeredPane = new RoundHolder("Registered rounds");
        DecoratorPanel registeredDec = new DecoratorPanel();
        registeredDec.setWidget(registeredPane);
        add(registeredDec, DockPanel.CENTER);
        
        currentPane = new RoundHolder("Current pane");
        DecoratorPanel currentDec = new DecoratorPanel();
        currentDec.setWidget(currentPane);
        add(currentDec, DockPanel.EAST);
        
        connection.subscribe(LobbyResponse.class, this::onLobby);
        connection.send(new LobbyRequest());
    }
    
    public void onLobby(LobbyResponse response) {
        availablePane.clearRounds();
        for(Round round : response.getAvailable()) {
            Button button = new Button("Join");
            button.addClickHandler(e -> {
                RoundRegisterRequest req = new RoundRegisterRequest();
                req.setJoin(true);
                req.setRoundId(round.getRoundId());
                conneciton.send(req);
            });
            availablePane.addRound(round, button);
        }
        
        registeredPane.clearRounds();
        for(Round round : response.getPending()) {
            Button button = new Button("Cancel");
            button.addClickHandler(e -> {
                RoundRegisterRequest req = new RoundRegisterRequest();
                req.setJoin(false);
                req.setRoundId(round.getRoundId());
                conneciton.send(req);
            });
            registeredPane.addRound(round, button);
        }
        
        currentPane.clearRounds();
        for(Round round : response.getCurrent()) {
            Button button = new Button("Enter");
            button.addClickHandler(e -> {
                RootPanel.get("allContent").clear();
                RootPanel.get("allContent").add(new MainPane(conneciton));
            });
            currentPane.addRound(round, button);
        }
    }

}
