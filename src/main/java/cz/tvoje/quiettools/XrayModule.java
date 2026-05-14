package cz.tvoje.quiettools;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class XrayModule {

    private static final Set<Block> targetBlocks = new HashSet<>();
    private static final Map<Block, String> blockOreIds = new HashMap<>();

    static {
        updateTargetBlocks();
    }

    public static void updateTargetBlocks() {
        targetBlocks.clear();
        blockOreIds.clear();

        // Vanilla ores
        if (ModSettings.xrayShowDiamond) {
            addOre(Blocks.DIAMOND_ORE, "diamond");
            addOre(Blocks.DEEPSLATE_DIAMOND_ORE, "diamond");
        }
        if (ModSettings.xrayShowEmerald) {
            addOre(Blocks.EMERALD_ORE, "emerald");
            addOre(Blocks.DEEPSLATE_EMERALD_ORE, "emerald");
        }
        if (ModSettings.xrayShowAncientDebris) {
            addOre(Blocks.ANCIENT_DEBRIS, "ancient_debris");
        }
        if (ModSettings.xrayShowGold) {
            addOre(Blocks.GOLD_ORE, "gold");
            addOre(Blocks.DEEPSLATE_GOLD_ORE, "gold");
        }
        if (ModSettings.xrayShowCopper) {
            addOre(Blocks.COPPER_ORE, "copper");
            addOre(Blocks.DEEPSLATE_COPPER_ORE, "copper");
        }
        if (ModSettings.xrayShowIron) {
            addOre(Blocks.IRON_ORE, "iron");
            addOre(Blocks.DEEPSLATE_IRON_ORE, "iron");
        }
        if (ModSettings.xrayShowLapis) {
            addOre(Blocks.LAPIS_ORE, "lapis");
            addOre(Blocks.DEEPSLATE_LAPIS_ORE, "lapis");
        }
        if (ModSettings.xrayShowRedstone) {
            addOre(Blocks.REDSTONE_ORE, "redstone");
            addOre(Blocks.DEEPSLATE_REDSTONE_ORE, "redstone");
        }
        if (ModSettings.xrayShowCoal) {
            addOre(Blocks.COAL_ORE, "coal");
            addOre(Blocks.DEEPSLATE_COAL_ORE, "coal");
        }

        addMythicMetalsOres();
    }

    private static void addOre(Block block, String oreId) {
        targetBlocks.add(block);
        blockOreIds.put(block, oreId);
    }

    private static void addMythicMetalsOres() {
        try {
            if (ModSettings.xrayShowKyber) {
                addBlockIfExists("mythicmetals", "kyber_ore", "kyber");
                addBlockIfExists("mythicmetals", "deepslate_kyber_ore", "kyber");
            }

            if (ModSettings.xrayShowOrichalcum) {
                addBlockIfExists("mythicmetals", "orichalcum_ore", "orichalcum");
                addBlockIfExists("mythicmetals", "deepslate_orichalcum_ore", "orichalcum");
            }

            if (ModSettings.xrayShowKalimite) {
                addBlockIfExists("mythicmetals", "kalimite_ore", "kalimite");
                addBlockIfExists("mythicmetals", "deepslate_kalimite_ore", "kalimite");
            }

            if (ModSettings.xrayShowMalachite) {
                addBlockIfExists("mythicmetals", "malachite_ore", "malachite");
                addBlockIfExists("mythicmetals", "deepslate_malachite_ore", "malachite");
            }

            if (ModSettings.xrayShowTitanium) {
                addBlockIfExists("mythicmetals", "titanium_ore", "titanium");
                addBlockIfExists("mythicmetals", "deepslate_titanium_ore", "titanium");
            }

            if (ModSettings.xrayShowAdamantite) {
                addBlockIfExists("mythicmetals", "adamantite_ore", "adamantite");
                addBlockIfExists("mythicmetals", "deepslate_adamantite_ore", "adamantite");
            }

            if (ModSettings.xrayShowMithril) {
                addBlockIfExists("mythicmetals", "mithril_ore", "mithril");
                addBlockIfExists("mythicmetals", "deepslate_mithril_ore", "mithril");
            }

            if (ModSettings.xrayShowPlatinum) {
                addBlockIfExists("mythicmetals", "platinum_ore", "platinum");
                addBlockIfExists("mythicmetals", "deepslate_platinum_ore", "platinum");
            }

            if (ModSettings.xrayShowSilver) {
                addBlockIfExists("mythicmetals", "silver_ore", "silver");
                addBlockIfExists("mythicmetals", "deepslate_silver_ore", "silver");
            }

            if (ModSettings.xrayShowBanglum) {
                addBlockIfExists("mythicmetals", "banglum_ore", "banglum");
                addBlockIfExists("mythicmetals", "deepslate_banglum_ore", "banglum");
            }

            if (ModSettings.xrayShowRunite) {
                addBlockIfExists("mythicmetals", "runite_ore", "runite");
                addBlockIfExists("mythicmetals", "deepslate_runite_ore", "runite");
            }

            if (ModSettings.xrayShowCarmot) {
                addBlockIfExists("mythicmetals", "carmot_ore", "carmot");
                addBlockIfExists("mythicmetals", "deepslate_carmot_ore", "carmot");
            }
        } catch (Exception e) {
            // Mythic Metals není nainstalovaný
        }
    }

    private static void addBlockIfExists(String namespace, String blockName, String oreId) {
        try {
            Identifier id = new Identifier(namespace, blockName);
            Block block = Registries.BLOCK.get(id);
            if (block != null && block != Blocks.AIR) {
                addOre(block, oreId);
            }
        } catch (Exception e) {
            // Blok neexistuje
        }
    }

    public static void scanAndRender(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || !ModSettings.xrayEnabled) {
            return;
        }

        World world = client.world;
        BlockPos playerPos = client.player.getBlockPos();
        int radius = Math.min(ModSettings.xrayRadius, 64);

        for (int x = playerPos.getX() - radius; x <= playerPos.getX() + radius; x++) {
            for (int y = playerPos.getY() - radius; y <= playerPos.getY() + radius; y++) {
                for (int z = playerPos.getZ() - radius; z <= playerPos.getZ() + radius; z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    Block block = world.getBlockState(blockPos).getBlock();

                    if (targetBlocks.contains(block)) {
                        renderOreBox(context, blockPos, block);
                    }
                }
            }
        }
    }

    private static void renderOreBox(WorldRenderContext context, BlockPos pos, Block block) {
        MinecraftClient client = MinecraftClient.getInstance();
        Vec3d cameraPos = context.camera().getPos();

        double x = pos.getX() - cameraPos.x;
        double y = pos.getY() - cameraPos.y;
        double z = pos.getZ() - cameraPos.z;

        Box box = new Box(x, y, z, x + 1, y + 1, z + 1);
        int color = getOreColor(block);

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        float rf = r / 255f;
        float gf = g / 255f;
        float bf = b / 255f;

        VertexConsumerProvider.Immediate consumers = client.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLines());

        WorldRenderer.drawBox(context.matrixStack(), buffer, box, rf, gf, bf, 1.0f);
        consumers.draw();
    }

    private static int getOreColor(Block block) {
        String oreId = blockOreIds.getOrDefault(block, "");

        // Vanilla ores
        if (oreId.equals("diamond")) return rgb(ModSettings.xrayDiamondR, ModSettings.xrayDiamondG, ModSettings.xrayDiamondB);
        if (oreId.equals("emerald")) return rgb(ModSettings.xrayEmeraldR, ModSettings.xrayEmeraldG, ModSettings.xrayEmeraldB);
        if (oreId.equals("ancient_debris")) return rgb(ModSettings.xrayAncientDebrisR, ModSettings.xrayAncientDebrisG, ModSettings.xrayAncientDebrisB);
        if (oreId.equals("gold")) return rgb(ModSettings.xrayGoldR, ModSettings.xrayGoldG, ModSettings.xrayGoldB);
        if (oreId.equals("copper")) return rgb(ModSettings.xrayCopperR, ModSettings.xrayCopperG, ModSettings.xrayCopperB);
        if (oreId.equals("iron")) return rgb(ModSettings.xrayIronR, ModSettings.xrayIronG, ModSettings.xrayIronB);
        if (oreId.equals("lapis")) return rgb(ModSettings.xrayLapisR, ModSettings.xrayLapisG, ModSettings.xrayLapisB);
        if (oreId.equals("redstone")) return rgb(ModSettings.xrayRedstoneR, ModSettings.xrayRedstoneG, ModSettings.xrayRedstoneB);
        if (oreId.equals("coal")) return rgb(ModSettings.xrayCoalR, ModSettings.xrayCoalG, ModSettings.xrayCoalB);

        // Mythic Metals ores
        if (oreId.equals("kyber")) return rgb(ModSettings.xrayKyberR, ModSettings.xrayKyberG, ModSettings.xrayKyberB);
        if (oreId.equals("orichalcum")) return rgb(ModSettings.xrayOrichalcumR, ModSettings.xrayOrichalcumG, ModSettings.xrayOrichalcumB);
        if (oreId.equals("kalimite")) return rgb(ModSettings.xrayKalimiteR, ModSettings.xrayKalimiteG, ModSettings.xrayKalimiteB);
        if (oreId.equals("malachite")) return rgb(ModSettings.xrayMalachiteR, ModSettings.xrayMalachiteG, ModSettings.xrayMalachiteB);
        if (oreId.equals("titanium")) return rgb(ModSettings.xrayTitaniumR, ModSettings.xrayTitaniumG, ModSettings.xrayTitaniumB);
        if (oreId.equals("adamantite")) return rgb(ModSettings.xrayAdamantiteR, ModSettings.xrayAdamantiteG, ModSettings.xrayAdamantiteB);
        if (oreId.equals("mithril")) return rgb(ModSettings.xrayMithrilR, ModSettings.xrayMithrilG, ModSettings.xrayMithrilB);
        if (oreId.equals("platinum")) return rgb(ModSettings.xrayPlatinumR, ModSettings.xrayPlatinumG, ModSettings.xrayPlatinumB);
        if (oreId.equals("silver")) return rgb(ModSettings.xraySilverR, ModSettings.xraySilverG, ModSettings.xraySilverB);
        if (oreId.equals("banglum")) return rgb(ModSettings.xrayBanglumR, ModSettings.xrayBanglumG, ModSettings.xrayBanglumB);
        if (oreId.equals("runite")) return rgb(ModSettings.xrayRuniteR, ModSettings.xrayRuniteG, ModSettings.xrayRuniteB);

        return 0xFFFFFF;
    }

    private static int rgb(int r, int g, int b) {
        return (0xFF << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}