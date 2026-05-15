package cz.tvoje.quiettools.movement;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.math.Vec3d;

public class AutoJumpAssist {

    private static long lastJump = 0;

    public static void register() {

        // ZMĚNA 1: Spouštíme na ZAČÁTKU ticku!
        // Zkontrolujeme hranu dřív, než nás engine hry reálně posune do propasti.
        ClientTickEvents.START_CLIENT_TICK.register(
                client -> tick()
        );
    }

    private static void tick() {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null) {
            return;
        }

        // =====================================================
        // CONDITIONS
        // =====================================================

        if (!cz.tvoje.quiettools.ModSettings.autoJumpAssist) return;
        if (!player.isOnGround()) return;
        if (!player.isSprinting()) return;
        if (player.isSneaking() || player.isSwimming() || player.isCrawling()) return;
        if (player.forwardSpeed <= 0) return;

        // =====================================================
        // COOLDOWN
        // =====================================================

        if (System.currentTimeMillis() - lastJump < 150) {
            return;
        }

        // =====================================================
        // TRUE 4-BLOCK PIXEL-PERFECT CHECK
        // =====================================================

        // Získáme přesný vektor rychlosti, kterým tě engine zrovna hýbe
        Vec3d vel = player.getVelocity();

        // Pokud se prakticky nehýbeme, neřešíme hrany
        if (Math.abs(vel.x) < 0.05 && Math.abs(vel.z) < 0.05) return;

        // Hitbox postavy posuneme naprosto PŘESNĚ tam, kde bude tvá postava v dalším ticku.
        // Žádné hádání vzdálenosti, použijeme čistou fyziku hry.
        Box futureBox = player.getBoundingBox().offset(vel.x, -0.05, vel.z);

        Iterable<VoxelShape> collisions = player.getWorld().getBlockCollisions(player, futureBox);

        boolean hasFloor = false;
        for (VoxelShape shape : collisions) {
            if (!shape.isEmpty()) {
                hasFloor = true;
                break;
            }
        }

        // =====================================================
        // JUMP TRIGGER
        // =====================================================

        // Pokud v dalším ticku opravdu nezbude pod naším přesným stínem ani pixel opory
        if (!hasFloor) {
            player.jump();
            lastJump = System.currentTimeMillis();
        }
    }
}