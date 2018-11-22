package bymihaj.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import bymihaj.Round;
import bymihaj.RoundStatus;
import bymihaj.RoundStatusRequest;

public class RoundHeader extends DockPanel {
    
    protected Label roundId;
    protected Label timeline;
    protected Label teamLabel;
    protected Round round;
    
    public RoundHeader(Connection connection) {
        setSpacing(20);
        setSize("1159px", "65px");
        
        roundId = new Label();
        add(roundId, DockPanel.WEST);
        
                
        teamLabel = new Label();
        add(teamLabel, DockPanel.WEST);
        
        timeline = new Label();
        add(timeline, DockPanel.CENTER);
        
        Button lobbyBtn = new Button("Lobby");
        lobbyBtn.addClickHandler(e -> {
            RootPanel.get("allContent").clear();
            RootPanel.get("allContent").add(new LobbyPane(connection));
        });
        add(lobbyBtn, DockPanel.EAST);
        setCellHorizontalAlignment(lobbyBtn, ALIGN_RIGHT);
        
        connection.send(new RoundStatusRequest());
        
        Timer time  = new Timer(){
            @Override
            public void run() {
                calcTimeline();
            }
        };
        time.scheduleRepeating(1000);
    }
    
    public void onRound(RoundStatus roundStatus) {
        roundId.setText("#"+roundStatus.getRound().getRoundId());
        
        String hint = "";
        switch (roundStatus.getTeam()) {
        case GREEN:
            hint = "(Buy more STK a to win)"; 
            break;
        case RED:
            hint = "(Sell STK and get more MON to win)";
            break;
        case SPECTATOR:
            hint = "(Just view at this round)";
            break;

        }
        
        teamLabel.setText("TEAM: "+roundStatus.getTeam().name()+ " "+hint);
        
        round = roundStatus.getRound();
        calcTimeline();
        /*
        calcTimeline();
        if(roundStatus.isStarted()) {
            round = roundStatus.getRound();
            calcTimeline();
            // TODO start timer
        } else {
            // TODO stop and reset;
            round = null;
            if(task != null) {
                task.cancel();
            }
        }*/
    }
    
    protected void calcTimeline() {
        if(round != null) {
            long sec = (round.getStartTime() + round.getDuration() - System.currentTimeMillis())/1000;
            String mm = String.valueOf(sec/60l);
            if(mm.length()<2) {
                mm = "0" + mm;
            }
            String ss = String.valueOf(sec%60);
            if(ss.length()<2) {
                ss = "0" + ss;
            }
            timeline.setText("time: "+mm+":"+ss);
        } else {
            timeline.setText("time: 00:00");
        }
    }
    
}
