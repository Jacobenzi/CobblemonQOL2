package cz.tvoje.quiettools.gui.components;

import cz.tvoje.quiettools.gui.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class ColorPickerComponent {

    private int x;
    private int y;
    private final int width;
    private final int height;

    private final String label;
    private final IntSupplier rGetter;
    private final IntSupplier gGetter;
    private final IntSupplier bGetter;
    private final IntConsumer rSetter;
    private final IntConsumer gSetter;
    private final IntConsumer bSetter;

    private boolean draggingR;
    private boolean draggingG;
    private boolean draggingB;

    public ColorPickerComponent(
            String label,
            IntSupplier rGetter,
            IntSupplier gGetter,
            IntSupplier bGetter,
            IntConsumer rSetter,
            IntConsumer gSetter,
            IntConsumer bSetter
    ) {
        this.width = 170;
        this.height = 110;
        this.label = label;
        this.rGetter = rGetter;
        this.gGetter = gGetter;
        this.bGetter = bGetter;
        this.rSetter = rSetter;
        this.gSetter = gSetter;
        this.bSetter = bSetter;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        RenderUtils.drawRoundedRect(context, x, y, width, height, 6, 0xF0151515);
        RenderUtils.drawRoundedRect(context, x + 1, y + 1, width - 2, 18, 4, 0xFF262626);

        context.drawText(MinecraftClient.getInstance().textRenderer, label, x + 6, y + 6, 0xFFFFFFFF, false);

        int sliderY = y + 28;
        drawSlider(context, "R", rGetter.getAsInt(), sliderY, 0xFFFF5555);
        sliderY += 22;
        drawSlider(context, "G", gGetter.getAsInt(), sliderY, 0xFF55FF55);
        sliderY += 22;
        drawSlider(context, "B", bGetter.getAsInt(), sliderY, 0xFF5555FF);

        int preview = rgb(rGetter.getAsInt(), gGetter.getAsInt(), bGetter.getAsInt());
        RenderUtils.drawRoundedRect(context, x + width - 32, y + 6, 24, 12, 3, preview);
    }

    private void drawSlider(DrawContext context, String channel, int value, int sliderY, int fillColor) {
        int sliderX = x + 26;
        int sliderWidth = width - 36;
        int sliderHeight = 8;

        context.drawText(MinecraftClient.getInstance().textRenderer, channel + ": " + value, x + 6, sliderY - 1, 0xFFCCCCCC, false);
        RenderUtils.drawRoundedRect(context, sliderX, sliderY + 8, sliderWidth, sliderHeight, 2, 0xFF333333);
        int fillWidth = (int) ((clamp(value) / 255f) * sliderWidth);
        RenderUtils.drawRoundedRect(context, sliderX, sliderY + 8, fillWidth, sliderHeight, 2, fillColor);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        draggingR = isOverSlider(mouseX, mouseY, 0);
        draggingG = isOverSlider(mouseX, mouseY, 1);
        draggingB = isOverSlider(mouseX, mouseY, 2);
        applyFromMouseX(mouseX);
        return draggingR || draggingG || draggingB;
    }

    public boolean mouseDragged(double mouseX, double mouseY) {
        if (!draggingR && !draggingG && !draggingB) {
            return false;
        }
        applyFromMouseX(mouseX);
        return true;
    }

    public void mouseReleased() {
        draggingR = false;
        draggingG = false;
        draggingB = false;
    }

    public boolean isDragging() {
        return draggingR || draggingG || draggingB;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void applyFromMouseX(double mouseX) {
        int sliderX = x + 26;
        int sliderWidth = width - 36;
        float percent = (float) ((mouseX - sliderX) / sliderWidth);
        int value = clamp(Math.round(percent * 255f));

        if (draggingR) {
            rSetter.accept(value);
        }
        if (draggingG) {
            gSetter.accept(value);
        }
        if (draggingB) {
            bSetter.accept(value);
        }
    }

    private boolean isOverSlider(double mouseX, double mouseY, int sliderIndex) {
        int sliderY = y + 36 + sliderIndex * 22;
        int sliderX = x + 26;
        int sliderWidth = width - 36;
        int sliderHeight = 8;
        return mouseX >= sliderX && mouseX <= sliderX + sliderWidth
                && mouseY >= sliderY && mouseY <= sliderY + sliderHeight;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private int rgb(int r, int g, int b) {
        return (0xFF << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }
}
