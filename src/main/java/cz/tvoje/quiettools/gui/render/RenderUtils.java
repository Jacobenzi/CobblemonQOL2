package cz.tvoje.quiettools.gui.render;

import net.minecraft.client.gui.DrawContext;

public class RenderUtils {

    public static void drawRoundedRect(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int radius,
            int color
    ) {

        // ZATÍM fake rounded rect
        // později můžeme udělat shader rounded rect

        context.fill(
                x,
                y,
                x + width,
                y + height,
                color
        );
    }
}