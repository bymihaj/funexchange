package bymihaj.client;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import bymihaj.Round;

public class RoundHolder extends VerticalPanel {

    protected VerticalPanel holder;
    protected DateTimeFormat dtf = DateTimeFormat.getFormat("HH:mm dd-MMM-yy");
    
    public RoundHolder(String name) {
        setSize("300px", "400px");
        add(new Label(name));
        holder = new VerticalPanel();
        add(holder);
    }
    
    public void clearRounds() {
        holder.clear();
    }
    
    public void addRound(Round round, Button button) {
        HorizontalPanel pane = new HorizontalPanel();
        pane.setSpacing(15);
        pane.add(new Label("#" + round.getRoundId()));
        pane.add(new Label("Started: " + dtf.format(new Date(round.getStartTime()))));
        pane.add(new Label(round.getDuration()/1000/60 + "m"));
        pane.add(button);
        holder.add(pane);
    }
}
