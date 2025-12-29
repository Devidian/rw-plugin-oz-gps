package de.omegazirkel.risingworld.gps.ui;

import de.omegazirkel.risingworld.tools.ui.AssetManager;
import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import net.risingworld.api.ui.style.Align;
import net.risingworld.api.ui.style.DisplayStyle;
import net.risingworld.api.ui.style.Justify;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.ScaleMode;
import net.risingworld.api.ui.style.Unit;

public class MarkerIconButton extends OZUIElement {

    public MarkerIconButton(String iconKey) {
        super();
        this.setClickable(true);
        this.setBorder(1);
        this.setHoverBackgroundColor(0xaaaaaa50);
        this.setBackgroundColor(0, 0, 0, 0.85f);
        this.setBorderColor(1, 1, 1, 0.4f);
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
        this.style.backgroundImage.set(AssetManager.getIcon(iconKey));
        this.style.backgroundImageScaleMode.set(ScaleMode.ScaleToFit);
    }

    public void setSelected(Boolean selected) {
        if (selected) {
            this.setBackgroundColor(0xaaaa00dd);
        } else {
            this.setBackgroundColor(0, 0, 0, 0.85f);
        }
    }

}
