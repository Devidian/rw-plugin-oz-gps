package de.omegazirkel.risingworld.gps.ui;

import de.omegazirkel.risingworld.tools.ui.AssetManager;
import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.style.Align;
import net.risingworld.api.ui.style.DisplayStyle;
import net.risingworld.api.ui.style.Justify;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.ScaleMode;
import net.risingworld.api.ui.style.Unit;

public class MarkerIconButton extends OZUIElement {

    private static final float GOLD_R = 0.95f;
    private static final float GOLD_G = 0.75f;
    private static final float GOLD_B = 0.25f;

    public MarkerIconButton(Player player, String iconKey) {
        super();
        this.setClickable(true);
        this.setBorder(1);
        this.setHoverBackgroundColor(0x611F1AF2);
        this.setBackgroundColor(0.14f, 0.13f, 0.12f, 0.92f);
        this.setBorderColor(GOLD_R, GOLD_G, GOLD_B, 0.26f);
        this.setBorderEdgeRadius(6, false);
        this.setPivot(Pivot.UpperLeft);
        this.style.display.set(DisplayStyle.Flex);
        this.style.justifyContent.set(Justify.Center);
        this.style.alignItems.set(Align.Center);
        this.style.width.set(80, Unit.Pixel);
        this.style.height.set(80, Unit.Pixel);
        int margin = 2;
        this.style.marginBottom.set(margin, Unit.Pixel);
        this.style.marginTop.set(margin, Unit.Pixel);
        this.style.marginLeft.set(margin, Unit.Pixel);
        this.style.marginRight.set(margin, Unit.Pixel);
        this.style.backgroundImage.set(AssetManager.getIcon(player, iconKey));
        this.style.backgroundImageScaleMode.set(ScaleMode.ScaleToFit);
    }

    public void setSelected(Boolean selected) {
        if (selected) {
            this.setBackgroundColor(0.19f, 0.10f, 0.03f, 0.92f);
            this.setBorderColor(1.0f, 0.48f, 0.12f, 0.86f);
        } else {
            this.setBackgroundColor(0.14f, 0.13f, 0.12f, 0.92f);
            this.setBorderColor(GOLD_R, GOLD_G, GOLD_B, 0.26f);
        }
    }

}
