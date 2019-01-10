package bymihaj.client.result;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import bymihaj.Round;
import bymihaj.client.Connection;
import bymihaj.client.RoundHolder;
import bymihaj.data.game.PlayedRoundRequest;
import bymihaj.data.game.PlayedRoundResponse;
import bymihaj.data.game.RoundTableRequest;

public class PlayedRoundPane extends VerticalPanel {
    
    //protected List<String> list;
    protected DataGrid<Round> grid;
    
    public PlayedRoundPane(Connection conn) {
        //list = new ArrayList<>();
        addStyleName("region-pane");
        
        Label label = new Label("Played rounds");
        label.addStyleName("region-title");
        add(label);
        
        grid = new DataGrid<Round>();
        grid.setSize("320px", "470px");
        grid.addStyleName("result-table");
        add(grid);
        setCellHorizontalAlignment(grid, HasHorizontalAlignment.ALIGN_CENTER);
        
        
        grid.addColumn(new TextColumn<Round>() {

            @Override
            public String getValue(Round object) {
                return "#" + String.valueOf(object.getRoundId());
            }
        }, "Round id");
        grid.setColumnWidth(0, "150px");
        
        grid.addColumn(new TextColumn<Round>() {

            @Override
            public String getValue(Round object) {
                return RoundHolder.dtf.format(new Date(object.getStartTime()));
            }
        }, "start time");
        grid.setColumnWidth(0, "150px");
        
        setWidth("340px");
        setHeight("550px");
        
        conn.send(new PlayedRoundRequest());
      
        SingleSelectionModel<Round> selection = new SingleSelectionModel<>();
        grid.setSelectionModel(selection);
        selection.addSelectionChangeHandler(e -> {
            if(selection.getSelectedObject() != null) {
                Long id = Long.valueOf(selection.getSelectedObject().getRoundId());
                RoundTableRequest reques = new RoundTableRequest();
                reques.setRoundId(id);
                conn.send(reques);
            }
        });
    }
    
    public void onPlayedRoud(PlayedRoundResponse resp) {
        List<String> list = new ArrayList<>();
        resp.getRoundList().forEach( l -> list.add(String.valueOf(l)));
        grid.setRowData(resp.getRoundList());
    }

}
