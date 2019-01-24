package bymihaj.client;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

public class RegionHeader extends DockPanel {
    
    public RegionHeader(String name, String help, String width) {
        Label label = new Label(name);
        label.addStyleName("region-title");
        
        Image helpIcon = new Image("image/help.png");
        helpIcon.addStyleName("region-help");
        helpIcon.addClickHandler(e -> {
            PopupPanel pp = new PopupPanel(true);
            Label helpLabel = new Label(help);
            helpLabel.addStyleName("region-help-text");
            pp.setWidget(helpLabel);
            pp.setPopupPosition(label.getAbsoluteLeft(), label.getAbsoluteTop());
            pp.show();
        });
        
        add(label, DockPanel.WEST);
        add(helpIcon, DockPanel.EAST);
        setCellHorizontalAlignment(helpIcon, HasHorizontalAlignment.ALIGN_RIGHT);
        setWidth(width);
    }

}
