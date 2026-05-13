// =========================================================
// FILE:
// gui/components/SliderComponent.java
// =========================================================

package cz.tvoje.quiettools.gui.components;

import cz.tvoje.quiettools.gui.render.RenderUtils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Consumer;

public class SliderComponent {

    // Odebráno 'final'
    private int x;
    private int y;

    private final int width;
    private final int height;

    private final String text;

    private final double min;
    private final double max;

    private double value;

    private final Consumer<Double> setter;

    private boolean dragging;

    public SliderComponent(
            int x,
            int y,
            int width,
            int height,
            String text,
            double min,
            double max,
            double value,
            Consumer<Double> setter
    ) {

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.text = text;

        this.min = min;
        this.max = max;

        this.value = value;

        this.setter = setter;
    }

    public void render(
            DrawContext context,
            int mouseX,
            int mouseY
    ) {

        if (dragging) {

            double percent =
                    (mouseX - x) / (double) width;

            percent = Math.max(0, Math.min(1, percent));

            value =
                    min + (max - min) * percent;

            setter.accept(value);
        }

        RenderUtils.drawRoundedRect(
                context,
                x,
                y,
                width,
                height,
                4,
                0xFF222222
        );

        int fill =
                (int) (
                        ((value - min) / (max - min))
                                * width
                );

        RenderUtils.drawRoundedRect(
                context,
                x,
                y,
                fill,
                height,
                4,
                0xFF9B5CFF
        );

        context.drawCenteredTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                text + ": " + (int) value,
                x + width / 2,
                y + 5,
                0xFFFFFFFF
        );
    }

    public void mouseClicked(
            double mouseX,
            double mouseY
    ) {

        dragging =
                mouseX >= x
                        && mouseX <= x + width
                        && mouseY >= y
                        && mouseY <= y + height;
    }

    public void mouseReleased() {
        dragging = false;
    }

    // =========================================================
    // DYNAMICKÁ ZMĚNA POZICE
    // =========================================================
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}