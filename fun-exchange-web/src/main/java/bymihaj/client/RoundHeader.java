package bymihaj.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import bymihaj.AssetsResponse;
import bymihaj.Round;
import bymihaj.RoundStatus;
import bymihaj.RoundStatusRequest;
import bymihaj.Symbol;

public class RoundHeader extends DockPanel {
    
    protected Label roundId;
    protected Label timeline;
    protected Label teamLabel;
    protected Label teamHint;
    protected Round round;
    
    protected Label mon;
    protected Label stock;
    
    public RoundHeader(Connection connection) {
        setSpacing(20);
        setSize("1440px", "65px");
        setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        
        roundId = new Label();
        roundId.addStyleName("trade-round-id");
        add(roundId, DockPanel.WEST);
        
        Label timeText = new Label("time left");
        timeText.addStyleName("trade-minor-text");
        timeline = new Label();
        timeline.addStyleName("trade-timeout");
        HorizontalPanel timeHolder = new HorizontalPanel();
        timeHolder.setSpacing(10);
        timeHolder.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        timeHolder.add(timeText);
        timeHolder.add(timeline);
        add(timeHolder, DockPanel.WEST);
        
        
        Label teamText = new Label("team");
        teamText.addStyleName("trade-minor-text");
        teamLabel = new Label();
        teamLabel.addStyleName("trade-round-id");
        teamHint = new Label();
        teamHint.addStyleName("trade-minor-text");
        HorizontalPanel teamHolder = new HorizontalPanel();
        teamHolder.setSpacing(5);
        teamHolder.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        teamHolder.add(teamText);
        teamHolder.add(teamLabel);
        teamHolder.add(teamHint);
        add(teamHolder, DockPanel.WEST);
        
        
        Label monLabel = new Label("MON");
        monLabel.addStyleName("trade-minor-text");
        mon = new Label("0");
        mon.addStyleName("trade-round-id");
        mon.setWidth("90px");
        Label stockLabel = new Label("STK");
        stockLabel.addStyleName("trade-minor-text");
        stock = new Label("0");
        stock.setWidth("90px");
        stock.addStyleName("trade-round-id");
        HorizontalPanel assetHolder = new HorizontalPanel();
        assetHolder.setSpacing(5);
        assetHolder.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        assetHolder.add(monLabel);
        assetHolder.add(mon);
        assetHolder.add(stockLabel);
        assetHolder.add(stock);
        add(assetHolder, DockPanel.WEST);
        
        
        
        
        Button lobbyBtn = new Button("Lobby");
        lobbyBtn.addStyleName("lobby-button");
        lobbyBtn.addClickHandler(e -> {
            WebClient.switchPane(new LobbyPane(connection), true);
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
        
        teamLabel.setText(roundStatus.getTeam().name());
        teamHint.setText(hint);
        
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
            timeline.setText(mm + ":" + ss);
        } else {
            timeline.setText("00:00");
        }
    }
    
    public void onAssets(AssetsResponse ar) {
        mon.setText(ar.getProperties().get(Symbol.MON).getAmount().toPlainString());
        stock.setText(ar.getProperties().get(Symbol.STK).getAmount().toPlainString());
    }
    
}
