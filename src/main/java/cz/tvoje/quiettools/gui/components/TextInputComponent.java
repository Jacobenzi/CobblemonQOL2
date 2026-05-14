package cz.tvoje.quiettools.gui.components;

import cz.tvoje.quiettools.gui.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Consumer;

public class TextInputComponent {

    private int x;
    private int y;
    private final int width;
    private final int height;

    private final String label;
    private String value = "";
    private String placeholder = "";

    private final Consumer<String> setter;

    private boolean focused = false;
    private float focusAnimation = 0f;
    private int cursorPos = 0;

    private long lastBlink = 0;
    private boolean cursorVisible = true;

    public TextInputComponent(
            int x,
            int y,
            int width,
            int height,
            String label,
            String placeholder,
            Consumer<String> setter
    ) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.label = label;
        this.placeholder = placeholder;
        this.setter = setter;
        this.cursorPos = 0;
    }

    public void render(
            DrawContext context,
            int mouseX,
            int mouseY
    ) {
        boolean hovered = isHovering(mouseX, mouseY);

        focusAnimation += ((focused ? 1f : 0f) - focusAnimation) * 0.15f;

        int bgColor = focused ? 0xFF2B2B2B : 0xFF222222;
        int borderColor = focused ? 0xFF9B5CFF : 0xFF444444;

        RenderUtils.drawRoundedRect(context, x, y, width, height, 5, bgColor);

        // Border
        if (focusAnimation > 0.01f) {
            int alpha = (int) (focusAnimation * 255);
            int borderWithAlpha = (alpha << 24) | (0x9B5CFF & 0xFFFFFF);
            RenderUtils.drawRoundedRect(context, x - 1, y - 1, width + 2, height + 2, 5, borderWithAlpha);
        }

        // Blink cursor
        long now = System.currentTimeMillis();
        if (now - lastBlink > 500) {
            cursorVisible = !cursorVisible;
            lastBlink = now;
        }

        // Text
        String displayText = value.isEmpty() && !focused ? placeholder : value;
        int textColor = value.isEmpty() && !focused ? 0xFF888888 : 0xFFFFFFFF;

        context.drawText(
                MinecraftClient.getInstance().textRenderer,
                displayText,
                x + 5,
                y + (height - 8) / 2,
                textColor,
                false
        );

        // Cursor
        if (focused && cursorVisible) {
            int cursorX = x + 5 + MinecraftClient.getInstance().textRenderer.getWidth(value.substring(0, Math.min(cursorPos, value.length())));
            context.fill(cursorX, y + 2, cursorX + 1, y + height - 2, 0xFFFFFFFF);
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        boolean hovered = isHovering((int)mouseX, (int)mouseY);  // ← PŘIDEJ (int)

        if (hovered && button == 0) {
            focused = true;
            cursorPos = value.length();
        } else if (!hovered) {
            focused = false;
        }
    }

    public boolean charTyped(char chr, int modifiers) {
        if (!focused) return false;

        if (chr >= 32 && chr <= 126) { // Printable ASCII
            if (value.length() < 30) { // Max 30 znaků
                value = value.substring(0, cursorPos) + chr + value.substring(cursorPos);
                cursorPos++;
                setter.accept(value);
                return true;
            }
        }

        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused) return false;

        if (keyCode == 259) { // BACKSPACE
            if (cursorPos > 0) {
                value = value.substring(0, cursorPos - 1) + value.substring(cursorPos);
                cursorPos--;
                setter.accept(value);
                return true;
            }
        } else if (keyCode == 261) { // DELETE
            if (cursorPos < value.length()) {
                value = value.substring(0, cursorPos) + value.substring(cursorPos + 1);
                setter.accept(value);
                return true;
            }
        } else if (keyCode == 263) { // LEFT
            if (cursorPos > 0) cursorPos--;
            return true;
        } else if (keyCode == 262) { // RIGHT
            if (cursorPos < value.length()) cursorPos++;
            return true;
        }

        return false;
    }

    private boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String newValue) {
        this.value = newValue;
        this.cursorPos = newValue.length();
    }
}