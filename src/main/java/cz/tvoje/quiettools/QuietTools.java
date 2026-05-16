package cz.tvoje.quiettools;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.tvoje.quiettools.gui.ClickGuiScreen;
import cz.tvoje.quiettools.movement.AutoJumpAssist;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.block.Block;

public class QuietTools implements ClientModInitializer {

    public static final String MOD_ID = "betterthirdperson";
    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    public static boolean autoJumpAssist = false;
    public static KeyBinding guiKey;

    // Klávesy pro Harvester
    public static KeyBinding berryKey;
    public static KeyBinding apricornKey;
    public static KeyBinding vivichokeKey;

    public static KeyBinding radiusUpKey;
    public static KeyBinding radiusDownKey;

    // Klávesy pro ESP
    public static KeyBinding blisseyKey;
    public static KeyBinding shinyKey;
    public static KeyBinding ivKey;

    // Klávesa pro Fullbright
    public static KeyBinding fullbrightKey;

    @Override
    public void onInitializeClient() {

        guiKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.betterthirdperson.gui",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_RIGHT_SHIFT,
                        "category.betterthirdperson"
                )
        );

        // Registrace ESP rendereru
        ESPRenderer.register();
        AutoJumpAssist.register();
        FullbrightModule.register();
        // --- REGISTRACE KLÁVES ---

        // Registrace Vivichoke klávesy (N)
        vivichokeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterthirdperson.toggle_vivichoke",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.betterthirdperson"
        ));

        // 1. Registrace klávesy na prostřední tlačítko myši
        KeyBinding selectTargetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Vybrat Auto-Mine cíl",
                InputUtil.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
                "GodClient" // Kategorie v nastavení ovládání
        ));

// 2. Kontrola kliknutí každý tick hry
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (selectTargetKey.wasPressed()) {

                // =========================================================
                // A) ZÁCHRANNÁ BRZDA (Shift + Kolečko)
                // =========================================================
                if (client.player != null && client.options.sneakKey.isPressed()) {
                    cz.tvoje.quiettools.XrayModule.clearDynamicTarget();
                    cz.tvoje.quiettools.ModSettings.autoMineBot = false;
                    client.player.sendMessage(Text.literal("§d[GodClient] §cZáchranná brzda! Cíl smazán a bot zastaven."), true);
                    continue;
                }

                // =========================================================
                // B) NORMÁLNÍ VÝBĚR CÍLE (Jen Kolečko)
                // =========================================================
                if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult hitResult = (BlockHitResult) client.crosshairTarget;
                    Block clickedBlock = client.world.getBlockState(hitResult.getBlockPos()).getBlock();

                    if      (clickedBlock == net.minecraft.block.Blocks.AIR ||
                            clickedBlock == net.minecraft.block.Blocks.CAVE_AIR ||
                            clickedBlock == net.minecraft.block.Blocks.VOID_AIR) {
                        continue;
                    }

                    // Odešleme blok do X-Raye!
                    cz.tvoje.quiettools.XrayModule.setDynamicTarget(clickedBlock);

                    // Automaticky zapneme X-Ray a bota
                    cz.tvoje.quiettools.ModSettings.xrayEnabled = true;
                    cz.tvoje.quiettools.ModSettings.autoMineBot = true;

                    // Pošleme hráči hezkou zprávu nad hotbar, ať ví, že to funguje
                    String blockName = clickedBlock.getName().getString();
                    client.player.sendMessage(Text.literal("§d[GodClient] §fCíl nastaven na: §b" + blockName), true);
                }
            }
        });

        // Apricorn ON/OFF
        apricornKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterthirdperson.toggle_harvester",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.betterthirdperson"
        ));

        // Registrace Berry klávesy (B)
        berryKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterthirdperson.toggle_berries",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.betterthirdperson"
        ));

        // Harvester Radius +
        radiusUpKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterthirdperson.radius_up",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_ADD,
                "category.betterthirdperson"
        ));

        // Harvester Radius -
        radiusDownKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterthirdperson.radius_down",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_SUBTRACT,
                "category.betterthirdperson"
        ));

        // Blissey ESP
        blisseyKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterthirdperson.toggle_blissey",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.betterthirdperson"
        ));

        // Shiny/Legendary ESP
        shinyKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterthirdperson.toggle_shiny",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "category.betterthirdperson"
        ));

        // IV Scanner ESP
        ivKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterthirdperson.toggle_ivs",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "category.betterthirdperson"
        ));

        // Fullbright
        fullbrightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterthirdperson.toggle_fullbright",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F,
                "category.betterthirdperson"
        ));

        // --- LOGIKA PO STISKNUTÍ KLÁVES ---
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // =========================
            // OPEN GUI
            // =========================

            while (guiKey.wasPressed()) {
                // Otevře GUI jen tehdy, když nemáš otevřený inventář nebo chat
                if (client.currentScreen == null) {
                    client.setScreen(new ClickGuiScreen());
                }
            }

            // Ovládání Vivichoke (N)
            while (vivichokeKey.wasPressed()) {
                ModSettings.vivichokeEnabled = !ModSettings.vivichokeEnabled;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§2[BTP] Vivichoke Harvest: " + (ModSettings.vivichokeEnabled ? "§aON" : "§cOFF")), true);
                }
            }

            // --- Auto Harvest smyčka ---
            if (client.player != null && client.world != null) {
                // PŘIDÁNO: Sklízeč běží i tehdy, když je zapnutý jen Vivichoke
                if (ModSettings.apricornEnabled || ModSettings.berryEnabled || ModSettings.vivichokeEnabled) {
                    if (client.player.age % 10 == 0) {
                        Harvester.harvest(client.player, client.world);
                    }
                }
            }

            // --- Apricorn ovládání ---
            while (apricornKey.wasPressed()) {
                // OPRAVENO: Používáme ModSettings místo staré proměnné 'enabled'
                ModSettings.apricornEnabled = !ModSettings.apricornEnabled;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal(
                            ModSettings.apricornEnabled ? "§a[BTP] Apricorn Harvest: ON §7| Radius: §e" + Harvester.radius
                                    : "§c[BTP] Apricorn Harvest: OFF"
                    ), true);
                }
            }

            // Ovládání Berries (B)
            while (berryKey.wasPressed()) {
                ModSettings.berryEnabled = !ModSettings.berryEnabled;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§d[BTP] Berry Harvest: " + (ModSettings.berryEnabled ? "§aON" : "§cOFF")), true);
                }
            }

            while (radiusUpKey.wasPressed()) {
                if (Harvester.radius < 20) {
                    Harvester.radius++;
                    if (client.player != null) {
                        client.player.sendMessage(Text.literal("§e[BTP] Harvester Radius: " + Harvester.radius), true);
                    }
                }
            }

            while (radiusDownKey.wasPressed()) {
                if (Harvester.radius > 1) {
                    Harvester.radius--;
                    if (client.player != null) {
                        client.player.sendMessage(Text.literal("§e[BTP] Harvester Radius: " + Harvester.radius), true);
                    }
                }
            }

            // --- ESP ovládání ---
            while (blisseyKey.wasPressed()) {
                ModSettings.blisseyEspEnabled = !ModSettings.blisseyEspEnabled;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§d[BTP] Blissey ESP: " + (ModSettings.blisseyEspEnabled ? "§aON" : "§cOFF")), true);
                }
            }

            while (shinyKey.wasPressed()) {
                ModSettings.shinyLegendaryEspEnabled = !ModSettings.shinyLegendaryEspEnabled;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§6[BTP] Shiny/Legend ESP: " + (ModSettings.shinyLegendaryEspEnabled ? "§aON" : "§cOFF")), true);
                }
            }

            while (ivKey.wasPressed()) {
                ModSettings.ivScannerEnabled = !ModSettings.ivScannerEnabled;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§b[BTP] IV Scanner: " + (ModSettings.ivScannerEnabled ? "§aON" : "§cOFF")), true);
                }
            }

            // Fullbright ovládání (F)
            while (fullbrightKey.wasPressed()) {
                ModSettings.fullbrightEnabled = !ModSettings.fullbrightEnabled;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§e[BTP] Fullbright: " + (ModSettings.fullbrightEnabled ? "§aON" : "§cOFF")), true);
                }
            }

        });

        // =========================================================
        // INICIALIZACE NASTAVENÍ BARITONE
        // =========================================================
        try {
            baritone.api.BaritoneAPI.getSettings().allowInventory.value = true;
        } catch (Exception e) {
            LOGGER.error("Nepodařilo se aplikovat nastavení Baritone!", e);
        }

        LOGGER.info("Better Third Person loaded.");
    }
}