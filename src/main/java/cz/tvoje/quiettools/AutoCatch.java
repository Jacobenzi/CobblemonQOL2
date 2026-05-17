package cz.tvoje.quiettools;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.process.ICustomGoalProcess;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AutoCatch {

    private enum State { IDLE, PATROL, CHASE, CATCH }

    private static State state = State.IDLE;

    private static BlockPos patrolCenter  = null;
    private static BlockPos patrolTarget  = null;
    private static PokemonEntity targetEntity = null;

    private static long lastThrow        = 0;
    private static final long THROW_COOLDOWN = 1200;

    private static long patrolArrivalTime = 0;
    private static final long PATROL_WAIT = 1000;

    private static int patrolAngle = 0;

    // =========================================================
    // REGISTER
    // =========================================================

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(AutoCatch::tick);
    }

    // =========================================================
    // MAIN TICK
    // =========================================================

    private static void tick(MinecraftClient client) {

        if (client.player == null || client.world == null) return;

        if (!ModSettings.autoCatchEnabled) {
            if (state != State.IDLE) {
                stopBaritone();
                state = State.IDLE;
            }
            return;
        }

        ClientPlayerEntity player = client.player;
        IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        ICustomGoalProcess goalProcess = baritone.getCustomGoalProcess();

        if (patrolCenter == null) {
            patrolCenter = player.getBlockPos();
        }

        PokemonEntity found = findTarget(client);
        if (found != null && state != State.CATCH) {
            targetEntity = found;
            state = State.CHASE;
        }

        switch (state) {
            case PATROL -> tickPatrol(player, goalProcess);
            case CHASE  -> tickChase(player, goalProcess);
            case CATCH  -> tickCatch(player, client);
            case IDLE   -> {}
        }
    }

    // =========================================================
    // PATROL
    // =========================================================

    private static void tickPatrol(ClientPlayerEntity player, ICustomGoalProcess goalProcess) {

        if (patrolTarget == null || hasArrived(player, patrolTarget)) {

            if (patrolTarget != null && System.currentTimeMillis() - patrolArrivalTime < PATROL_WAIT) {
                return;
            }

            patrolAngle = (patrolAngle + 60) % 360;
            double rad = Math.toRadians(patrolAngle);
            int r = ModSettings.autoCatchPatrolRadius;

            patrolTarget = patrolCenter.add(
                    (int)(Math.sin(rad) * r),
                    0,
                    (int)(Math.cos(rad) * r)
            );

            patrolArrivalTime = System.currentTimeMillis();
            goalProcess.setGoalAndPath(new GoalBlock(patrolTarget));
        }
    }

    // =========================================================
    // CHASE — při vodním pokémonovi jde k hladině
    // =========================================================

    private static void tickChase(ClientPlayerEntity player, ICustomGoalProcess goalProcess) {

        if (targetEntity == null || targetEntity.isRemoved()) {
            targetEntity = null;
            state = State.PATROL;
            return;
        }

        Vec3d playerPos  = player.getPos();
        Vec3d pokemonPos = targetEntity.getPos();
        double horizDist = Math.sqrt(
                Math.pow(playerPos.x - pokemonPos.x, 2) +
                        Math.pow(playerPos.z - pokemonPos.z, 2)
        );

        if (horizDist <= 4.0) {
            stopBaritone();
            state = State.CATCH;
            return;
        }

        if (player.age % 20 == 0) {
            BlockPos targetPos = targetEntity.getBlockPos();
            BlockPos goalPos   = isWaterBlock(targetPos) ? findWaterSurface(targetPos) : targetPos;
            goalProcess.setGoalAndPath(new GoalNear(goalPos, 3));
        }
    }

    // =========================================================
    // CATCH
    // =========================================================

    private static void tickCatch(ClientPlayerEntity player, MinecraftClient client) {

        if (targetEntity == null || targetEntity.isRemoved()) {
            targetEntity = null;
            state = State.PATROL;
            return;
        }

        if (System.currentTimeMillis() - lastThrow < THROW_COOLDOWN) return;

        Vec3d playerPos  = player.getPos();
        Vec3d pokemonPos = targetEntity.getPos();
        double horizDist = Math.sqrt(
                Math.pow(playerPos.x - pokemonPos.x, 2) +
                        Math.pow(playerPos.z - pokemonPos.z, 2)
        );

        if (horizDist > 5.0) {
            state = State.CHASE;
            return;
        }

        int ballSlot = findBallSlot(player);

        if (ballSlot == -1) {
            ModSettings.autoCatchEnabled = false;
            state = State.IDLE;
            if (client.player != null) {
                client.player.sendMessage(
                        Text.literal("§c[GodClient] Dosly Pokebally! AutoCatch vypnut."), true
                );
            }
            return;
        }

        int previousSlot = player.getInventory().selectedSlot;
        player.getInventory().selectedSlot = ballSlot;
        player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(ballSlot));

        // Vypočítej rotaci přesně na střed pokémona
        double dx = pokemonPos.x - player.getX();
        double dy = pokemonPos.y + targetEntity.getHeight() / 2.0
                - (player.getY() + player.getEyeHeight(player.getPose()));
        double dz = pokemonPos.z - player.getZ();
        double horizDist2 = Math.sqrt(dx * dx + dz * dz);

        float yaw   = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, horizDist2)));

        // 1. Nastav rotaci lokálně i na serveru
        player.setYaw(yaw);
        player.setPitch(pitch);
        player.networkHandler.sendPacket(
                new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, player.isOnGround())
        );

        // 2. Nastav crosshair target na pokémona
        client.crosshairTarget = new net.minecraft.util.hit.EntityHitResult(targetEntity);

        // 3. Hoď ball jako projektil — stejně jako snowball
        player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, yaw, pitch));

        // 4. Animace ruky
        player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        player.getInventory().selectedSlot = previousSlot;
        player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));

        player.getInventory().selectedSlot = previousSlot;
        player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));

        lastThrow = System.currentTimeMillis();
        targetEntity = null;
        state = State.PATROL;
    }

    // =========================================================
    // HLEDÁNÍ CÍLOVÉHO POKÉMONA
    // =========================================================

    private static PokemonEntity findTarget(MinecraftClient client) {

        if (client.player == null || client.world == null) return null;
        if (ModSettings.searchEntries.isEmpty()) return null;

        PokemonEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (PokemonEntity entity : client.world.getEntitiesByClass(
                PokemonEntity.class,
                client.player.getBoundingBox().expand(ModSettings.espRadius),
                e -> true
        )) {
            if (entity.getPokemon() == null || entity.getPokemon().getSpecies() == null) continue;

            String name = entity.getPokemon().getSpecies().getName().toLowerCase();

            for (SearchEntry entry : ModSettings.searchEntries) {
                if (!entry.enabled) continue;
                if (name.equals(entry.name)) {
                    double dist = client.player.getPos().distanceTo(entity.getPos());
                    if (dist < closestDist) {
                        closestDist = dist;
                        closest = entity;
                    }
                }
            }
        }

        return closest;
    }

    // =========================================================
    // NAJDI BALL V HOTBARU
    // =========================================================

    private static int findBallSlot(ClientPlayerEntity player) {
        PlayerInventory inv = player.getInventory();
        String ballItem = ModSettings.autoCatchBallItem;

        for (int i = 0; i < 9; i++) {
            String itemId = inv.getStack(i).getItem().toString();
            if (itemId.equals(ballItem) || itemId.contains(ballItem.replace("cobblemon:", ""))) {
                return i;
            }
        }
        return -1;
    }

    // =========================================================
    // VODA HELPERS
    // =========================================================

    private static boolean isWaterBlock(BlockPos pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return false;
        var block = client.world.getBlockState(pos).getBlock();
        return block == Blocks.WATER || block == Blocks.BUBBLE_COLUMN;
    }

    private static BlockPos findWaterSurface(BlockPos pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return pos;

        BlockPos check = pos;
        for (int i = 0; i < 64; i++) {
            check = check.up();
            if (client.world.getBlockState(check).isAir()) {
                return check;
            }
        }
        return pos;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private static boolean hasArrived(ClientPlayerEntity player, BlockPos target) {
        return player.getBlockPos().isWithinDistance(target, 2.5);
    }

    private static void stopBaritone() {
        BaritoneAPI.getProvider().getPrimaryBaritone()
                .getPathingBehavior().cancelEverything();
    }

    public static void resetPatrolCenter() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            patrolCenter  = client.player.getBlockPos();
            patrolTarget  = null;
            patrolAngle   = 0;
        }
    }
}