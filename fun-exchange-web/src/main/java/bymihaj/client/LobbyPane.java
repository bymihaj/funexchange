package bymihaj.client;

import com.google.gwt.dom.client.Style.Unit;
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
        setSpacing(20);
        LobbyHeader profilePane = new LobbyHeader(connection);
        //DecoratorPanel profileDec = new DecoratorPanel();
        //profileDec.setWidget(profilePane);
        add(profilePane, DockPanel.NORTH);
        
        availablePane = new AvailableRoundHolder("Available rounds", connection); //new RoundHolder("Available rounds", connection);
        //DecoratorPanel availableDec = new DecoratorPanel();
        //availableDec.setWidget(availablePane);
        add(availablePane, DockPanel.WEST);
        
        registeredPane = new RegisteredRoundHolder("Registered rounds", connection);//new RoundHolder("Registered rounds", connection);
        //DecoratorPanel registeredDec = new DecoratorPanel();
        //registeredDec.setWidget(registeredPane);
        add(registeredPane, DockPanel.CENTER);
        
        currentPane = new ActiveRoundHolder("Active rounds", connection);// new RoundHolder("Current pane", connection);
        //DecoratorPanel currentDec = new DecoratorPanel();
        //currentDec.setWidget(currentPane);
        add(currentPane, DockPanel.EAST);
        
        connection.subscribe(LobbyResponse.class, this::onLobby);
        connection.send(new LobbyRequest());
    }
    
    public void onLobby(LobbyResponse response) {
        /*
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
        */
        availablePane.addRounds(response.getAvailable());
        
        /*
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
        */
        registeredPane.addRounds(response.getPending());
        
        /*
        currentPane.clearRounds();
        for(Round round : response.getCurrent()) {
            Button button = new Button("Enter");
            button.addClickHandler(e -> {
                RootPanel.get("allContent").clear();
                RootPanel.get("allContent").add(new MainPane(conneciton));
            });
            currentPane.addRound(round, button);
        }
        */
        currentPane.addRounds(response.getCurrent());
    }

}
