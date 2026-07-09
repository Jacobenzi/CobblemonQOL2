package cz.tvoje.quiettools;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class Harvester {

    public static int radius = 5;

    private static class PendingReplant {
        BlockPos pos;
        String seedName;
        boolean fromHarvest;

        PendingReplant(BlockPos pos, String seedName, boolean fromHarvest) {
            this.pos = pos;
            this.seedName = seedName;
            this.fromHarvest = fromHarvest;
        }
    }

    private static final List<PendingReplant> pendingReplants = new ArrayList<>();

    public static void harvest(ClientPlayerEntity player, World world) {
        processPendingReplants(player);
        BlockPos center = player.getBlockPos();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.add(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    FluidState fluidState = world.getFluidState(pos);
                    Identifier id = Registries.BLOCK.getId(block);

                    if (id == null) continue;

                    String namespace = id.getNamespace();
                    String path = id.getPath();

                    if (namespace.equals("cobblemon")) {
                        boolean isApricorn = path.contains("apricorn");
                        boolean isVivichoke = path.contains("vivichoke");
                        boolean isLeek = path.contains("medicinal_leek");

                        // BERRIES
                        boolean isVipBerry = false;
                        if (path.contains("grepa") && ModSettings.harvestGrepa) isVipBerry = true;
                        else if (path.contains("pomeg") && ModSettings.harvestPomeg) isVipBerry = true;
                        else if (path.contains("tamato") && ModSettings.harvestTamato) isVipBerry = true;
                        else if (path.contains("hondew") && ModSettings.harvestHondew) isVipBerry = true;
                        else if (path.contains("qualot") && ModSettings.harvestQualot) isVipBerry = true;
                        else if (path.contains("kelpsy") && ModSettings.harvestKelpsy) isVipBerry = true;

                        if (isApricorn && ModSettings.apricornEnabled && isRipe(state)) {
                            interactWithBlock(player, pos);
                        } else if (isVipBerry && ModSettings.berryEnabled && isRipe(state)) {
                            interactWithBlock(player, pos);
                        } else if (isVivichoke && ModSettings.vivichokeEnabled && isRipe(state)) {
                            destroyAndQueue(player, pos, "vivichoke", true);
                        } else if (isLeek && ModSettings.leekEnabled && isRipe(state)) {
                            destroyAndQueue(player, pos, "medicinal_leek", true);
                        }

                    } else if (namespace.equals("minecraft")) {
                        // Vivichoke — prázdná hlína
                        if (path.equals("farmland") && ModSettings.vivichokeEnabled) {
                            if (world.getBlockState(pos.up()).isAir()) {
                                queueIfMissing(pos, "vivichoke", false);
                            }
                        }
                        // Leek — slab pod vodou
                        if (path.contains("slab") && ModSettings.leekEnabled && fluidState.isOf(Fluids.WATER)) {
                            if (world.getBlockState(pos.up()).isAir()) {
                                // Pokud je leekOnlyReplant, sázíme jen místa kde jsme harvestli
                                if (!ModSettings.leekOnlyReplant) {
                                    queueIfMissing(pos, "medicinal_leek", false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================
    // Přímá interakce (apricorns, berries)
    // =========================================================

    private static void interactWithBlock(ClientPlayerEntity player, BlockPos pos) {
        player.networkHandler.sendPacket(
                new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                        new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false), 0)
        );
    }

    // =========================================================
    // Destroy + fronta pro replant (vivichoke, leek)
    // =========================================================

    private static void destroyAndQueue(ClientPlayerEntity player, BlockPos pos, String name, boolean fromHarvest) {
        player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
        player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
        pendingReplants.add(new PendingReplant(pos.down(), name, fromHarvest));
    }

    private static void queueIfMissing(BlockPos pos, String name, boolean fromHarvest) {
        for (PendingReplant r : pendingReplants) {
            if (r.pos.equals(pos)) return;
        }
        pendingReplants.add(new PendingReplant(pos, name, fromHarvest));
    }

    // =========================================================
    // Zpracování fronty replantů
    // =========================================================

    private static void processPendingReplants(ClientPlayerEntity player) {
        if (pendingReplants.isEmpty()) return;

        MinecraftClient mc = MinecraftClient.getInstance();

        for (int i = pendingReplants.size() - 1; i >= 0; i--) {
            PendingReplant r = pendingReplants.get(i);

            // leekOnlyReplant filtr — ignoruj leek místa která nebyla harvestnuta
            if (r.seedName.equals("medicinal_leek") && ModSettings.leekOnlyReplant && !r.fromHarvest) {
                pendingReplants.remove(i);
                continue;
            }

            if (r.pos.isWithinDistance(player.getPos(), radius + 2)) {
                int slot = getSeedSlot(player, r.seedName);
                if (slot != -1) {
                    int oldSlot = player.getInventory().selectedSlot;
                    player.getInventory().selectedSlot = slot;
                    player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));

                    if (r.seedName.equals("medicinal_leek")) {
                        // Otočíme hráče na cílový blok
                        double dx = r.pos.getX() + 0.5 - player.getX();
                        double dy = r.pos.getY() + 0.5 - (player.getY() + player.getEyeHeight(player.getPose()));
                        double dz = r.pos.getZ() + 0.5 - player.getZ();
                        double horizDist = Math.sqrt(dx * dx + dz * dz);

                        float yaw   = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
                        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, horizDist)));

                        player.setYaw(yaw);
                        player.setPitch(pitch);

                        mc.interactionManager.interactItem(player, Hand.MAIN_HAND);
                    }else {
                        // Vivichoke — kliknutí na blok
                        player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                                new BlockHitResult(Vec3d.ofCenter(r.pos), Direction.UP, r.pos, false), 0));
                    }

                    player.getInventory().selectedSlot = oldSlot;
                    player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot));
                    pendingReplants.remove(i);
                }
            }
        }
    }

    // =========================================================
    // Helpers
    // =========================================================

    private static boolean isRipe(BlockState state) {
        for (var property : state.getProperties()) {
            if ((property.getName().equals("stage") || property.getName().equals("age"))
                    && property instanceof IntProperty intProp) {
                return state.get(intProp) >= intProp.getValues().stream().mapToInt(i -> i).max().orElse(3);
            }
        }
        return false;
    }

    private static int getSeedSlot(ClientPlayerEntity player, String name) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).getItem().toString().contains(name)) return i;
        }
        return -1;
    }
    public static void harvestHeartyGrains(ClientPlayerEntity player, World world) {
        BlockPos playerPos = player.getBlockPos();
        int currentRadius = Math.min(radius, 10);

        // --- PŘIDÁNO: Dávkový limit pro bezpečnost ---
        int harvestedThisTick = 0;
        int maxPerTick = 20; // Zničí 4 rostliny najednou. Pokud by tě server vyhazoval, sniž to na 2 nebo 3.

        for (int x = -currentRadius; x <= currentRadius; x++) {
            for (int y = -currentRadius; y <= currentRadius; y++) {
                for (int z = -currentRadius; z <= currentRadius; z++) {

                    BlockPos targetPos = playerPos.add(x, y, z);
                    Block block = world.getBlockState(targetPos).getBlock();

                    // 1. Zkontrolujeme, jestli je to Hearty Grains
                    if (Registries.BLOCK.getId(block).toString().equals("cobblemon:hearty_grains")) {

                        // 2. CHYTRÁ KONTROLA: Zkontrolujeme, jestli je pod ní DALŠÍ blok Hearty Grains.
                        Block blockBelow = world.getBlockState(targetPos.down()).getBlock();

                        if (Registries.BLOCK.getId(blockBelow).toString().equals("cobblemon:hearty_grains")) {

                            MinecraftClient client = MinecraftClient.getInstance();
                            if (client.getNetworkHandler() != null) {

                                // 3. Pošleme serveru paket o "rozbití"
                                client.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                                        PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, targetPos, Direction.UP
                                ));
                                client.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, targetPos, Direction.UP
                                ));

                                // 4. Přidáme animaci ruky
                                player.swingHand(Hand.MAIN_HAND);

                                // 5. Zvýšíme počítadlo vytěžených rostlin v tomto cyklu
                                harvestedThisTick++;

                                // Pokud jsme dosáhli limitu (např. 4 rostliny), ukončíme hledání pro teď.
                                // V dalším pípnutí bota (za 10 ticků hry) se vytěží další várka.
                                if (harvestedThisTick >= maxPerTick) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}