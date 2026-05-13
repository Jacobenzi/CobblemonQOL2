package cz.tvoje.quiettools.movement;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.block.BlockState;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AutoJumpAssist {

    private static long lastJump = 0;

    public static void register() {

        ClientTickEvents.END_CLIENT_TICK.register(
                client -> tick()
        );
    }

    private static void tick() {

        MinecraftClient client =
                MinecraftClient.getInstance();

        ClientPlayerEntity player =
                client.player;

        if (player == null) {
            return;
        }

        // =====================================================
        // CONDITIONS
        // =====================================================

        if (
                !cz.tvoje.quiettools.ModSettings.autoJumpAssist
        ) {
            return;
        }

        if (!player.isOnGround()) {
            return;
        }

        if (!player.isSprinting()) {
            return;
        }

        if (player.isSneaking()) {
            return;
        }

        if (player.forwardSpeed <= 0) {
            return;
        }

        // =====================================================
        // COOLDOWN
        // =====================================================

        if (
                System.currentTimeMillis()
                        - lastJump
                        < 150
        ) {
            return;
        }

        // =====================================================
        // FORWARD CHECK
        // =====================================================

        Vec3d forward =
                player.getRotationVec(1.0f)
                        .normalize();

        double checkDistance = 0.42;

        double checkX =
                player.getX()
                        + forward.x * checkDistance;

        double checkY =
                player.getY() - 0.5;

        double checkZ =
                player.getZ()
                        + forward.z * checkDistance;

        BlockPos blockPos =
                BlockPos.ofFloored(
                        checkX,
                        checkY,
                        checkZ
                );

        BlockState state =
                player.getWorld()
                        .getBlockState(blockPos);

        // =====================================================
        // EDGE DETECTED
        // =====================================================

        if (state.isAir()) {

            player.jump();

            lastJump =
                    System.currentTimeMillis();
        }
    }
}