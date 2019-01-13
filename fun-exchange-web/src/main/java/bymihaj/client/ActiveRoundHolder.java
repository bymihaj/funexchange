package bymihaj.client;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.RootPanel;

import bymihaj.Round;


public class ActiveRoundHolder extends RoundHolder {

    public ActiveRoundHolder(String name, Connection conn) {
        super(name, conn);
        grid.addColumn(new EnterButton());
        grid.setColumnWidth(3, "80px");
    }
    
    class EnterButton extends Column<Round, String> {

        public EnterButton() {
            super(new ButtonCell());
            setFieldUpdater(new FieldUpdater<Round, String>() {
                
                @Override
                public void update(int index, Round object, String value) {
                    // TODO Auto-generated method stub
                    //RootPanel.get("allContent").clear();
                    //RootPanel.get("allContent").add(new MainPane(conn));
                    WebClient.switchPane(new MainPane(conn), true);
                }
            });
            setCellStyleNames("enter-round-button");
        }

        @Override
        public String getValue(Round object) {
            return "ENTER";
        }
        
    }

}
