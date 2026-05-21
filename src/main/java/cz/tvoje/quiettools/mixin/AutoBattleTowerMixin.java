package cz.tvoje.quiettools.mixin;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import com.cobblemon.mod.common.pokemon.Species;

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

import java.util.ArrayList;
import java.util.List;

@Mixin(Screen.class)
public class AutoBattleTowerMixin {

    @Unique
    private int tickDelay = 0;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    public void onScreenTick(CallbackInfo ci) {

        if (!ModSettings.autoBattleTowerEnabled) {
            return;
        }

        tickDelay++;

        Screen screen = (Screen) (Object) this;

        // =======================================================
        // BATTLE TOWER GUI
        // =======================================================

        if (screen.getClass().getSimpleName().equals("BattleTowerScreen")) {

            if (tickDelay < 10) return;

            try {
                Class<?> towerClass = screen.getClass();

                Field lockedInField = towerClass.getDeclaredField("partyLockedIn");
                lockedInField.setAccessible(true);
                boolean isLockedIn = lockedInField.getBoolean(screen);

                if (!isLockedIn) {

                    Field levelModeField = towerClass.getDeclaredField("selectedLevelMode");
                    levelModeField.setAccessible(true);
                    int levelModeInt = levelModeField.getInt(screen);
                    String levelMode = (levelModeInt == 1) ? "LEVEL_100" : "LEVEL_50";

                    Field bossCountField = towerClass.getDeclaredField("selectedBossPlayerCount");
                    bossCountField.setAccessible(true);
                    int bossCount = bossCountField.getInt(screen);

                    Class<?> networkClass = Class.forName("battle.tower.network.BattleTowerNetwork");
                    Method sendLockIn = networkClass.getMethod("sendLockInTeam", String.class, boolean.class, String.class, String.class, int.class);
                    sendLockIn.invoke(null, levelMode, ModSettings.allowLegendaries, "SINGLES", "NORMAL", bossCount);

                    tickDelay = 0;

                } else {

                    Field currentFloorField = towerClass.getDeclaredField("currentFloor");
                    currentFloorField.setAccessible(true);
                    int currentFloor = currentFloorField.getInt(screen);

                    Field blockPosField = towerClass.getDeclaredField("blockPos");
                    blockPosField.setAccessible(true);
                    Object blockPosObj = blockPosField.get(screen);

                    Class<?> networkClass = Class.forName("battle.tower.network.BattleTowerNetwork");
                    for (Method m : networkClass.getMethods()) {
                        if (m.getName().equals("sendStartBattleRequest")) {
                            m.invoke(null, currentFloor, blockPosObj);
                            break;
                        }
                    }

                    MinecraftClient.getInstance().setScreen(null);
                }

            } catch (Exception e) {
                System.out.println("[AutoTower] Reflection error: " + e.getMessage());
            }

            return;
        }

        // =======================================================
        // NORMAL BATTLE GUI
        // =======================================================

        if (tickDelay < 15) return;

        // Získáme typy protivníka přímo z BattleGUI.actor
        List<String> enemyTypes = getEnemyTypes();

        for (Object element : screen.children()) {

            if (!(element instanceof ClickableWidget widget)) continue;

            String text = widget.getMessage().getString().toLowerCase();

            // ===================================================
            // OPEN MOVE MENU
            // ===================================================

            if (text.contains("choose_action")) {
                simulateClick(widget);
                tickDelay = -10;
                return;
            }

            // ===================================================
            // MOVE MENU — type-aware scoring
            // ===================================================

            if (text.contains("select_move")) {

                try {
                    Field moveTilesField = widget.getClass().getDeclaredField("moveTiles");
                    moveTilesField.setAccessible(true);
                    List<?> moveTiles = (List<?>) moveTilesField.get(widget);

                    if (moveTiles == null || moveTiles.isEmpty()) return;

                    Object bestTile = null;
                    double bestScore = -999999;

                    for (Object moveTile : moveTiles) {
                        try {
                            double score = scoreMoveVsEnemyTypes(moveTile, enemyTypes);

                            if (score > bestScore) {
                                bestScore = score;
                                bestTile = moveTile;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (bestTile != null) {
                        Method onClickMethod = null;
                        for (Method method : bestTile.getClass().getMethods()) {
                            if (method.getName().equalsIgnoreCase("onClick")) {
                                onClickMethod = method;
                                break;
                            }
                        }

                        if (onClickMethod != null) {
                            onClickMethod.invoke(bestTile);
                            tickDelay = 20;
                            return;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // ===================================================
            // POKEMON SWITCH MENU — type-aware scoring
            // ===================================================

            if (text.contains("switch_pokemon")) {

                try {
                    Field tilesField = widget.getClass().getDeclaredField("tiles");
                    tilesField.setAccessible(true);
                    List<?> pokemonTiles = (List<?>) tilesField.get(widget);

                    if (pokemonTiles == null || pokemonTiles.isEmpty()) return;

                    Object bestTile = null;
                    double bestScore = -999999;

                    for (Object pokemonTile : pokemonTiles) {
                        try {
                            double score = scorePokemonVsEnemyTypes(pokemonTile, enemyTypes);

                            if (score > bestScore) {
                                bestScore = score;
                                bestTile = pokemonTile;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (bestTile != null) {
                        Method getSelectionMethod = bestTile.getClass().getMethod("getSelection");
                        Object selection = getSelectionMethod.invoke(bestTile);

                        Method getX = bestTile.getClass().getMethod("getX");
                        Method getY = bestTile.getClass().getMethod("getY");

                        double x = ((Number) getX.invoke(bestTile)).doubleValue();
                        double y = ((Number) getY.invoke(bestTile)).doubleValue();

                        Method mousePrimaryClicked = selection.getClass().getMethod("mousePrimaryClicked", double.class, double.class);
                        mousePrimaryClicked.invoke(selection, x + 5, y + 5);

                        tickDelay = 40;
                        return;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // =========================================================
    // ZÍSKÁNÍ TYPŮ PROTIVNÍKA PŘÍMO Z BattleGUI.actor
    // =========================================================

    @Unique
    private List<String> getEnemyTypes() {
        List<String> types = new ArrayList<>();

        try {
            Screen currentScreen = MinecraftClient.getInstance().currentScreen;
            if (!(currentScreen instanceof BattleGUI battleGUI)) return types;

            // Získáme actor (náš actor)
            ClientBattleActor actor = battleGUI.getActor();
            if (actor == null) return types;

            // Získáme protivníkovu stranu přes side.getOpponentSide()
            Field sideField = actor.getClass().getDeclaredField("side");
            sideField.setAccessible(true);
            Object side = sideField.get(actor);

            if (side == null) return types;

            // Získáme opponentSide
            Method getOpponentSideMethod = null;
            for (Method m : side.getClass().getMethods()) {
                if (m.getName().contains("pponent") || m.getName().contains("other")) {
                    getOpponentSideMethod = m;
                    break;
                }
            }

            if (getOpponentSideMethod == null) return types;

            Object opponentSide = getOpponentSideMethod.invoke(side);
            if (opponentSide == null) return types;

            // Získáme aktivní pokémony protivníka
            Method getActiveMethod = null;
            for (Method m : opponentSide.getClass().getMethods()) {
                if (m.getName().toLowerCase().contains("active")) {
                    getActiveMethod = m;
                    break;
                }
            }

            if (getActiveMethod == null) return types;

            Object activeList = getActiveMethod.invoke(opponentSide);
            if (!(activeList instanceof Iterable<?> iterable)) return types;

            for (Object activePokemon : iterable) {
                if (!(activePokemon instanceof ActiveClientBattlePokemon active)) continue;

                ClientBattlePokemon battlePokemon = active.getBattlePokemon();
                if (battlePokemon == null) continue;

                Species species = battlePokemon.getSpecies();
                if (species == null) continue;

                // Přidáme primární typ
                ElementalType primary = species.getPrimaryType();
                if (primary != null) {
                    types.add(primary.getName().toLowerCase());
                }

                // Přidáme sekundární typ pokud existuje
                ElementalType secondary = species.getSecondaryType();
                if (secondary != null) {
                    types.add(secondary.getName().toLowerCase());
                }

                System.out.println("[AutoBattle] Enemy types: " + types);
            }

        } catch (Exception e) {
            System.out.println("[AutoBattle] getEnemyTypes error: " + e.getMessage());
        }

        return types;
    }

    // =========================================================
    // MOVE SCORING — type effectiveness
    // =========================================================

    @Unique
    private double scoreMoveVsEnemyTypes(Object moveTile, List<String> enemyTypes) throws Exception {
        double score = 50;

        // Získáme move objekt
        Method getMoveMethod = null;
        for (Method method : moveTile.getClass().getMethods()) {
            if (method.getName().equalsIgnoreCase("getMove")) {
                getMoveMethod = method;
                break;
            }
        }

        if (getMoveMethod == null) return score;

        Object move = getMoveMethod.invoke(moveTile);
        if (move == null) return score;

        // PP kontrola
        try {
            Field ppField = move.getClass().getDeclaredField("pp");
            ppField.setAccessible(true);
            int pp = ppField.getInt(move);
            if (pp <= 0) return -9999;
            if (pp <= 3) score -= 30;
        } catch (Exception ignored) {}

        // Získáme typ moveu
        String moveType = "";
        try {
            for (Field field : move.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName().toLowerCase();
                if (fieldName.equals("elementaltype") || fieldName.equals("type")) {
                    Object typeObj = field.get(move);
                    if (typeObj instanceof ElementalType et) {
                        moveType = et.getName().toLowerCase();
                        break;
                    }
                }
            }
        } catch (Exception ignored) {}

        // Získáme power moveu
        int power = 0;
        try {
            for (Field field : move.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName().toLowerCase();
                if (fieldName.equals("power") || fieldName.equals("basePower") || fieldName.equals("damagebase")) {
                    Object val = field.get(move);
                    if (val instanceof Number n) {
                        power = n.intValue();
                        break;
                    }
                }
            }
        } catch (Exception ignored) {}

        // Power bonus
        score += power * 0.3;

        // Type effectiveness vs enemy types
        if (!moveType.isEmpty() && !enemyTypes.isEmpty()) {
            double effectiveness = getTypeEffectiveness(moveType, enemyTypes);
            score += effectiveness * 60;
            System.out.println("[AutoBattle] Move " + moveType + " vs " + enemyTypes + " effectiveness=" + effectiveness);
        }

        // Status move penalty
        try {
            Field idField = move.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            String moveId = String.valueOf(idField.get(move)).toLowerCase();

            if (moveId.contains("growl") || moveId.contains("tailwhip") || moveId.contains("leer")
                    || moveId.contains("protect") || moveId.contains("helpinghand")
                    || moveId.contains("splash") || moveId.contains("sandattack")) {
                score -= 40;
            }
        } catch (Exception ignored) {}

        return score;
    }

    // =========================================================
    // POKEMON SCORING — type matchup vs enemy
    // =========================================================

    @Unique
    private double scorePokemonVsEnemyTypes(Object pokemonTile, List<String> enemyTypes) throws Exception {
        double score = 50;

        // Fainted check
        try {
            Method isFaintedMethod = pokemonTile.getClass().getMethod("isFainted");
            boolean fainted = (boolean) isFaintedMethod.invoke(pokemonTile);
            if (fainted) return -9999;
        } catch (Exception ignored) {}

        // Already in battle check
        try {
            Method activeMethod = pokemonTile.getClass().getMethod("isCurrentlyInBattle");
            boolean active = (boolean) activeMethod.invoke(pokemonTile);
            if (active) return -9999;
        } catch (Exception ignored) {}

        if (enemyTypes.isEmpty()) return score;

        // Získáme Cobblemon Pokemon objekt
        try {
            Method getPokemonMethod = pokemonTile.getClass().getMethod("getPokemon");
            Object pokemon = getPokemonMethod.invoke(pokemonTile);
            if (pokemon == null) return score;

            // Získáme species a typy
            Method getSpeciesMethod = pokemon.getClass().getMethod("getSpecies");
            Object species = getSpeciesMethod.invoke(pokemon);
            if (species == null) return score;

            // Typy našeho Pokémona
            List<String> ourTypes = new ArrayList<>();

            try {
                Method getPrimaryType = species.getClass().getMethod("getPrimaryType");
                Object primaryType = getPrimaryType.invoke(species);
                if (primaryType instanceof ElementalType et) {
                    ourTypes.add(et.getName().toLowerCase());
                }
            } catch (Exception ignored) {}

            try {
                Method getSecondaryType = species.getClass().getMethod("getSecondaryType");
                Object secondaryType = getSecondaryType.invoke(species);
                if (secondaryType instanceof ElementalType et) {
                    ourTypes.add(et.getName().toLowerCase());
                }
            } catch (Exception ignored) {}

            // Skórujeme naše typy vs typy protivníka
            for (String ourType : ourTypes) {
                // Jak efektivní jsou naše STAB movey proti protivníkovi
                double offensiveScore = getTypeEffectiveness(ourType, enemyTypes);
                score += offensiveScore * 40;

                // Jak odolní jsme proti protivníkovým typům
                double defensiveScore = getTypeEffectiveness(enemyTypes.isEmpty() ? "" : enemyTypes.get(0), List.of(ourType));
                score -= defensiveScore * 30; // chceme být odolní = nízká effectiveness proti nám
            }

            System.out.println("[AutoBattle] Our types: " + ourTypes + " vs enemy: " + enemyTypes + " score=" + score);

        } catch (Exception e) {
            System.out.println("[AutoBattle] scorePokemon error: " + e.getMessage());
        }

        return score;
    }

    // =========================================================
    // TYPE EFFECTIVENESS TABLE
    // Returns multiplier: 2.0 = super effective, 0.5 = not very effective, 0.0 = immune
    // =========================================================

    @Unique
    private double getTypeEffectiveness(String attackType, List<String> defenderTypes) {
        double multiplier = 1.0;

        for (String defType : defenderTypes) {
            multiplier *= getSingleTypeEffectiveness(attackType, defType);
        }

        return multiplier;
    }

    @Unique
    private double getSingleTypeEffectiveness(String atk, String def) {
        return switch (atk) {
            case "normal"   -> switch (def) { case "rock", "steel" -> 0.5; case "ghost" -> 0.0; default -> 1.0; };
            case "fire"     -> switch (def) { case "fire", "water", "rock", "dragon" -> 0.5; case "grass", "ice", "bug", "steel" -> 2.0; default -> 1.0; };
            case "water"    -> switch (def) { case "water", "grass", "dragon" -> 0.5; case "fire", "ground", "rock" -> 2.0; default -> 1.0; };
            case "electric" -> switch (def) { case "electric", "grass", "dragon" -> 0.5; case "ground" -> 0.0; case "water", "flying" -> 2.0; default -> 1.0; };
            case "grass"    -> switch (def) { case "fire", "grass", "poison", "flying", "bug", "dragon", "steel" -> 0.5; case "water", "ground", "rock" -> 2.0; default -> 1.0; };
            case "ice"      -> switch (def) { case "water", "ice" -> 0.5; case "steel", "fire" -> 0.5; case "grass", "ground", "flying", "dragon" -> 2.0; default -> 1.0; };
            case "fighting" -> switch (def) { case "poison", "flying", "psychic", "bug", "fairy" -> 0.5; case "ghost" -> 0.0; case "normal", "ice", "rock", "dark", "steel" -> 2.0; default -> 1.0; };
            case "poison"   -> switch (def) { case "poison", "ground", "rock", "ghost" -> 0.5; case "steel" -> 0.0; case "grass", "fairy" -> 2.0; default -> 1.0; };
            case "ground"   -> switch (def) { case "grass", "bug" -> 0.5; case "flying" -> 0.0; case "fire", "electric", "poison", "rock", "steel" -> 2.0; default -> 1.0; };
            case "flying"   -> switch (def) { case "electric", "rock", "steel" -> 0.5; case "grass", "fighting", "bug" -> 2.0; default -> 1.0; };
            case "psychic"  -> switch (def) { case "psychic", "steel" -> 0.5; case "dark" -> 0.0; case "fighting", "poison" -> 2.0; default -> 1.0; };
            case "bug"      -> switch (def) { case "fire", "fighting", "flying", "ghost", "steel", "fairy" -> 0.5; case "grass", "psychic", "dark" -> 2.0; default -> 1.0; };
            case "rock"     -> switch (def) { case "fighting", "ground", "steel" -> 0.5; case "fire", "ice", "flying", "bug" -> 2.0; default -> 1.0; };
            case "ghost"    -> switch (def) { case "normal" -> 0.0; case "dark" -> 0.5; case "ghost", "psychic" -> 2.0; default -> 1.0; };
            case "dragon"   -> switch (def) { case "steel" -> 0.5; case "fairy" -> 0.0; case "dragon" -> 2.0; default -> 1.0; };
            case "dark"     -> switch (def) { case "fighting", "dark", "fairy" -> 0.5; case "ghost", "psychic" -> 2.0; default -> 1.0; };
            case "steel"    -> switch (def) { case "fire", "water", "electric", "steel" -> 0.5; case "ice", "rock", "fairy" -> 2.0; default -> 1.0; };
            case "fairy"    -> switch (def) { case "fire", "poison", "steel" -> 0.5; case "fighting", "dragon", "dark" -> 2.0; default -> 1.0; };
            default -> 1.0;
        };
    }

    @Unique
    private void simulateClick(ClickableWidget widget) {
        tickDelay = 0;
        widget.mouseClicked(widget.getX() + 1, widget.getY() + 1, 0);
        widget.mouseReleased(widget.getX() + 1, widget.getY() + 1, 0);
    }
}