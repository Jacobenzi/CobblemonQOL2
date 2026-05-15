package cz.tvoje.quiettools.gui;

import cz.tvoje.quiettools.Harvester;
import cz.tvoje.quiettools.ModSettings;
import cz.tvoje.quiettools.QuietTools;
import cz.tvoje.quiettools.XrayModule;

import cz.tvoje.quiettools.gui.components.ColorPickerComponent;
import cz.tvoje.quiettools.gui.components.ModuleButton;
import cz.tvoje.quiettools.gui.components.SliderComponent;
import cz.tvoje.quiettools.gui.render.RenderUtils;
import cz.tvoje.quiettools.gui.components.TextInputComponent;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class ClickGuiScreen extends Screen {

    private float scrollOffset = 0f;
    private float targetScrollOffset = 0f;
    private final int componentWidth = 220;
    private float maxScroll = 0f;


    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {
        if (!isHovering(
                panelX + sidebarWidth,
                panelY + 30,
                panelWidth - sidebarWidth,
                panelHeight - 30,
                mouseX,
                mouseY
        )) {
            return false;
        }

        targetScrollOffset += (float) (-verticalAmount * 20);

        return super.mouseScrolled(
                mouseX,
                mouseY,
                horizontalAmount,
                verticalAmount
        );
    }

    // =========================================================
    // CATEGORY
    // =========================================================

    private Category selectedCategory = Category.ESP;

    // =========================================================
    // PANEL
    // =========================================================

    private int panelX = 300;
    private int panelY = 180;

    private int panelWidth = ModSettings.guiWidth;
    private int panelHeight = ModSettings.guiHeight;

    private final int sidebarWidth = 70;

    // =========================================================
    // DRAGGING
    // =========================================================

    private boolean dragging = false;

    private int dragOffsetX;
    private int dragOffsetY;

    // =========================================================
    // ANIMATION
    // =========================================================

    private float openAnimation = 0f;

    // =========================================================
    // COMPONENTS
    // =========================================================

    private ModuleButton blisseyButton;
    private ModuleButton shinyButton;
    private ModuleButton ivButton;
    private ModuleButton autoJumpButton;
    private SliderComponent espRadiusSlider;

    // =========================================================
    // CUSTOM POKEMON ESP COMPONENTS
    // =========================================================
    private ModuleButton customPokemonButton;
    private TextInputComponent customPokemonInput;
    private SliderComponent customPokemonRadiusSlider;
    private SliderComponent customPokemonRSlider;
    private SliderComponent customPokemonGSlider;
    private SliderComponent customPokemonBSlider;

    private ModuleButton apricornButton;
    private ModuleButton berryButton;
    private ModuleButton vivichokeButton;

    private ModuleButton grepaButton;
    private ModuleButton pomegButton;
    private ModuleButton tamatoButton;
    private ModuleButton hondewButton;
    private ModuleButton qualotButton;
    private ModuleButton kelpsyButton;

    private SliderComponent radiusSlider;

    private SliderComponent blisseyRSlider;
    private SliderComponent blisseyGSlider;
    private SliderComponent blisseyBSlider;

    private SliderComponent shinyRSlider;
    private SliderComponent shinyGSlider;
    private SliderComponent shinyBSlider;

    private SliderComponent ivRSlider;
    private SliderComponent ivGSlider;
    private SliderComponent ivBSlider;

    private SliderComponent berryRSlider;
    private SliderComponent berryGSlider;
    private SliderComponent berryBSlider;

    private SliderComponent guiWidthSlider;
    private SliderComponent guiHeightSlider;

    // =========================================================
    // X-RAY COMPONENTS
    // =========================================================

    private ModuleButton xrayButton;
    private SliderComponent xrayRadiusSlider;
    private SliderComponent xrayOpacitySlider;
    private ModuleButton fullbrightButton;
    private ModuleButton xrayTracerButton;
    private ModuleButton autoMineButton;
    private ModuleButton legitModeButton;

    // Vanilla ores
    private ModuleButton xrayDiamondButton;
    private ModuleButton xrayEmeraldButton;
    private ModuleButton xrayAncientDebrisButton;
    private ModuleButton xrayGoldButton;
    private ModuleButton xrayIronButton;
    private ModuleButton xrayCoalButton;
    private ModuleButton xrayLapisButton;
    private ModuleButton xrayRedstoneButton;
    private ModuleButton xrayCopperButton;
    private ModuleButton xrayQuartzButton;

    // Mythic Metals ores (včetně Kyber)
    private ModuleButton xrayKyberButton;
    private ModuleButton xrayOrichalcumButton;
    private ModuleButton xrayKalimiteButton;
    private ModuleButton xrayMalachiteButton;
    private ModuleButton xrayTitaniumButton;
    private ModuleButton xrayAdamantiteButton;
    private ModuleButton xrayMithrilButton;
    private ModuleButton xrayPlatinumButton;
    private ModuleButton xraySilverButton;
    private ModuleButton xrayBanglumButton;
    private ModuleButton xrayRuniteButton;
    private ModuleButton xrayCarmotButton;
    private ModuleButton keystoneOreButton;

    private ColorPickerComponent currentColorPicker;

    public ClickGuiScreen() {
        super(Text.literal("BetterThirdPerson"));
    }

    @Override
    public void renderBackground(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {

        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;

        int y = panelY + 40;

        autoJumpButton = new ModuleButton(
                panelX + sidebarWidth + 15,
                y,
                componentWidth,
                22,
                "Auto Jump Assist",
                () -> ModSettings.autoJumpAssist,
                value -> ModSettings.autoJumpAssist = value
        );

        // =========================================================
        // ESP
        // =========================================================

        blisseyButton = new ModuleButton(
                panelX + sidebarWidth + 15,
                y,
                componentWidth,
                22,
                "Blissey ESP",
                () -> ModSettings.blisseyEspEnabled,
                value -> ModSettings.blisseyEspEnabled = value
        );

        y += 28;

        shinyButton = new ModuleButton(
                panelX + sidebarWidth + 15,
                y,
                componentWidth,
                22,
                "Shiny ESP",
                () -> ModSettings.shinyLegendaryEspEnabled,
                value -> ModSettings.shinyLegendaryEspEnabled = value
        );

        y += 28;

        ivButton = new ModuleButton(
                panelX + sidebarWidth + 15,
                y,
                componentWidth,
                22,
                "IV Scanner",
                () -> ModSettings.ivScannerEnabled,
                value -> ModSettings.ivScannerEnabled = value
        );

        y += 40;

        espRadiusSlider = new SliderComponent(
                panelX + sidebarWidth + 15,
                y,
                componentWidth,
                18,
                "ESP Radius",
                16,
                256,
                ModSettings.espRadius,
                value -> ModSettings.espRadius = value.intValue()
        );

        // =========================================================
        // FARMING
        // =========================================================

        y = panelY + 40;

        apricornButton = new ModuleButton(
                panelX + sidebarWidth + 15,
                y,
                componentWidth,
                22,
                "Harvest Apricorns",
                () -> ModSettings.apricornEnabled,
                value -> ModSettings.apricornEnabled = value
        );

        y += 28;

        berryButton = new ModuleButton(
                panelX + sidebarWidth + 15,
                y,
                componentWidth,
                22,
                "Harvest Berries",
                () -> ModSettings.berryEnabled,
                value -> ModSettings.berryEnabled = value,
                () -> ModSettings.berryExpanded,
                value -> ModSettings.berryExpanded = value
        );

        grepaButton  = new ModuleButton(0, 0, componentWidth - 20, 20, " > Grepa",  () -> ModSettings.harvestGrepa,  v -> ModSettings.harvestGrepa  = v);
        pomegButton  = new ModuleButton(0, 0, componentWidth - 20, 20, " > Pomeg",  () -> ModSettings.harvestPomeg,  v -> ModSettings.harvestPomeg  = v);
        tamatoButton = new ModuleButton(0, 0, componentWidth - 20, 20, " > Tamato", () -> ModSettings.harvestTamato, v -> ModSettings.harvestTamato = v);
        hondewButton = new ModuleButton(0, 0, componentWidth - 20, 20, " > Hondew", () -> ModSettings.harvestHondew, v -> ModSettings.harvestHondew = v);
        qualotButton = new ModuleButton(0, 0, componentWidth - 20, 20, " > Qualot", () -> ModSettings.harvestQualot, v -> ModSettings.harvestQualot = v);
        kelpsyButton = new ModuleButton(0, 0, componentWidth - 20, 20, " > Kelpsy", () -> ModSettings.harvestKelpsy, v -> ModSettings.harvestKelpsy = v);

        y += 28;

        vivichokeButton = new ModuleButton(
                panelX + sidebarWidth + 15,
                y,
                componentWidth,
                22,
                "Harvest Vivichoke",
                () -> ModSettings.vivichokeEnabled,
                value -> ModSettings.vivichokeEnabled = value
        );

        y += 40;

        radiusSlider = new SliderComponent(
                panelX + sidebarWidth + 15,
                y,
                componentWidth,
                18,
                "Radius",
                1,
                20,
                Harvester.radius,
                value -> Harvester.radius = value.intValue()
        );

        // =========================================================
        // MISC RGB SLIDERS
        // =========================================================

        y = panelY + 40;

        blisseyRSlider = new SliderComponent(
                panelX + sidebarWidth + 15, y, componentWidth, 18, "Blissey R",
                0, 255, ModSettings.blisseyR, value -> ModSettings.blisseyR = value.intValue()
        );

        y += 24;

        blisseyGSlider = new SliderComponent(
                panelX + sidebarWidth + 15, y, componentWidth, 18, "Blissey G",
                0, 255, ModSettings.blisseyG, value -> ModSettings.blisseyG = value.intValue()
        );

        y += 24;

        blisseyBSlider = new SliderComponent(
                panelX + sidebarWidth + 15, y, componentWidth, 18, "Blissey B",
                0, 255, ModSettings.blisseyB, value -> ModSettings.blisseyB = value.intValue()
        );

        y += 40;

        shinyRSlider = new SliderComponent(
                panelX + sidebarWidth + 15, y, componentWidth, 18, "Shiny R",
                0, 255, ModSettings.shinyR, value -> ModSettings.shinyR = value.intValue()
        );

        y += 24;

        shinyGSlider = new SliderComponent(
                panelX + sidebarWidth + 15, y, componentWidth, 18, "Shiny G",
                0, 255, ModSettings.shinyG, value -> ModSettings.shinyG = value.intValue()
        );

        y += 24;

        shinyBSlider = new SliderComponent(
                panelX + sidebarWidth + 15, y, componentWidth, 18, "Shiny B",
                0, 255, ModSettings.shinyB, value -> ModSettings.shinyB = value.intValue()
        );

        y += 40;

        ivRSlider = new SliderComponent(
                panelX + sidebarWidth + 15, y, componentWidth, 18, "IV R",
                0, 255, ModSettings.ivR, value -> ModSettings.ivR = value.intValue()
        );

        y += 24;

        ivGSlider = new SliderComponent(
                panelX + sidebarWidth + 15, y, componentWidth, 18, "IV G",
                0, 255, ModSettings.ivG, value -> ModSettings.ivG = value.intValue()
        );

        y += 24;

        ivBSlider = new SliderComponent(
                panelX + sidebarWidth + 15, y, componentWidth, 18, "IV B",
                0, 255, ModSettings.ivB, value -> ModSettings.ivB = value.intValue()
        );

        y += 40;

        berryRSlider = new SliderComponent(
                panelX + sidebarWidth + 15, y, componentWidth, 18, "Berry R",
                0, 255, ModSettings.berryR, value -> ModSettings.berryR = value.intValue()
        );

        y += 24;

        berryGSlider = new SliderComponent(
                panelX + sidebarWidth + 15, y, componentWidth, 18, "Berry G",
                0, 255, ModSettings.berryG, value -> ModSettings.berryG = value.intValue()
        );

        y += 24;

        berryBSlider = new SliderComponent(
                panelX + sidebarWidth + 15, y, componentWidth, 18, "Berry B",
                0, 255, ModSettings.berryB, value -> ModSettings.berryB = value.intValue()
        );

        y += 40;

        guiWidthSlider = new SliderComponent(
                panelX + sidebarWidth + 15, y, componentWidth, 18, "GUI Width",
                320, 700, panelWidth,
                value -> {
                    panelWidth = value.intValue();
                    ModSettings.guiWidth = panelWidth;
                }
        );

        y += 24;

        guiHeightSlider = new SliderComponent(
                panelX + sidebarWidth + 15, y, componentWidth, 18, "GUI Height",
                220, 600, panelHeight,
                value -> {
                    panelHeight = value.intValue();
                    ModSettings.guiHeight = panelHeight;
                }
        );

        // =========================================================
        // X-RAY COMPONENTS
        // =========================================================

        y = panelY + 40;

        xrayButton = new ModuleButton(
                panelX + sidebarWidth + 15,
                y,
                componentWidth,
                22,
                "X-Ray",
                () -> ModSettings.xrayEnabled,
                value -> ModSettings.xrayEnabled = value
        );

        y += 28;

        xrayTracerButton = new ModuleButton(
                panelX + sidebarWidth + 15,
                y, // Pozici si to vezme automaticky podle aktuálního 'y'
                componentWidth,
                22,
                "X-Ray Tracer",
                () -> ModSettings.xrayTracerEnabled,
                value -> ModSettings.xrayTracerEnabled = value
        );

        y += 28;

        autoMineButton = new ModuleButton(
                panelX + sidebarWidth + 15,
                y, // Pozice se nastaví dynamicky při vykreslování
                componentWidth,
                22,
                "Auto-Mine Bot",
                () -> ModSettings.autoMineBot,
                value -> ModSettings.autoMineBot = value
        );

        y += 28;

        legitModeButton = new ModuleButton(
                panelX + sidebarWidth + 15,
                y,
                componentWidth,
                22,
                "Legit Movement",
                () -> ModSettings.legitMode,

                // TADY JE ZMĚNA: Tohle se provede JEN JEDNOU při kliknutí
                value -> {
                    ModSettings.legitMode = value; // Uloží hodnotu do tvého nastavení

                    // Okamžitě předáme nastavení Baritonu
                    var settings = baritone.api.BaritoneAPI.getSettings();
                    settings.antiCheatCompatibility.value = value;
                    settings.blockFreeLook.value = value;
                    settings.allowParkour.value = value;
                    settings.smoothLook.value = value;

                    // Přidáme ještě tuhle lahůdku - nutí to bota točit hlavou mnohem organičtěji
                    settings.randomLooking.value = value ? 0.01 : 0.0;
                }
        );

        y += 28;

        xrayRadiusSlider = new SliderComponent(
                panelX + sidebarWidth + 15,
                y,
                componentWidth,
                18,
                "Radius",
                16,
                256,
                ModSettings.xrayRadius,
                value -> {
                    ModSettings.xrayRadius = value.intValue();
                }
        );

        y += 40;

        xrayOpacitySlider = new SliderComponent(
                panelX + sidebarWidth + 15,
                y,
                componentWidth,
                18,
                "Opacity",
                50,
                255,
                ModSettings.xrayOpacity,
                value -> ModSettings.xrayOpacity = value.intValue()
        );

        y += 40;

        fullbrightButton = new ModuleButton(
                panelX + sidebarWidth + 15,
                y,
                componentWidth,
                22,
                "Fullbright",
                () -> ModSettings.fullbrightEnabled,
                value -> ModSettings.fullbrightEnabled = value
        );

        // Vanilla ores - ALL
        y = panelY + 40;
        xrayDiamondButton = new ModuleButton(0, 0, componentWidth, 22, "Diamond",
                () -> ModSettings.xrayShowDiamond,
                v -> {
                    ModSettings.xrayShowDiamond = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Diamond",
                        () -> ModSettings.xrayDiamondR, () -> ModSettings.xrayDiamondG, () -> ModSettings.xrayDiamondB,
                        value -> ModSettings.xrayDiamondR = value,
                        value -> ModSettings.xrayDiamondG = value,
                        value -> ModSettings.xrayDiamondB = value
                ));

        xrayEmeraldButton = new ModuleButton(0, 0, componentWidth, 22, "Emerald",
                () -> ModSettings.xrayShowEmerald,
                v -> {
                    ModSettings.xrayShowEmerald = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Emerald",
                        () -> ModSettings.xrayEmeraldR, () -> ModSettings.xrayEmeraldG, () -> ModSettings.xrayEmeraldB,
                        value -> ModSettings.xrayEmeraldR = value,
                        value -> ModSettings.xrayEmeraldG = value,
                        value -> ModSettings.xrayEmeraldB = value
                ));

        xrayAncientDebrisButton = new ModuleButton(0, 0, componentWidth, 22, "Ancient Debris",
                () -> ModSettings.xrayShowAncientDebris,
                v -> {
                    ModSettings.xrayShowAncientDebris = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Ancient Debris",
                        () -> ModSettings.xrayAncientDebrisR, () -> ModSettings.xrayAncientDebrisG, () -> ModSettings.xrayAncientDebrisB,
                        value -> ModSettings.xrayAncientDebrisR = value,
                        value -> ModSettings.xrayAncientDebrisG = value,
                        value -> ModSettings.xrayAncientDebrisB = value
                ));

        xrayGoldButton = new ModuleButton(0, 0, componentWidth, 22, "Gold",
                () -> ModSettings.xrayShowGold,
                v -> {
                    ModSettings.xrayShowGold = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Gold",
                        () -> ModSettings.xrayGoldR, () -> ModSettings.xrayGoldG, () -> ModSettings.xrayGoldB,
                        value -> ModSettings.xrayGoldR = value,
                        value -> ModSettings.xrayGoldG = value,
                        value -> ModSettings.xrayGoldB = value
                ));

        xrayIronButton = new ModuleButton(0, 0, componentWidth, 22, "Iron",
                () -> ModSettings.xrayShowIron,
                v -> {
                    ModSettings.xrayShowIron = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Iron",
                        () -> ModSettings.xrayIronR, () -> ModSettings.xrayIronG, () -> ModSettings.xrayIronB,
                        value -> ModSettings.xrayIronR = value,
                        value -> ModSettings.xrayIronG = value,
                        value -> ModSettings.xrayIronB = value
                ));

        xrayCoalButton = new ModuleButton(0, 0, componentWidth, 22, "Coal",
                () -> ModSettings.xrayShowCoal,
                v -> {
                    ModSettings.xrayShowCoal = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Coal",
                        () -> ModSettings.xrayCoalR, () -> ModSettings.xrayCoalG, () -> ModSettings.xrayCoalB,
                        value -> ModSettings.xrayCoalR = value,
                        value -> ModSettings.xrayCoalG = value,
                        value -> ModSettings.xrayCoalB = value
                ));

        xrayLapisButton = new ModuleButton(0, 0, componentWidth, 22, "Lapis",
                () -> ModSettings.xrayShowLapis,
                v -> {
                    ModSettings.xrayShowLapis = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Lapis",
                        () -> ModSettings.xrayLapisR, () -> ModSettings.xrayLapisG, () -> ModSettings.xrayLapisB,
                        value -> ModSettings.xrayLapisR = value,
                        value -> ModSettings.xrayLapisG = value,
                        value -> ModSettings.xrayLapisB = value
                ));

        xrayRedstoneButton = new ModuleButton(0, 0, componentWidth, 22, "Redstone",
                () -> ModSettings.xrayShowRedstone,
                v -> {
                    ModSettings.xrayShowRedstone = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Redstone",
                        () -> ModSettings.xrayRedstoneR, () -> ModSettings.xrayRedstoneG, () -> ModSettings.xrayRedstoneB,
                        value -> ModSettings.xrayRedstoneR = value,
                        value -> ModSettings.xrayRedstoneG = value,
                        value -> ModSettings.xrayRedstoneB = value
                ));

        xrayCopperButton = new ModuleButton(0, 0, componentWidth, 22, "Copper",
                () -> ModSettings.xrayShowCopper,
                v -> {
                    ModSettings.xrayShowCopper = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Copper",
                        () -> ModSettings.xrayCopperR, () -> ModSettings.xrayCopperG, () -> ModSettings.xrayCopperB,
                        value -> ModSettings.xrayCopperR = value,
                        value -> ModSettings.xrayCopperG = value,
                        value -> ModSettings.xrayCopperB = value
                ));

        xrayQuartzButton = new ModuleButton(0, 0, componentWidth, 22, "Quartz",
                () -> ModSettings.xrayShowQuartz,
                v -> {
                    ModSettings.xrayShowQuartz = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Quartz",
                        () -> ModSettings.xrayQuartzR, () -> ModSettings.xrayQuartzG, () -> ModSettings.xrayQuartzB,
                        value -> ModSettings.xrayQuartzR = value,
                        value -> ModSettings.xrayQuartzG = value,
                        value -> ModSettings.xrayQuartzB = value
                ));

        // Mythic Metals ores (včetně Kyber)

        // Mythic Metals ores (včetně Kyber)

        keystoneOreButton = new ModuleButton(0, 0, componentWidth, 22, "Keystone Ore",
                () -> ModSettings.xrayShowKeystone,
                value -> {
                    ModSettings.xrayShowKeystone = value;
                    cz.tvoje.quiettools.XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Keystone ore",
                        () -> ModSettings.xrayKeystoneR, () -> ModSettings.xrayKeystoneG, () -> ModSettings.xrayKeystoneB,
                        value -> ModSettings.xrayKeystoneR = value,
                        value -> ModSettings.xrayKeystoneG = value,
                        value -> ModSettings.xrayKeystoneB = value
                ));

        xrayKyberButton = new ModuleButton(0, 0, componentWidth, 22, "Kyber",
                () -> ModSettings.xrayShowKyber,
                v -> {
                    ModSettings.xrayShowKyber = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Kyber",
                        () -> ModSettings.xrayKyberR, () -> ModSettings.xrayKyberG, () -> ModSettings.xrayKyberB,
                        value -> ModSettings.xrayKyberR = value,
                        value -> ModSettings.xrayKyberG = value,
                        value -> ModSettings.xrayKyberB = value
                ));

        xrayOrichalcumButton = new ModuleButton(0, 0, componentWidth, 22, "Orichalcum",
                () -> ModSettings.xrayShowOrichalcum,
                v -> {
                    ModSettings.xrayShowOrichalcum = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Orichalcum",
                        () -> ModSettings.xrayOrichalcumR, () -> ModSettings.xrayOrichalcumG, () -> ModSettings.xrayOrichalcumB,
                        value -> ModSettings.xrayOrichalcumR = value,
                        value -> ModSettings.xrayOrichalcumG = value,
                        value -> ModSettings.xrayOrichalcumB = value
                ));

        xrayKalimiteButton = new ModuleButton(0, 0, componentWidth, 22, "Kalimite",
                () -> ModSettings.xrayShowKalimite,
                v -> {
                    ModSettings.xrayShowKalimite = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Kalimite",
                        () -> ModSettings.xrayKalimiteR, () -> ModSettings.xrayKalimiteG, () -> ModSettings.xrayKalimiteB,
                        value -> ModSettings.xrayKalimiteR = value,
                        value -> ModSettings.xrayKalimiteG = value,
                        value -> ModSettings.xrayKalimiteB = value
                ));

        xrayMalachiteButton = new ModuleButton(0, 0, componentWidth, 22, "Malachite",
                () -> ModSettings.xrayShowMalachite,
                v -> {
                    ModSettings.xrayShowMalachite = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Malachite",
                        () -> ModSettings.xrayMalachiteR, () -> ModSettings.xrayMalachiteG, () -> ModSettings.xrayMalachiteB,
                        value -> ModSettings.xrayMalachiteR = value,
                        value -> ModSettings.xrayMalachiteG = value,
                        value -> ModSettings.xrayMalachiteB = value
                ));

        xrayTitaniumButton = new ModuleButton(0, 0, componentWidth, 22, "Titanium",
                () -> ModSettings.xrayShowTitanium,
                v -> {
                    ModSettings.xrayShowTitanium = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Titanium",
                        () -> ModSettings.xrayTitaniumR, () -> ModSettings.xrayTitaniumG, () -> ModSettings.xrayTitaniumB,
                        value -> ModSettings.xrayTitaniumR = value,
                        value -> ModSettings.xrayTitaniumG = value,
                        value -> ModSettings.xrayTitaniumB = value
                ));

        xrayAdamantiteButton = new ModuleButton(0, 0, componentWidth, 22, "Adamantite",
                () -> ModSettings.xrayShowAdamantite,
                v -> {
                    ModSettings.xrayShowAdamantite = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Adamantite",
                        () -> ModSettings.xrayAdamantiteR, () -> ModSettings.xrayAdamantiteG, () -> ModSettings.xrayAdamantiteB,
                        value -> ModSettings.xrayAdamantiteR = value,
                        value -> ModSettings.xrayAdamantiteG = value,
                        value -> ModSettings.xrayAdamantiteB = value
                ));

        xrayMithrilButton = new ModuleButton(0, 0, componentWidth, 22, "Mithril",
                () -> ModSettings.xrayShowMithril,
                v -> {
                    ModSettings.xrayShowMithril = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Mithril",
                        () -> ModSettings.xrayMithrilR, () -> ModSettings.xrayMithrilG, () -> ModSettings.xrayMithrilB,
                        value -> ModSettings.xrayMithrilR = value,
                        value -> ModSettings.xrayMithrilG = value,
                        value -> ModSettings.xrayMithrilB = value
                ));

        xrayPlatinumButton = new ModuleButton(0, 0, componentWidth, 22, "Platinum",
                () -> ModSettings.xrayShowPlatinum,
                v -> {
                    ModSettings.xrayShowPlatinum = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Platinum",
                        () -> ModSettings.xrayPlatinumR, () -> ModSettings.xrayPlatinumG, () -> ModSettings.xrayPlatinumB,
                        value -> ModSettings.xrayPlatinumR = value,
                        value -> ModSettings.xrayPlatinumG = value,
                        value -> ModSettings.xrayPlatinumB = value
                ));

        xraySilverButton = new ModuleButton(0, 0, componentWidth, 22, "Silver",
                () -> ModSettings.xrayShowSilver,
                v -> {
                    ModSettings.xrayShowSilver = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Silver",
                        () -> ModSettings.xraySilverR, () -> ModSettings.xraySilverG, () -> ModSettings.xraySilverB,
                        value -> ModSettings.xraySilverR = value,
                        value -> ModSettings.xraySilverG = value,
                        value -> ModSettings.xraySilverB = value
                ));

        xrayBanglumButton = new ModuleButton(0, 0, componentWidth, 22, "Banglum",
                () -> ModSettings.xrayShowBanglum,
                v -> {
                    ModSettings.xrayShowBanglum = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Banglum",
                        () -> ModSettings.xrayBanglumR, () -> ModSettings.xrayBanglumG, () -> ModSettings.xrayBanglumB,
                        value -> ModSettings.xrayBanglumR = value,
                        value -> ModSettings.xrayBanglumG = value,
                        value -> ModSettings.xrayBanglumB = value
                ));

        xrayRuniteButton = new ModuleButton(0, 0, componentWidth, 22, "Runite",
                () -> ModSettings.xrayShowRunite,
                v -> {
                    ModSettings.xrayShowRunite = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Runite",
                        () -> ModSettings.xrayRuniteR, () -> ModSettings.xrayRuniteG, () -> ModSettings.xrayRuniteB,
                        value -> ModSettings.xrayRuniteR = value,
                        value -> ModSettings.xrayRuniteG = value,
                        value -> ModSettings.xrayRuniteB = value
                ));

        xrayCarmotButton = new ModuleButton(0, 0, componentWidth, 22, "Carmot",
                () -> ModSettings.xrayShowCarmot,
                v -> {
                    ModSettings.xrayShowCarmot = v;
                    XrayModule.updateTargetBlocks();
                },
                () -> openXrayColorPicker(
                        "Carmot",
                        () -> ModSettings.xrayCarmotR, () -> ModSettings.xrayCarmotG, () -> ModSettings.xrayCarmotB,
                        value -> ModSettings.xrayCarmotR = value,
                        value -> ModSettings.xrayCarmotG = value,
                        value -> ModSettings.xrayCarmotB = value
                ));



        // =========================================================
        // CUSTOM POKEMON ESP COMPONENTS
        // =========================================================

        customPokemonButton = new ModuleButton(
                panelX + sidebarWidth + 15,
                panelY + 150,
                componentWidth,
                22,
                "Custom Pokémon",
                () -> ModSettings.customPokemonEspEnabled,
                value -> ModSettings.customPokemonEspEnabled = value
        );

        customPokemonInput = new TextInputComponent(
                panelX + sidebarWidth + 15,
                panelY + 180,
                componentWidth,
                20,
                "Pokémon Name",
                "Pikachu...",
                value -> ModSettings.customPokemonName = value
        );
        customPokemonInput.setValue(ModSettings.customPokemonName);

        customPokemonRadiusSlider = new SliderComponent(
                panelX + sidebarWidth + 15,
                panelY + 210,
                componentWidth,
                18,
                "Radius",
                16,
                256,
                ModSettings.customPokemonRadius,
                value -> ModSettings.customPokemonRadius = value.intValue()
        );

        customPokemonRSlider = new SliderComponent(
                panelX + sidebarWidth + 15, panelY + 240, componentWidth, 18, "Custom R",
                0, 255, ModSettings.customPokemonR, value -> ModSettings.customPokemonR = value.intValue()
        );

        customPokemonGSlider = new SliderComponent(
                panelX + sidebarWidth + 15, panelY + 270, componentWidth, 18, "Custom G",
                0, 255, ModSettings.customPokemonG, value -> ModSettings.customPokemonG = value.intValue()
        );

        customPokemonBSlider = new SliderComponent(
                panelX + sidebarWidth + 15, panelY + 300, componentWidth, 18, "Custom B",
                0, 255, ModSettings.customPokemonB, value -> ModSettings.customPokemonB = value.intValue()
        );
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {

        // BACKGROUND OVERLAY
        context.fill(0, 0, width, height, 0x88000000);

        // =========================================================
        // OPEN ANIMATION
        // =========================================================
        scrollOffset += (targetScrollOffset - scrollOffset) * 0.15f;

        targetScrollOffset = Math.max(0, targetScrollOffset);
        targetScrollOffset = Math.min(targetScrollOffset, maxScroll);

        openAnimation += (1f - openAnimation) * 0.12f;

        // =========================================================
        // DRAGGING
        // =========================================================

        if (dragging) {
            panelX = mouseX - dragOffsetX;
            panelY = mouseY - dragOffsetY;
        }

        // =========================================================
        // ANIMATION MATRIX
        // =========================================================

        context.getMatrices().push();

        context.getMatrices().translate(width / 2f, height / 2f, 0);
        context.getMatrices().scale(openAnimation, openAnimation, 1f);
        context.getMatrices().translate(-width / 2f, -height / 2f, 0);

        // =========================================================
        // MAIN PANEL
        // =========================================================

        RenderUtils.drawRoundedRect(context, panelX, panelY, panelWidth, panelHeight, 8, 0xFF1A1A1A);

        // TITLE BAR
        RenderUtils.drawRoundedRect(context, panelX, panelY, panelWidth, 28, 8, 0xFF111111);
        context.drawText(textRenderer, "§5GodClient §7| §fQOL Client", panelX + 10, panelY + 10, 0xFFFFFFFF, false);

        // SIDEBAR
        RenderUtils.drawRoundedRect(context, panelX, panelY + 28, sidebarWidth, panelHeight - 28, 0, 0xFF161616);

        // =========================================================
        // SIDEBAR CATEGORIES
        // =========================================================

        int catY = panelY + 45;

        drawCategory(context, Category.ESP, panelX + 12, catY);
        catY += 22;
        drawCategory(context, Category.FARMING, panelX + 12, catY);
        catY += 22;
        drawCategory(context, Category.MISC, panelX + 12, catY);
        catY += 22;
        drawCategory(context, Category.MOVEMENT, panelX + 12, catY);
        catY += 22;
        drawCategory(context, Category.XRAY, panelX + 12, catY);

        // =========================================================
        // SCISSOR
        // =========================================================

        context.enableScissor(
                panelX + sidebarWidth,
                panelY + 30,
                panelX + panelWidth - 6,
                panelY + panelHeight - 6
        );

        // =========================================================
        // CATEGORY COMPONENTS
        // =========================================================

        if (selectedCategory == Category.ESP) {

            blisseyButton.setPosition(panelX + sidebarWidth + 15, (int)(panelY + 40 - scrollOffset));
            shinyButton.setPosition(panelX + sidebarWidth + 15, (int)(panelY + 68 - scrollOffset));
            ivButton.setPosition(panelX + sidebarWidth + 15, (int)(panelY + 96 - scrollOffset));
            espRadiusSlider.setPosition(panelX + sidebarWidth + 15, (int)(panelY + 136 - scrollOffset));

            // CUSTOM POKEMON ESP
            customPokemonButton.setPosition(panelX + sidebarWidth + 15, (int)(panelY + 176 - scrollOffset));
            customPokemonInput.setPosition(panelX + sidebarWidth + 15, (int)(panelY + 204 - scrollOffset));
            customPokemonRadiusSlider.setPosition(panelX + sidebarWidth + 15, (int)(panelY + 232 - scrollOffset));
            customPokemonRSlider.setPosition(panelX + sidebarWidth + 15, (int)(panelY + 262 - scrollOffset));
            customPokemonGSlider.setPosition(panelX + sidebarWidth + 15, (int)(panelY + 290 - scrollOffset));
            customPokemonBSlider.setPosition(panelX + sidebarWidth + 15, (int)(panelY + 318 - scrollOffset));

            blisseyButton.render(context, mouseX, mouseY);
            shinyButton.render(context, mouseX, mouseY);
            ivButton.render(context, mouseX, mouseY);
            espRadiusSlider.render(context, mouseX, mouseY);

            // CUSTOM POKEMON RENDER
            customPokemonButton.render(context, mouseX, mouseY);
            customPokemonInput.render(context, mouseX, mouseY);
            customPokemonRadiusSlider.render(context, mouseX, mouseY);
            customPokemonRSlider.render(context, mouseX, mouseY);
            customPokemonGSlider.render(context, mouseX, mouseY);
            customPokemonBSlider.render(context, mouseX, mouseY);

            // VALIDATION - zobraz status pokémona
            int statusColor = ModSettings.customPokemonFound ? 0xFF00FF00 : 0xFFFF0000;
            String status = ModSettings.customPokemonFound ? "✓ Found" : "✗ Not Found";
            context.drawText(textRenderer, status, panelX + sidebarWidth + 15 + componentWidth - 60, (int)(panelY + 207 - scrollOffset), statusColor, false);
        }

        if (selectedCategory == Category.FARMING) {
            int currentY = 40;

            apricornButton.setPosition(panelX + sidebarWidth + 15, (int)(panelY + currentY - scrollOffset));
            apricornButton.render(context, mouseX, mouseY);
            currentY += 28;

            berryButton.setPosition(panelX + sidebarWidth + 15, (int)(panelY + currentY - scrollOffset));
            berryButton.render(context, mouseX, mouseY);
            currentY += 28;

            if (ModSettings.berryExpanded) {
                int subX = panelX + sidebarWidth + 35;

                grepaButton.setPosition(subX,  (int)(panelY + currentY - scrollOffset)); grepaButton.render(context,  mouseX, mouseY); currentY += 22;
                pomegButton.setPosition(subX,  (int)(panelY + currentY - scrollOffset)); pomegButton.render(context,  mouseX, mouseY); currentY += 22;
                tamatoButton.setPosition(subX, (int)(panelY + currentY - scrollOffset)); tamatoButton.render(context, mouseX, mouseY); currentY += 22;
                hondewButton.setPosition(subX, (int)(panelY + currentY - scrollOffset)); hondewButton.render(context, mouseX, mouseY); currentY += 22;
                qualotButton.setPosition(subX, (int)(panelY + currentY - scrollOffset)); qualotButton.render(context, mouseX, mouseY); currentY += 22;
                kelpsyButton.setPosition(subX, (int)(panelY + currentY - scrollOffset)); kelpsyButton.render(context, mouseX, mouseY); currentY += 28;
            }

            vivichokeButton.setPosition(panelX + sidebarWidth + 15, (int)(panelY + currentY - scrollOffset));
            vivichokeButton.render(context, mouseX, mouseY);
            currentY += 40;

            radiusSlider.setPosition(panelX + sidebarWidth + 15, (int)(panelY + currentY - scrollOffset));
            radiusSlider.render(context, mouseX, mouseY);
        }

        if (selectedCategory == Category.MISC) {
            int miscY = (int)(panelY + 40 - scrollOffset);
            int previewX = panelX + sidebarWidth + componentWidth + 35;

            drawSectionHeader(context, "Blissey ESP Color", panelX + sidebarWidth + 15, miscY);
            miscY += 16;

            int blisseyYPos = miscY;
            blisseyRSlider.setPosition(panelX + sidebarWidth + 15, miscY); miscY += 24;
            blisseyGSlider.setPosition(panelX + sidebarWidth + 15, miscY); miscY += 24;
            blisseyBSlider.setPosition(panelX + sidebarWidth + 15, miscY); miscY += 40;

            drawSectionHeader(context, "Shiny/Legendary Color", panelX + sidebarWidth + 15, miscY);
            miscY += 16;

            int shinyYPos = miscY;
            shinyRSlider.setPosition(panelX + sidebarWidth + 15, miscY); miscY += 24;
            shinyGSlider.setPosition(panelX + sidebarWidth + 15, miscY); miscY += 24;
            shinyBSlider.setPosition(panelX + sidebarWidth + 15, miscY); miscY += 40;

            drawSectionHeader(context, "IV Scanner Color", panelX + sidebarWidth + 15, miscY);
            miscY += 16;

            int ivYPos = miscY;
            ivRSlider.setPosition(panelX + sidebarWidth + 15, miscY); miscY += 24;
            ivGSlider.setPosition(panelX + sidebarWidth + 15, miscY); miscY += 24;
            ivBSlider.setPosition(panelX + sidebarWidth + 15, miscY); miscY += 40;

            drawSectionHeader(context, "Berry Drops Color", panelX + sidebarWidth + 15, miscY);
            miscY += 16;

            int berryYPos = miscY;
            berryRSlider.setPosition(panelX + sidebarWidth + 15, miscY); miscY += 24;
            berryGSlider.setPosition(panelX + sidebarWidth + 15, miscY); miscY += 24;
            berryBSlider.setPosition(panelX + sidebarWidth + 15, miscY); miscY += 40;

            drawSectionHeader(context, "GUI Settings", panelX + sidebarWidth + 15, miscY);
            miscY += 16;

            guiWidthSlider.setPosition(panelX + sidebarWidth + 15, miscY); miscY += 24;
            guiHeightSlider.setPosition(panelX + sidebarWidth + 15, miscY);

            blisseyRSlider.render(context, mouseX, mouseY);
            blisseyGSlider.render(context, mouseX, mouseY);
            blisseyBSlider.render(context, mouseX, mouseY);

            shinyRSlider.render(context, mouseX, mouseY);
            shinyGSlider.render(context, mouseX, mouseY);
            shinyBSlider.render(context, mouseX, mouseY);

            ivRSlider.render(context, mouseX, mouseY);
            ivGSlider.render(context, mouseX, mouseY);
            ivBSlider.render(context, mouseX, mouseY);

            berryRSlider.render(context, mouseX, mouseY);
            berryGSlider.render(context, mouseX, mouseY);
            berryBSlider.render(context, mouseX, mouseY);

            guiWidthSlider.render(context, mouseX, mouseY);
            guiHeightSlider.render(context, mouseX, mouseY);

            int blisseyPreview = (255 << 24) | (ModSettings.blisseyR << 16) | (ModSettings.blisseyG << 8) | ModSettings.blisseyB;
            RenderUtils.drawRoundedRect(context, previewX, blisseyYPos, 20, 20, 4, blisseyPreview);

            int shinyPreview = (255 << 24) | (ModSettings.shinyR << 16) | (ModSettings.shinyG << 8) | ModSettings.shinyB;
            RenderUtils.drawRoundedRect(context, previewX, shinyYPos, 20, 20, 4, shinyPreview);

            int ivPreview = (255 << 24) | (ModSettings.ivR << 16) | (ModSettings.ivG << 8) | ModSettings.ivB;
            RenderUtils.drawRoundedRect(context, previewX, ivYPos, 20, 20, 4, ivPreview);

            int berryPreview = (255 << 24) | (ModSettings.berryR << 16) | (ModSettings.berryG << 8) | ModSettings.berryB;
            RenderUtils.drawRoundedRect(context, previewX, berryYPos, 20, 20, 4, berryPreview);
        }

        if (selectedCategory == Category.MOVEMENT) {

            int movementY =
                    (int)(panelY + 40 - scrollOffset);

            autoJumpButton.setPosition(
                    panelX + sidebarWidth + 15,
                    movementY
            );

            autoJumpButton.render(
                    context,
                    mouseX,
                    mouseY
            );
        }

        // =========================================================
        // X-RAY CATEGORY
        // =========================================================

        if (selectedCategory == Category.XRAY) {
            int xrayY = (int)(panelY + 40 - scrollOffset);
            int xrayPreviewX = panelX + sidebarWidth + componentWidth + 35;

            xrayButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayButton.render(context, mouseX, mouseY);
            xrayY += 28;

            xrayTracerButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayTracerButton.render(context, mouseX, mouseY);
            xrayY += 28;

            autoMineButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            autoMineButton.render(context, mouseX, mouseY);
            xrayY += 28;

            legitModeButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            legitModeButton.render(context, mouseX, mouseY);
            xrayY += 28;

            xrayRadiusSlider.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayRadiusSlider.render(context, mouseX, mouseY);
            xrayY += 40;

            xrayOpacitySlider.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayOpacitySlider.render(context, mouseX, mouseY);
            xrayY += 40;

            fullbrightButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            fullbrightButton.render(context, mouseX, mouseY);
            xrayY += 40;

            // VANILLA ORES HEADER
            drawSectionHeader(context, "Vanilla Ores", panelX + sidebarWidth + 15, xrayY);
            xrayY += 16;

            xrayDiamondButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayDiamondButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayDiamondR, ModSettings.xrayDiamondG, ModSettings.xrayDiamondB);
            xrayY += 24;

            xrayEmeraldButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayEmeraldButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayEmeraldR, ModSettings.xrayEmeraldG, ModSettings.xrayEmeraldB);
            xrayY += 24;

            xrayAncientDebrisButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayAncientDebrisButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayAncientDebrisR, ModSettings.xrayAncientDebrisG, ModSettings.xrayAncientDebrisB);
            xrayY += 24;

            xrayGoldButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayGoldButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayGoldR, ModSettings.xrayGoldG, ModSettings.xrayGoldB);
            xrayY += 24;

            xrayIronButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayIronButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayIronR, ModSettings.xrayIronG, ModSettings.xrayIronB);
            xrayY += 24;

            xrayCoalButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayCoalButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayCoalR, ModSettings.xrayCoalG, ModSettings.xrayCoalB);
            xrayY += 24;

            xrayLapisButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayLapisButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayLapisR, ModSettings.xrayLapisG, ModSettings.xrayLapisB);
            xrayY += 24;

            xrayRedstoneButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayRedstoneButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayRedstoneR, ModSettings.xrayRedstoneG, ModSettings.xrayRedstoneB);
            xrayY += 24;

            xrayCopperButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayCopperButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayCopperR, ModSettings.xrayCopperG, ModSettings.xrayCopperB);
            xrayY += 24;

            xrayQuartzButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayQuartzButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayQuartzR, ModSettings.xrayQuartzG, ModSettings.xrayQuartzB);
            xrayY += 40;

            // MYTHIC METALS HEADER
            drawSectionHeader(context, "Mythic Metals", panelX + sidebarWidth + 15, xrayY);
            xrayY += 16;

            keystoneOreButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            keystoneOreButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayKeystoneR, ModSettings.xrayKeystoneG, ModSettings.xrayKeystoneB);
            xrayY += 24;

            xrayKyberButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayKyberButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayKyberR, ModSettings.xrayKyberG, ModSettings.xrayKyberB);
            xrayY += 24;

            xrayOrichalcumButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayOrichalcumButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayOrichalcumR, ModSettings.xrayOrichalcumG, ModSettings.xrayOrichalcumB);
            xrayY += 24;

            xrayKalimiteButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayKalimiteButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayKalimiteR, ModSettings.xrayKalimiteG, ModSettings.xrayKalimiteB);
            xrayY += 24;

            xrayMalachiteButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayMalachiteButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayMalachiteR, ModSettings.xrayMalachiteG, ModSettings.xrayMalachiteB);
            xrayY += 24;

            xrayTitaniumButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayTitaniumButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayTitaniumR, ModSettings.xrayTitaniumG, ModSettings.xrayTitaniumB);
            xrayY += 24;

            xrayAdamantiteButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayAdamantiteButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayAdamantiteR, ModSettings.xrayAdamantiteG, ModSettings.xrayAdamantiteB);
            xrayY += 24;

            xrayMithrilButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayMithrilButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayMithrilR, ModSettings.xrayMithrilG, ModSettings.xrayMithrilB);
            xrayY += 24;

            xrayPlatinumButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayPlatinumButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayPlatinumR, ModSettings.xrayPlatinumG, ModSettings.xrayPlatinumB);
            xrayY += 24;

            xraySilverButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xraySilverButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xraySilverR, ModSettings.xraySilverG, ModSettings.xraySilverB);
            xrayY += 24;

            xrayBanglumButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayBanglumButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayBanglumR, ModSettings.xrayBanglumG, ModSettings.xrayBanglumB);
            xrayY += 24;

            xrayRuniteButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayRuniteButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayRuniteR, ModSettings.xrayRuniteG, ModSettings.xrayRuniteB);
            xrayY += 24;

            xrayCarmotButton.setPosition(panelX + sidebarWidth + 15, xrayY);
            xrayCarmotButton.render(context, mouseX, mouseY);
            drawXrayColorPreview(context, xrayPreviewX, xrayY + 1, ModSettings.xrayCarmotR, ModSettings.xrayCarmotG, ModSettings.xrayCarmotB);
            xrayY += 24;

            if (currentColorPicker != null) {
                currentColorPicker.setPosition(panelX + panelWidth - 185, panelY + 36);
                currentColorPicker.render(context, mouseX, mouseY);
            }
        }

        context.disableScissor();

        // =========================================================
        // SCROLLBAR
        // =========================================================

        float contentHeight = 0f;

        if (selectedCategory == Category.ESP) {
            contentHeight = 360f;
        }

        if (selectedCategory == Category.FARMING) {
            contentHeight = ModSettings.berryExpanded ? 320f : 150f;
        }

        if (selectedCategory == Category.MISC) {
            contentHeight = 590f;
        }

        if (selectedCategory == Category.MOVEMENT) {
            contentHeight = 100f;
        }

        if (selectedCategory == Category.XRAY) {
            contentHeight = 1040f;
        }

        maxScroll = Math.max(0, contentHeight - (panelHeight - 40));

        float scrollProgress = maxScroll <= 0 ? 0 : scrollOffset / maxScroll;
        scrollProgress = Math.min(scrollProgress, 1f);

        int scrollbarHeight = Math.max(30, (int)((panelHeight - 70) * ((float)(panelHeight - 40) / contentHeight)));
        int scrollbarY = (int)(panelY + 35 + scrollProgress * (panelHeight - 70 - scrollbarHeight));

        if (maxScroll > 0) {
            RenderUtils.drawRoundedRect(context, panelX + panelWidth - 6, panelY + 35, 3, panelHeight - 70, 2, 0xFF222222);
            RenderUtils.drawRoundedRect(context, panelX + panelWidth - 6, scrollbarY, 3, scrollbarHeight, 2, 0xFF9B5CFF);
        }

        context.getMatrices().pop();

        super.render(context, mouseX, mouseY, delta);
    }

    private void openXrayColorPicker(
            String oreName,
            IntSupplier rGetter,
            IntSupplier gGetter,
            IntSupplier bGetter,
            IntConsumer rSetter,
            IntConsumer gSetter,
            IntConsumer bSetter
    ) {
        currentColorPicker = new ColorPickerComponent(
                oreName + " Color",
                rGetter,
                gGetter,
                bGetter,
                rSetter,
                gSetter,
                bSetter
        );
    }

    private void drawXrayColorPreview(DrawContext context, int x, int y, int r, int g, int b) {
        RenderUtils.drawRoundedRect(context, x, y, 20, 20, 4, rgb(r, g, b));
    }

    private int rgb(int r, int g, int b) {
        return (0xFF << 24)
                | ((r & 0xFF) << 16)
                | ((g & 0xFF) << 8)
                | (b & 0xFF);
    }

    private void drawSectionHeader(DrawContext context, String title, int x, int y) {
        String header = "━━ " + title + " ━━";
        context.drawText(textRenderer, "§7" + header, x, y, 0xFF888888, false);
    }

    private void drawCategory(
            DrawContext context,
            Category category,
            int x,
            int y
    ) {
        boolean selected = selectedCategory == category;

        if (selected) {
            RenderUtils.drawRoundedRect(context, x - 4, y - 2, 54, 14, 3, 0x449B5CFF);
        }

        context.drawText(textRenderer, category.name(), x, y, selected ? 0xFF9B5CFF : 0xFFFFFFFF, false);
    }

    private boolean isHovering(int x, int y, int w, int h, double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (mouseX >= panelX && mouseX <= panelX + panelWidth
                && mouseY >= panelY && mouseY <= panelY + 28) {
            dragging = true;
            dragOffsetX = (int) mouseX - panelX;
            dragOffsetY = (int) mouseY - panelY;
        }

        int catY = panelY + 45;

        if (isHovering(panelX + 8, catY, 60, 14, mouseX, mouseY)) {
            selectedCategory = Category.ESP;
            targetScrollOffset = 0;
            scrollOffset = 0;
        }

        catY += 22;

        if (isHovering(panelX + 8, catY, 60, 14, mouseX, mouseY)) {
            selectedCategory = Category.FARMING;
            targetScrollOffset = 0;
            scrollOffset = 0;
        }

        catY += 22;

        if (isHovering(panelX + 8, catY, 60, 14, mouseX, mouseY)) {
            selectedCategory = Category.MISC;
            targetScrollOffset = 0;
            scrollOffset = 0;
        }

        catY += 22;

        if (isHovering(panelX + 8, catY, 60, 14, mouseX, mouseY)) {
            selectedCategory = Category.MOVEMENT;
            targetScrollOffset = 0;
            scrollOffset = 0;
        }

        catY += 22;

        if (isHovering(panelX + 8, catY, 60, 14, mouseX, mouseY)) {
            selectedCategory = Category.XRAY;
            targetScrollOffset = 0;
            scrollOffset = 0;
        }

        if (selectedCategory != Category.XRAY) {
            currentColorPicker = null;
        }

        if (selectedCategory == Category.ESP) {
            blisseyButton.mouseClicked(mouseX, mouseY, button);
            shinyButton.mouseClicked(mouseX, mouseY, button);
            ivButton.mouseClicked(mouseX, mouseY, button);
            espRadiusSlider.mouseClicked(mouseX, mouseY);

                // CUSTOM POKEMON
                customPokemonButton.mouseClicked(mouseX, mouseY, button);
                customPokemonInput.mouseClicked(mouseX, mouseY, button);
                customPokemonRadiusSlider.mouseClicked(mouseX, mouseY);
                customPokemonRSlider.mouseClicked(mouseX, mouseY);
                customPokemonGSlider.mouseClicked(mouseX, mouseY);
                customPokemonBSlider.mouseClicked(mouseX, mouseY);
        }

        if (selectedCategory == Category.FARMING) {
            apricornButton.mouseClicked(mouseX, mouseY, button);
            berryButton.mouseClicked(mouseX, mouseY, button);

            if (ModSettings.berryExpanded) {
                grepaButton.mouseClicked(mouseX, mouseY, button);
                pomegButton.mouseClicked(mouseX, mouseY, button);
                tamatoButton.mouseClicked(mouseX, mouseY, button);
                hondewButton.mouseClicked(mouseX, mouseY, button);
                qualotButton.mouseClicked(mouseX, mouseY, button);
                kelpsyButton.mouseClicked(mouseX, mouseY, button);
            }

            vivichokeButton.mouseClicked(mouseX, mouseY, button);
            radiusSlider.mouseClicked(mouseX, mouseY);
        }

        if (selectedCategory == Category.MISC) {
            blisseyRSlider.mouseClicked(mouseX, mouseY);
            blisseyGSlider.mouseClicked(mouseX, mouseY);
            blisseyBSlider.mouseClicked(mouseX, mouseY);

            shinyRSlider.mouseClicked(mouseX, mouseY);
            shinyGSlider.mouseClicked(mouseX, mouseY);
            shinyBSlider.mouseClicked(mouseX, mouseY);

            ivRSlider.mouseClicked(mouseX, mouseY);
            ivGSlider.mouseClicked(mouseX, mouseY);
            ivBSlider.mouseClicked(mouseX, mouseY);

            berryRSlider.mouseClicked(mouseX, mouseY);
            berryGSlider.mouseClicked(mouseX, mouseY);
            berryBSlider.mouseClicked(mouseX, mouseY);

            guiWidthSlider.mouseClicked(mouseX, mouseY);
            guiHeightSlider.mouseClicked(mouseX, mouseY);
        }

        if (selectedCategory == Category.MOVEMENT) {
            autoJumpButton.mouseClicked(mouseX, mouseY, button);
        }

        if (selectedCategory == Category.XRAY) {
            if (currentColorPicker != null && currentColorPicker.isMouseOver(mouseX, mouseY)) {
                if (currentColorPicker.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            } else if (button == 0 && (currentColorPicker == null || !currentColorPicker.isDragging())) {
                currentColorPicker = null;
            }

            xrayButton.mouseClicked(mouseX, mouseY, button);
            xrayRadiusSlider.mouseClicked(mouseX, mouseY);
            xrayOpacitySlider.mouseClicked(mouseX, mouseY);
            fullbrightButton.mouseClicked(mouseX, mouseY, button);
            xrayTracerButton.mouseClicked(mouseX, mouseY, button);
            autoMineButton.mouseClicked(mouseX, mouseY, button);
            legitModeButton.mouseClicked(mouseX, mouseY, button);

            // Vanilla ores
            xrayDiamondButton.mouseClicked(mouseX, mouseY, button);
            xrayEmeraldButton.mouseClicked(mouseX, mouseY, button);
            xrayAncientDebrisButton.mouseClicked(mouseX, mouseY, button);
            xrayGoldButton.mouseClicked(mouseX, mouseY, button);
            xrayIronButton.mouseClicked(mouseX, mouseY, button);
            xrayCoalButton.mouseClicked(mouseX, mouseY, button);
            xrayLapisButton.mouseClicked(mouseX, mouseY, button);
            xrayRedstoneButton.mouseClicked(mouseX, mouseY, button);
            xrayCopperButton.mouseClicked(mouseX, mouseY, button);
            xrayQuartzButton.mouseClicked(mouseX, mouseY, button);

            // Mythic Metals
            keystoneOreButton.mouseClicked(mouseX, mouseY, button);
            xrayKyberButton.mouseClicked(mouseX, mouseY, button);
            xrayOrichalcumButton.mouseClicked(mouseX, mouseY, button);
            xrayKalimiteButton.mouseClicked(mouseX, mouseY, button);
            xrayMalachiteButton.mouseClicked(mouseX, mouseY, button);
            xrayTitaniumButton.mouseClicked(mouseX, mouseY, button);
            xrayAdamantiteButton.mouseClicked(mouseX, mouseY, button);
            xrayMithrilButton.mouseClicked(mouseX, mouseY, button);
            xrayPlatinumButton.mouseClicked(mouseX, mouseY, button);
            xraySilverButton.mouseClicked(mouseX, mouseY, button);
            xrayBanglumButton.mouseClicked(mouseX, mouseY, button);
            xrayRuniteButton.mouseClicked(mouseX, mouseY, button);
            xrayCarmotButton.mouseClicked(mouseX, mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {
        dragging = false;

        if (selectedCategory == Category.ESP) {
            espRadiusSlider.mouseReleased();
            customPokemonRadiusSlider.mouseReleased();
            customPokemonRSlider.mouseReleased();
            customPokemonGSlider.mouseReleased();
            customPokemonBSlider.mouseReleased();
        }

        if (selectedCategory == Category.FARMING) {
            radiusSlider.mouseReleased();
        }

        if (selectedCategory == Category.MISC) {
            blisseyRSlider.mouseReleased();
            blisseyGSlider.mouseReleased();
            blisseyBSlider.mouseReleased();

            shinyRSlider.mouseReleased();
            shinyGSlider.mouseReleased();
            shinyBSlider.mouseReleased();

            ivRSlider.mouseReleased();
            ivGSlider.mouseReleased();
            ivBSlider.mouseReleased();

            berryRSlider.mouseReleased();
            berryGSlider.mouseReleased();
            berryBSlider.mouseReleased();

            guiWidthSlider.mouseReleased();
            guiHeightSlider.mouseReleased();
        }

        if (selectedCategory == Category.XRAY) {
            xrayRadiusSlider.mouseReleased();
            xrayOpacitySlider.mouseReleased();
            if (currentColorPicker != null) {
                currentColorPicker.mouseReleased();
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (selectedCategory == Category.XRAY && currentColorPicker != null && currentColorPicker.mouseDragged(mouseX, mouseY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (selectedCategory == Category.ESP && customPokemonInput != null) {
            return customPokemonInput.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (selectedCategory == Category.ESP && customPokemonInput != null) {
            if (customPokemonInput.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}