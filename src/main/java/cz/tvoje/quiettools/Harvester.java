package cz.tvoje.quiettools;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
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

    private static final List<BlockPos> pendingReplants = new ArrayList<>();

    public static void harvest(ClientPlayerEntity player, World world) {

        processPendingReplants(player);

        BlockPos center = player.getBlockPos();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    BlockPos pos = center.add(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    Identifier id = Registries.BLOCK.getId(block);

                    if (id != null) {
                        String namespace = id.getNamespace();
                        String path = id.getPath();

                        if (namespace.equals("cobblemon")) {
                            boolean isApricorn = path.contains("apricorn");
                            boolean isVivichoke = path.contains("vivichoke");

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
                                destroyAndReplant(player, pos);
                            }
                        }

                        else if (namespace.equals("minecraft") && path.equals("farmland")) {
                            if (ModSettings.vivichokeEnabled) {
                                BlockPos upPos = pos.up();
                                if (world.getBlockState(upPos).isAir()) {
                                    plantSeedOnFarmland(player, pos);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isRipe(BlockState state) {
        for (var property : state.getProperties()) {
            if ((property.getName().equals("stage") || property.getName().equals("age"))
                    && property instanceof IntProperty intProp) {
                int current = state.get(intProp);
                int max = intProp.getValues().stream().mapToInt(i -> i).max().orElse(3);
                return current >= max;
            }
        }
        return false;
    }

    private static void interactWithBlock(ClientPlayerEntity player, BlockPos pos) {
        player.networkHandler.sendPacket(
                new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false), 0)
        );
    }

    // =========================================================
    // OPRAVENO: přidán UpdateSelectedSlotC2SPacket aby server věděl o změně slotu
    // =========================================================

    private static void destroyAndReplant(ClientPlayerEntity player, BlockPos pos) {
        player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
        player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP));

        int seedSlot = getSeedSlot(player);
        BlockPos dirtPos = pos.down();

        if (seedSlot != -1) {
            int previousSlot = player.getInventory().selectedSlot;

            player.getInventory().selectedSlot = seedSlot;
            player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(seedSlot));

            player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(
                    Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(dirtPos), Direction.UP, dirtPos, false), 0
            ));

            player.getInventory().selectedSlot = previousSlot;
            player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
        } else {
            if (!pendingReplants.contains(dirtPos)) {
                pendingReplants.add(dirtPos);
            }
        }
    }

    private static void plantSeedOnFarmland(ClientPlayerEntity player, BlockPos dirtPos) {
        int seedSlot = getSeedSlot(player);

        if (seedSlot != -1) {
            int previousSlot = player.getInventory().selectedSlot;

            player.getInventory().selectedSlot = seedSlot;
            player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(seedSlot));

            player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(
                    Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(dirtPos), Direction.UP, dirtPos, false), 0
            ));

            player.getInventory().selectedSlot = previousSlot;
            player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
        }
    }

    private static void processPendingReplants(ClientPlayerEntity player) {
        if (pendingReplants.isEmpty()) return;

        int seedSlot = getSeedSlot(player);
        if (seedSlot == -1) return;

        int previousSlot = player.getInventory().selectedSlot;

        for (int i = pendingReplants.size() - 1; i >= 0; i--) {
            BlockPos dirtPos = pendingReplants.get(i);

            if (dirtPos.isWithinDistance(player.getPos(), radius + 2)) {
                player.getInventory().selectedSlot = seedSlot;
                player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(seedSlot));

                player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(
                        Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(dirtPos), Direction.UP, dirtPos, false), 0
                ));
                pendingReplants.remove(i);
            } else {
                pendingReplants.remove(i);
            }
        }

        player.getInventory().selectedSlot = previousSlot;
        player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
    }

    private static int getSeedSlot(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).getItem().toString().contains("vivichoke")) {
                return i;
            }
        }
        return -1;
    }
}