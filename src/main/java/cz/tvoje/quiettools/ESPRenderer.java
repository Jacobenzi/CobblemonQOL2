package cz.tvoje.quiettools;

import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class ESPRenderer {

    public static void register() {

        WorldRenderEvents.LAST.register(
                ESPRenderer::render
        );
    }

    private static void render(WorldRenderContext context) {

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null) {
            return;
        }

        Vec3d camera = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();

        VertexConsumerProvider.Immediate consumers =
                client.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer buffer =
                consumers.getBuffer(RenderLayer.getLines());

        RenderSystem.depthMask(true);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        // =========================================================
        // POKEMON ESP
        // =========================================================

        for (PokemonEntity pokemonEntity :
                client.world.getEntitiesByClass(
                        PokemonEntity.class,
                        client.player.getBoundingBox().expand(ModSettings.espRadius),
                        entity -> true
                )) {

            Pokemon pokemon = pokemonEntity.getPokemon();

            if (pokemon == null || pokemon.getSpecies() == null || pokemon.getSpecies().getName() == null) continue;

            float r = 1f, g = 1f, b = 1f;
            boolean shouldRender = false;
            boolean shouldGlow = false;

            if (ModSettings.shinyLegendaryEspEnabled && (pokemon.getShiny() || isLegendary(pokemon))) {
                r = ModSettings.shinyR / 255f; g = ModSettings.shinyG / 255f; b = ModSettings.shinyB / 255f;
                shouldRender = true; shouldGlow = true;
            } else if (ModSettings.blisseyEspEnabled && isTargetBlissey(pokemon)) {
                r = ModSettings.blisseyR / 255f; g = ModSettings.blisseyG / 255f; b = ModSettings.blisseyB / 255f;
                shouldRender = true;
            } else if (ModSettings.ivScannerEnabled && countPerfectIVs(pokemon) >= 3) {
                r = ModSettings.ivR / 255f; g = ModSettings.ivG / 255f; b = ModSettings.ivB / 255f;
                shouldRender = true;
            }

            if (pokemonEntity.isGlowing() != shouldGlow) {
                pokemonEntity.setGlowing(shouldGlow);
            }

            if (!shouldRender) continue;

            Box box = pokemonEntity.getBoundingBox().offset(-camera.x, -camera.y, -camera.z);

            WorldRenderer.drawBox(matrices, buffer, box, r, g, b, 1.0f);
            drawTracer(matrices, buffer, pokemonEntity, camera, r, g, b);
        }

        // =========================================================
        // CUSTOM POKEMON ESP
        // =========================================================

        if (ModSettings.customPokemonEspEnabled && !ModSettings.customPokemonName.isEmpty()) {

            ModSettings.customPokemonFound = false;

            for (PokemonEntity pokemonEntity :
                    client.world.getEntitiesByClass(
                            PokemonEntity.class,
                            client.player.getBoundingBox().expand(ModSettings.customPokemonRadius),
                            entity -> true
                    )) {

                Pokemon pokemon = pokemonEntity.getPokemon();

                if (pokemon == null || pokemon.getSpecies() == null || pokemon.getSpecies().getName() == null) continue;

                String pokemonName = pokemon.getSpecies().getName().toLowerCase();
                String searchName = ModSettings.customPokemonName.toLowerCase().trim();

                if (pokemonName.contains(searchName)) {
                    ModSettings.customPokemonFound = true;

                    float r = ModSettings.customPokemonR / 255f;
                    float g = ModSettings.customPokemonG / 255f;
                    float b = ModSettings.customPokemonB / 255f;

                    Box box = pokemonEntity.getBoundingBox().offset(-camera.x, -camera.y, -camera.z);

                    WorldRenderer.drawBox(matrices, buffer, box, r, g, b, 1.0f);
                    drawTracer(matrices, buffer, pokemonEntity, camera, r, g, b);
                }
            }
        }

        // =========================================================
        // BERRY DROP ESP — zobrazí se jen když je Berry Harvest aktivní
        // =========================================================

        if (ModSettings.berryEnabled) {

            for (ItemEntity itemEntity :
                    client.world.getEntitiesByClass(
                            ItemEntity.class,
                            client.player.getBoundingBox().expand(ModSettings.espRadius),
                            entity -> true
                    )) {

                String itemName = itemEntity.getStack().getItem().toString().toLowerCase();

                float r = 1f, g = 0.5f, b = 0f; // výchozí oranžová pro berry
                boolean showEsp = false;

                if (itemName.contains("grepa")  && ModSettings.harvestGrepa)  showEsp = true;
                if (itemName.contains("pomeg")  && ModSettings.harvestPomeg)  showEsp = true;
                if (itemName.contains("tamato") && ModSettings.harvestTamato) showEsp = true;
                if (itemName.contains("hondew") && ModSettings.harvestHondew) showEsp = true;
                if (itemName.contains("qualot") && ModSettings.harvestQualot) showEsp = true;
                if (itemName.contains("kelpsy") && ModSettings.harvestKelpsy) showEsp = true;

                if (!showEsp) continue;

                Box box = itemEntity.getBoundingBox().offset(-camera.x, -camera.y, -camera.z);

                WorldRenderer.drawBox(matrices, buffer, box, r, g, b, 1.0f);
                drawTracer(matrices, buffer, itemEntity, camera, r, g, b);
            }
        }

        consumers.draw();

        // =========================================================
        // X-RAY RENDERING
        // =========================================================

        if (ModSettings.xrayEnabled) {
            XrayModule.scanAndRender(context);
        }
    }

    // =============================================================
    // BLISSEY CHECK
    // =============================================================

    private static boolean isTargetBlissey(Pokemon pokemon) {

        return pokemon.getSpecies()
                .getName()
                .equalsIgnoreCase("blissey")

                && pokemon.getLevel() >= 80;
    }

    // =============================================================
    // LEGENDARY CHECK
    // =============================================================

    private static boolean isLegendary(Pokemon pokemon) {

        boolean hasTag = false;

        try {

            hasTag =
                    pokemon.getSpecies()
                            .getLabels()
                            .contains("legendary")

                            ||

                            pokemon.getSpecies()
                                    .getLabels()
                                    .contains("mythical")

                            ||

                            pokemon.getSpecies()
                                    .getLabels()
                                    .contains("ultra_beast");

        } catch (Exception ignored) {
        }

        String name =
                pokemon.getSpecies()
                        .getName()
                        .toLowerCase();

        boolean hardcoded =
                name.equals("mewtwo")
                        || name.equals("lugia")
                        || name.equals("ho-oh")
                        || name.equals("rayquaza")
                        || name.equals("kyogre")
                        || name.equals("groudon")
                        || name.equals("giratina")
                        || name.equals("dialga")
                        || name.equals("palkia")
                        || name.equals("mew")
                        || name.equals("celebi")
                        || name.equals("arceus")
                        || name.equals("zacian")
                        || name.equals("zamazenta")
                        || name.equals("eternatus");

        return hasTag || hardcoded;
    }

    // =============================================================
    // PERFECT IV COUNT
    // =============================================================

    private static int countPerfectIVs(Pokemon pokemon) {

        int perfect = 0;

        if (pokemon.getIvs().get(Stats.HP) >= 31)           perfect++;
        if (pokemon.getIvs().get(Stats.ATTACK) >= 31)       perfect++;
        if (pokemon.getIvs().get(Stats.DEFENCE) >= 31)      perfect++;
        if (pokemon.getIvs().get(Stats.SPECIAL_ATTACK) >= 31)  perfect++;
        if (pokemon.getIvs().get(Stats.SPECIAL_DEFENCE) >= 31) perfect++;
        if (pokemon.getIvs().get(Stats.SPEED) >= 31)        perfect++;

        return perfect;
    }

    // =============================================================
    // TRACER
    // =============================================================

    // =============================================================
    // TRACER (S VYHLAZOVÁNÍM A PODPOROU FIRST/THIRD PERSON)
    // =============================================================

    private static void drawTracer(
            MatrixStack matrices,
            VertexConsumer buffer,
            Entity entity,
            Vec3d camera,
            float r,
            float g,
            float b
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        float tickDelta = client.getRenderTickCounter().getTickDelta(true);

        Vec3d startRaw;

        // Zjistíme, jestli hráč kouká z první osoby (First-Person)
        if (client.options.getPerspective().isFirstPerson()) {
            // Z první osoby: Kreslíme čáru přímo z crosshairu (z kamery)
            // Přidáme malinký posun (0.1) dopředu po směru tvého pohledu, aby se čára nebugovala o samotnou kameru
            Vec3d lookVec = client.player.getRotationVec(tickDelta);
            startRaw = camera.add(lookVec.multiply(0.1));
        } else {
            // Ze třetí osoby: Kreslíme čáru z poloviny těla (hrudník)
            startRaw = client.player.getLerpedPos(tickDelta).add(0, client.player.getHeight() * 0.5, 0);
        }

        // Odečteme pozici kamery pro oba případy
        Vec3d start = startRaw.subtract(camera);

        // Konec čáry (cíl = pokémon) zůstává stejný
        Vec3d targetPos = entity.getLerpedPos(tickDelta);
        Vec3d end = targetPos
                .add(0, entity.getHeight() * 0.5, 0)
                .subtract(camera);

        Vec3d dir = end.subtract(start).normalize();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        buffer.vertex(matrix, (float) start.x, (float) start.y, (float) start.z)
                .color(r, g, b, 1.0f)
                .normal((float) dir.x, (float) dir.y, (float) dir.z);

        buffer.vertex(matrix, (float) end.x, (float) end.y, (float) end.z)
                .color(r, g, b, 1.0f)
                .normal((float) dir.x, (float) dir.y, (float) dir.z);
    }
}