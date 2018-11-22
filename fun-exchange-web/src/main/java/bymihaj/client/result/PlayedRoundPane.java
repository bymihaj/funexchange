package bymihaj.client.result;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import bymihaj.client.Connection;
import bymihaj.data.game.PlayedRoundRequest;
import bymihaj.data.game.PlayedRoundResponse;
import bymihaj.data.game.RoundTableRequest;

public class PlayedRoundPane extends DataGrid<String> {
    
    protected List<String> list;
    
    public PlayedRoundPane(Connection conn) {
        list = new ArrayList<>();
        
        addColumn(new TextColumn<String>() {

            @Override
            public String getValue(String object) {
                return object;
            }
        }, "Round id");
        setColumnWidth(0, "150px");
        
        setWidth("150px");
        setHeight("490px");
        
        conn.send(new PlayedRoundRequest());
      
        SingleSelectionModel<String> selection = new SingleSelectionModel<>();
        setSelectionModel(selection);
        selection.addSelectionChangeHandler(e -> {
            if(selection.getSelectedObject() != null) {
                Long id = Long.valueOf(selection.getSelectedObject());
                RoundTableRequest reques = new RoundTableRequest();
                reques.setRoundId(id);
                conn.send(reques);
            }
        });
    }
    
    public void onPlayedRoud(PlayedRoundResponse resp) {
        list.clear();
        resp.getRoundList().forEach( l -> list.add(String.valueOf(l)));
        setRowData(list);
    }

}
