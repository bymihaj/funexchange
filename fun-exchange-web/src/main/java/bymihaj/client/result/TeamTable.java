package bymihaj.client.result;

import java.util.List;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;

import bymihaj.data.game.PlayedRecord;

public class TeamTable extends DataGrid<PlayedRecord>{
    
    public TeamTable() {
        addColumn(new TextColumn<PlayedRecord>() {

            @Override
            public String getValue(PlayedRecord object) {
                return String.valueOf(object.getPosition());
            }
        }, "Position");
        setColumnWidth(0, "100px");
        
        addColumn(new TextColumn<PlayedRecord>() {

            @Override
            public String getValue(PlayedRecord object) {
                return String.valueOf(object.getAmount());
            }
        }, "Amount");
        setColumnWidth(1, "100px");
        
        
        addColumn(new TextColumn<PlayedRecord>() {

            @Override
            public String getValue(PlayedRecord object) {
                return object.getUser();
            }
        }, "User");
        setColumnWidth(2, "100px");
        
        setWidth("300px");
        setHeight("445px");
    }
    
    public void load(List<PlayedRecord> list) {
        setRowData(list);
    }
}
