package bymihaj.client;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.Column;

import bymihaj.Round;
import bymihaj.RoundRegisterRequest;

public class RegisteredRoundHolder extends RoundHolder {

    static String HELP ="List of subcribed round, just wait a starting time.";
    
    public RegisteredRoundHolder(String name, Connection conn) {
        super(name, HELP, conn);
        grid.addColumn(new CancelButton());
        grid.setColumnWidth(3, "80px");
    }

    class CancelButton extends Column<Round, String> {

        public CancelButton() {
            super(new ButtonCell());
            setFieldUpdater(new FieldUpdater<Round, String>() {
                
                @Override
                public void update(int index, Round object, String value) {
                    RoundRegisterRequest req = new RoundRegisterRequest();
                    req.setJoin(false);
                    req.setRoundId(object.getRoundId());
                    conn.send(req);
                    
                }
            });
            setCellStyleNames("cancel-button");
        }

        @Override
        public String getValue(Round object) {
           return "CANCEL";
        }
        
    }
}
