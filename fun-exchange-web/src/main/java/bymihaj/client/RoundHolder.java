package bymihaj.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.DockPanel.DockLayoutConstant;

import bymihaj.Round;

abstract public class RoundHolder extends VerticalPanel {

    public static DateTimeFormat dtf = DateTimeFormat.getFormat("HH:mm dd-MMM-yy");
    
    //protected VerticalPanel holder;
    protected DataGrid<Round> grid;
    //protected List<Round> provider;
    protected Connection conn;
    
    
    public RoundHolder(String name, String help, Connection conn) {
        //provider = new ArrayList<>();
        this.conn = conn;
        addStyleName("region-pane");
        setSize("500px", "367px");
        
        /*
        Label label = new Label(name);
        label.addStyleName("region-title");
        //add(label);
        
        Image helpIcon = new Image("image/help.png");
        helpIcon.addStyleName("region-help");
        helpIcon.addClickHandler(e -> {
            PopupPanel pp = new PopupPanel(true);
            pp.setWidget(new Label("Just for test"));
            pp.setPopupPosition(helpIcon.getAbsoluteLeft() + 60, helpIcon.getAbsoluteTop());
            pp.show();
        });
        
        //add(helpIcon);
        DockPanel titleHeader = new DockPanel();
        titleHeader.add(label, DockPanel.WEST);
        titleHeader.add(helpIcon, DockPanel.EAST);
        titleHeader.setCellHorizontalAlignment(helpIcon, HasHorizontalAlignment.ALIGN_RIGHT);
        titleHeader.setWidth("500px");
        add(titleHeader);
        */
        add(new RegionHeader(name, help, "500px"));
        
        //holder = new VerticalPanel();
        //add(holder);
        grid = new DataGrid<>();
        grid.addStyleName("table-header");
        grid.setSize("460px", "240px");
        add(grid);
        setCellHorizontalAlignment(grid, HasHorizontalAlignment.ALIGN_CENTER);
        
        
        grid.addColumn(new TextColumn<Round>() {

            @Override
            public String getValue(Round object) {
                return "#"+object.getRoundId();
            }
        }, "round ID");
        grid.setColumnWidth(0, "80px");
        
        grid.addColumn(new TextColumn<Round>() {

            @Override
            public String getValue(Round object) {
                return dtf.format(new Date(object.getStartTime()));
            }
        }, "start time");
        grid.setColumnWidth(1, "110px");
        
        
        grid.addColumn(new TextColumn<Round>() {

            @Override
            public String getValue(Round object) {
                return object.getDuration()/1000/60 + "m";
            }
        }, "duration");
        grid.setColumnWidth(2, "50px");
        
        // TODO Button
    }
    
    /*
    public void clearRounds() {
        provider.clear();
        grid.setRowData(provider);
    }*/
    
    public void addRounds(List<Round> list) {
        grid.setRowData(list);
    }
    
    /*
    public void addRound(Round round, Button button) {
        HorizontalPanel pane = new HorizontalPanel();
        pane.setSpacing(15);
        pane.add(new Label("#" + round.getRoundId()));
        pane.add(new Label("Started: " + dtf.format(new Date(round.getStartTime()))));
        pane.add(new Label(round.getDuration()/1000/60 + "m"));
        pane.add(button);
        holder.add(pane);
    }*/
}
