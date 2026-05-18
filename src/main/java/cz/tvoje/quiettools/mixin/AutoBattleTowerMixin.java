package cz.tvoje.quiettools.mixin;

import cz.tvoje.quiettools.ModSettings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.List;

@Mixin(Screen.class)
public class AutoBattleTowerMixin {

    @Unique
    private int tickDelay = 0;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    public void onScreenTick(
            CallbackInfo ci
    ) {

        if (!ModSettings.autoBattleTowerEnabled) {
            return;
        }

        tickDelay++;

        Screen screen =
                (Screen) (Object) this;

        // =======================================================
        // BATTLE TOWER GUI
        // =======================================================

        if (
                screen.getClass()
                        .getSimpleName()
                        .equals("BattleTowerScreen")
        ) {

            if (tickDelay < 10) {
                return;
            }

            try {

                Class<?> towerClass =
                        screen.getClass();

                Field lockedInField =
                        towerClass.getDeclaredField(
                                "partyLockedIn"
                        );

                lockedInField.setAccessible(true);

                boolean isLockedIn =
                        lockedInField.getBoolean(screen);

                // ===================================================
                // LOCK TEAM
                // ===================================================

                if (!isLockedIn) {

                    System.out.println(
                            "[AutoTower] Locking team..."
                    );

                    Field levelModeField =
                            towerClass.getDeclaredField(
                                    "selectedLevelMode"
                            );

                    levelModeField.setAccessible(true);

                    int levelModeInt =
                            levelModeField.getInt(screen);

                    String levelMode =
                            (levelModeInt == 1)
                                    ? "LEVEL_100"
                                    : "LEVEL_50";

                    Field bossCountField =
                            towerClass.getDeclaredField(
                                    "selectedBossPlayerCount"
                            );

                    bossCountField.setAccessible(true);

                    int bossCount =
                            bossCountField.getInt(screen);

                    Class<?> networkClass =
                            Class.forName(
                                    "battle.tower.network.BattleTowerNetwork"
                            );

                    Method sendLockIn =
                            networkClass.getMethod(
                                    "sendLockInTeam",
                                    String.class,
                                    boolean.class,
                                    String.class,
                                    String.class,
                                    int.class
                            );

                    sendLockIn.invoke(
                            null,
                            levelMode,
                            ModSettings.allowLegendaries,
                            "SINGLES",
                            "NORMAL",
                            bossCount
                    );

                    tickDelay = 0;
                }

                // ===================================================
                // START BATTLE
                // ===================================================

                else {

                    System.out.println(
                            "[AutoTower] Starting battle..."
                    );

                    Field currentFloorField =
                            towerClass.getDeclaredField(
                                    "currentFloor"
                            );

                    currentFloorField.setAccessible(true);

                    int currentFloor =
                            currentFloorField.getInt(screen);

                    Field blockPosField =
                            towerClass.getDeclaredField(
                                    "blockPos"
                            );

                    blockPosField.setAccessible(true);

                    Object blockPosObj =
                            blockPosField.get(screen);

                    Class<?> networkClass =
                            Class.forName(
                                    "battle.tower.network.BattleTowerNetwork"
                            );

                    for (Method m : networkClass.getMethods()) {

                        if (
                                m.getName()
                                        .equals("sendStartBattleRequest")
                        ) {

                            m.invoke(
                                    null,
                                    currentFloor,
                                    blockPosObj
                            );

                            break;
                        }
                    }

                    MinecraftClient.getInstance()
                            .setScreen(null);
                }

            } catch (Exception e) {

                System.out.println(
                        "[AutoTower] Reflection error: "
                                + e.getMessage()
                );
            }

            return;
        }

        // =======================================================
        // NORMAL BATTLE GUI
        // =======================================================

        if (tickDelay < 15) {
            return;
        }

        for (Object element : screen.children()) {

            if (!(element instanceof ClickableWidget widget)) {
                continue;
            }

            String text =
                    widget.getMessage()
                            .getString()
                            .toLowerCase();

            // ===================================================
            // OPEN MOVE MENU
            // ===================================================

            if (
                    text.contains("choose_action")
            ) {

                System.out.println(
                        "[AutoBattle] Opening move menu"
                );

                simulateClick(widget);

                tickDelay = -10;

                return;
            }

            // ===================================================
            // MOVE MENU
            // ===================================================

            if (
                    text.contains("select_move")
            ) {

                System.out.println(
                        "[AutoBattle] Move menu detected"
                );

                try {

                    Field moveTilesField =
                            widget.getClass()
                                    .getDeclaredField(
                                            "moveTiles"
                                    );

                    moveTilesField.setAccessible(true);

                    List<?> moveTiles =
                            (List<?>) moveTilesField.get(widget);

                    if (
                            moveTiles == null
                                    || moveTiles.isEmpty()
                    ) {

                        return;
                    }

                    int randomIndex =
                            (int) (
                                    Math.random()
                                            * moveTiles.size()
                            );

                    Object moveTile =
                            moveTiles.get(randomIndex);

                    System.out.println(
                            "[AutoBattle] Clicking move tile "
                                    + randomIndex
                    );

                    Method onClickMethod = null;

                    for (
                            Method method :
                            moveTile.getClass().getMethods()
                    ) {

                        if (
                                method.getName()
                                        .equalsIgnoreCase("onClick")
                        ) {

                            onClickMethod = method;
                            break;
                        }
                    }

                    if (onClickMethod != null) {

                        onClickMethod.invoke(
                                moveTile
                        );

                        System.out.println(
                                "[AutoBattle] Move selected"
                        );

                        tickDelay = 20;

                        return;
                    }

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

            // ===================================================
            // POKEMON SWITCH MENU
            // ===================================================

            if (
                    text.contains("switch_pokemon")
            ) {

                System.out.println(
                        "[AutoBattle] Pokemon switch menu detected"
                );

                try {

                    Field tilesField =
                            widget.getClass()
                                    .getDeclaredField(
                                            "tiles"
                                    );

                    tilesField.setAccessible(true);

                    List<?> pokemonTiles =
                            (List<?>) tilesField.get(widget);

                    if (
                            pokemonTiles == null
                                    || pokemonTiles.isEmpty()
                    ) {

                        return;
                    }

                    int randomIndex =
                            (int) (
                                    Math.random()
                                            * pokemonTiles.size()
                            );

                    Object pokemonTile =
                            pokemonTiles.get(randomIndex);

                    System.out.println(
                            "[AutoBattle] Clicking pokemon tile "
                                    + randomIndex
                    );

                    // ===================================================
                    // GET SELECTION OBJECT
                    // ===================================================

                    Method getSelectionMethod =
                            pokemonTile.getClass()
                                    .getMethod("getSelection");

                    Object selection =
                            getSelectionMethod.invoke(
                                    pokemonTile
                            );

                    // ===================================================
                    // GET TILE POSITION
                    // ===================================================

                    Method getX =
                            pokemonTile.getClass()
                                    .getMethod("getX");

                    Method getY =
                            pokemonTile.getClass()
                                    .getMethod("getY");

                    double x =
                            ((Number) getX.invoke(pokemonTile))
                                    .doubleValue();

                    double y =
                            ((Number) getY.invoke(pokemonTile))
                                    .doubleValue();

                    // ===================================================
                    // REAL OWO CLICK
                    // ===================================================

                    Method mousePrimaryClicked =
                            selection.getClass()
                                    .getMethod(
                                            "mousePrimaryClicked",
                                            double.class,
                                            double.class
                                    );

                    mousePrimaryClicked.invoke(
                            selection,
                            x + 5,
                            y + 5
                    );

                    System.out.println(
                            "[AutoBattle] Pokemon selected successfully"
                    );

                    tickDelay = 40;

                    return;

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }
    }

    @Unique
    private void simulateClick(
            ClickableWidget widget
    ) {

        tickDelay = 0;

        widget.mouseClicked(
                widget.getX() + 1,
                widget.getY() + 1,
                0
        );

        widget.mouseReleased(
                widget.getX() + 1,
                widget.getY() + 1,
                0
        );
    }
}