// =========================================================
// FILE:
// gui/components/ModuleButton.java
// =========================================================

package cz.tvoje.quiettools.gui.components;

import cz.tvoje.quiettools.gui.render.RenderUtils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Consumer;
import java.util.function.Supplier;


public class ModuleButton {

    private int x;
    private int y;
    private final int width;
    private final int height;

    private final String text;

    private final Supplier<Boolean> getter;
    private final Consumer<Boolean> setter;

    // NOVÉ: expand getter/setter (nullable — tlačítka bez submenu je nemají)
    private final Supplier<Boolean> expandedGetter;
    private final Consumer<Boolean> expandedSetter;
    private final Runnable rightClickAction;

    private float hoverAnimation = 0f;

    // =========================================================
    // KONSTRUKTOR S EXPAND PODPOROU
    // =========================================================
    public ModuleButton(
            int x,
            int y,
            int width,
            int height,
            String text,
            Supplier<Boolean> getter,
            Consumer<Boolean> setter,
            Supplier<Boolean> expandedGetter,
            Consumer<Boolean> expandedSetter,
            Runnable rightClickAction
    ) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.getter = getter;
        this.setter = setter;
        this.expandedGetter = expandedGetter;
        this.expandedSetter = expandedSetter;
        this.rightClickAction = rightClickAction;
    }

    // =========================================================
    // PŮVODNÍ KONSTRUKTOR (bez expand) — zachován pro ostatní tlačítka
    // =========================================================
    public ModuleButton(
            int x,
            int y,
            int width,
            int height,
            String text,
            Supplier<Boolean> getter,
            Consumer<Boolean> setter
    ) {
        this(x, y, width, height, text, getter, setter, null, null, null);
    }

    public ModuleButton(
            int x,
            int y,
            int width,
            int height,
            String text,
            Supplier<Boolean> getter,
            Consumer<Boolean> setter,
            Supplier<Boolean> expandedGetter,
            Consumer<Boolean> expandedSetter
    ) {
        this(x, y, width, height, text, getter, setter, expandedGetter, expandedSetter, null);
    }

    public ModuleButton(
            int x,
            int y,
            int width,
            int height,
            String text,
            Supplier<Boolean> getter,
            Consumer<Boolean> setter,
            Runnable rightClickAction
    ) {
        this(x, y, width, height, text, getter, setter, null, null, rightClickAction);
    }

    public void render(
            DrawContext context,
            int mouseX,
            int mouseY
    ) {

        boolean hovered =
                mouseX >= x
                        && mouseX <= x + width
                        && mouseY >= y
                        && mouseY <= y + height;

        hoverAnimation += (
                (hovered ? 1f : 0f)
                        - hoverAnimation
        ) * 0.15f;

        int bg =
                hovered
                        ? 0xFF2B2B2B
                        : 0xFF222222;

        RenderUtils.drawRoundedRect(
                context,
                x,
                y,
                width,
                height,
                5,
                bg
        );

        // ACCENT BAR
        if (hoverAnimation > 0.01f) {
            RenderUtils.drawRoundedRect(
                    context,
                    x,
                    y,
                    (int) (3 * hoverAnimation),
                    height,
                    2,
                    0xFF9B5CFF
            );
        }

        context.drawText(
                MinecraftClient.getInstance().textRenderer,
                text,
                x + 10,
                y + 7,
                0xFFFFFFFF,
                false
        );

        boolean enabled = getter.get();

        // TOGGLE KOLEČKO
        RenderUtils.drawRoundedRect(
                context,
                x + width - 22,
                y + 6,
                12,
                12,
                6,
                enabled
                        ? 0xFF9B5CFF
                        : 0xFF555555
        );

        // ŠIPKA — zobrazí se jen pokud má tlačítko expand podporu
        if (expandedGetter != null) {
            boolean expanded = expandedGetter.get();
            String arrow = expanded ? "▼" : "▶";
            context.drawText(
                    MinecraftClient.getInstance().textRenderer,
                    arrow,
                    x + width - 38,
                    y + 7,
                    0xFFAAAAAA,
                    false
            );
        }
    }

    // =========================================================
    // MOUSE CLICKED — s button parametrem (0 = levý, 1 = pravý)
    // =========================================================
    public void mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        boolean hovered =
                mouseX >= x
                        && mouseX <= x + width
                        && mouseY >= y
                        && mouseY <= y + height;

        if (hovered) {
            if (button == 0) {
                // Levý klik = zapnout/vypnout
                setter.accept(!getter.get());
            } else if (button == 1) {
                if (rightClickAction != null) {
                    rightClickAction.run();
                } else if (expandedSetter != null) {
                    // Pravý klik = rozbalit/sbalit submenu
                    expandedSetter.accept(!expandedGetter.get());
                }
            }
        }
    }

    // =========================================================
    // POSITION UPDATE
    // =========================================================
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
