package cz.tvoje.quiettools.automation;

import net.minecraft.client.gui.widget.ClickableWidget;

public class MoveData {

    public ClickableWidget widget;

    public String name;

    public MoveData(
            ClickableWidget widget,
            String name
    ) {

        this.widget = widget;

        this.name = name;
    }
}