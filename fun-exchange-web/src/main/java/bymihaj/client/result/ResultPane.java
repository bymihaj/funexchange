package bymihaj.client.result;


import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DockPanel;

import bymihaj.client.Connection;
import bymihaj.data.game.PlayedRoundResponse;
import bymihaj.data.game.RoundTableResponse;

public class ResultPane extends DockPanel {
    
    public ResultPane(Connection connection) {
        setSpacing(20);
        ResultHeader header = new ResultHeader(connection);
        //DecoratorPanel headerDoc = new DecoratorPanel();
        //headerDoc.setWidget(header);
        add(header, DockPanel.NORTH);
        
        PlayedRoundPane roundPane = new PlayedRoundPane(connection);
        connection.subscribe(PlayedRoundResponse.class, roundPane::onPlayedRoud);
        //DecoratorPanel roundDec = new DecoratorPanel();
        //roundDec.setWidget(roundPane);
        add(roundPane, DockPanel.WEST);
        
        TablePane tablePane = new TablePane();
        connection.subscribe(RoundTableResponse.class, tablePane::onRoundTable);
        //DecoratorPanel tableDec = new DecoratorPanel();
        //tableDec.setWidget(tablePane);
        add(tablePane, DockPanel.EAST);
    }

}
