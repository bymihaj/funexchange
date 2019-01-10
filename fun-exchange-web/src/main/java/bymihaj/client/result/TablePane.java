package bymihaj.client.result;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import bymihaj.data.game.RoundTableResponse;

public class TablePane extends HorizontalPanel {

    protected TeamTable greenTable;
    protected TeamTable redTable;
    
    public TablePane() {
        addStyleName("region-pane");
        VerticalPanel greenPane = new VerticalPanel();
        greenPane.setSpacing(10);
        Label greenLabel = new Label("Green team");
        greenLabel.addStyleName("region-title-team-result");
        greenPane.add(greenLabel);
        greenTable = new TeamTable();
        greenPane.add(greenTable);
        add(greenPane);
        
        VerticalPanel redPane = new VerticalPanel();
        redPane.setSpacing(10);
        Label redLabel = new Label("Red team");
        redLabel.addStyleName("region-title-team-result");
        redPane.add(redLabel);
        redTable = new TeamTable();
        redPane.add(redTable);
        add(redPane);
    }
    
    public void onRoundTable(RoundTableResponse table) {
        greenTable.load(table.getGreen());
        redTable.load(table.getRed());
    }
}
