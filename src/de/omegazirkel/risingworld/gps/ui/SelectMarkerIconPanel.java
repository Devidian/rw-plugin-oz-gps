package de.omegazirkel.risingworld.gps.ui;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.gps.PluginGUI;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.UIScrollView;
import net.risingworld.api.ui.UIScrollView.ScrollViewMode;
import net.risingworld.api.ui.style.DisplayStyle;
import net.risingworld.api.ui.style.FlexDirection;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.Justify;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.ui.style.Unit;
import net.risingworld.api.ui.style.Wrap;

public class SelectMarkerIconPanel extends OZUIElement {

    private static final float GOLD_R = 0.95f;
    private static final float GOLD_G = 0.75f;
    private static final float GOLD_B = 0.25f;

    private static I18n t() {
        return I18n.getInstance(GPS.name);
    }

    private OZUIElement headerSection;
    private UIScrollView iconGridSec;
    private Player uiPlayer;
    private String selectedKey = null;

    public SelectMarkerIconPanel(Player uiPlayer) {
        this(uiPlayer, null);
    }

    public SelectMarkerIconPanel(Player uiPlayer, String selectedKey) {
        super();
        this.setSize(35, 70, true);
        this.setPivot(Pivot.MiddleCenter);
        this.setPosition(30, 50, true);
        this.setBackgroundColor(0, 0, 0, 0.86f);
        this.setBorderColor(GOLD_R, GOLD_G, GOLD_B, 0.6f);
        this.setBorder(1);
        this.setBorderEdgeRadius(6, false);
        this.style.paddingBottom.set(5);
        this.style.paddingTop.set(5);
        this.style.paddingLeft.set(5);
        this.style.paddingRight.set(5);
        this.uiPlayer = uiPlayer;
        this.selectedKey = selectedKey;

        this.setupHeaderSection();
        this.setupIconGridSection();
    }

    private void setupHeaderSection() {
        headerSection = new OZUIElement();
        headerSection.style.width.set(100, Unit.Percent);
        headerSection.style.height.set(10, Unit.Percent);
        this.addChild(headerSection);

        UILabel title = new UILabel(t().get("TC_LABEL_MARKER_ICON", uiPlayer));
        title.setSize(100, 10, true);
        title.setFontSize(17);
        title.setTextAlign(TextAnchor.MiddleCenter);
        title.setFont(Font.DefaultBold);
        title.setPivot(Pivot.UpperLeft);
        title.setPosition(5, 5, true);
        headerSection.addChild(title);
    }

    private void setupIconGridSection() {
        if (iconGridSec == null) {

            iconGridSec = new UIScrollView(ScrollViewMode.Vertical);
            iconGridSec.style.width.set(100, Unit.Percent);
            iconGridSec.style.height.set(90, Unit.Percent);
            // iconGridSec.setBackgroundColor(0x44228899);
            this.addChild(iconGridSec);
        }
        iconGridSec.removeAllChilds();

        // Create icon grid
        OZUIElement iconGrid = new OZUIElement();
        // iconGrid.setBackgroundColor(0x44880099);
        iconGrid.style.width.set(100, Unit.Percent);
        iconGrid.style.height.set(100, Unit.Percent);
        iconGrid.style.display.set(DisplayStyle.Flex);
        iconGrid.style.flexDirection.set(FlexDirection.Row);
        iconGrid.style.flexWrap.set(Wrap.Wrap);
        iconGrid.style.justifyContent.set(Justify.FlexStart);

        for (String key : PluginGUI.markerKeys) {
            MarkerIconButton iconBtn = new MarkerIconButton(uiPlayer, key);

            if (key.equals(selectedKey)) {
                iconBtn.setSelected(true);
            }

            iconBtn.setClickAction(event -> {
                if (key.equals(this.selectedKey))
                    return;
                this.selectedKey = key;
                setupIconGridSection();
            });
            iconGrid.addChild(iconBtn);
        }

        iconGridSec.addChild(iconGrid);
    }

    public String getSelectedKey() {
        return selectedKey;
    }

}
