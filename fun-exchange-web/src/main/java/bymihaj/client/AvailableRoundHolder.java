package bymihaj.client;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.Column;

import bymihaj.Round;
import bymihaj.RoundRegisterRequest;

public class AvailableRoundHolder extends RoundHolder {

    public AvailableRoundHolder(String name, Connection conn) {
        super(name, conn);
        
        grid.addColumn(new JoinButton());
        grid.setColumnWidth(3, "80px");
        
    }

    class JoinButton extends Column<Round, String> {

        public JoinButton() {
            super(new ButtonCell());
            setFieldUpdater(new FieldUpdater<Round, String>() {
                
                @Override
                public void update(int index, Round object, String value) {
                    RoundRegisterRequest req = new RoundRegisterRequest();
                    req.setJoin(true);
                    req.setRoundId(object.getRoundId());
                    conn.send(req);
                }
            });
            setCellStyleNames("join-button");
        }

        @Override
        public String getValue(Round object) {
            return "JOIN";
        }
        
    }
}
