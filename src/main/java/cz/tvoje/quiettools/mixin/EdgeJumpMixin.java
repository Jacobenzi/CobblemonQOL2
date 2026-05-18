package cz.tvoje.quiettools.mixin;

import cz.tvoje.quiettools.ModSettings;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class EdgeJumpMixin {

    // Speciální proměnná, která nám pamatuje, že máme skočit "hned jak to půjde"
    @Unique
    private boolean jumpNextTick = false;

    // KROK 1: Odraz a aplikace Sprint Boostu na úplném začátku ticku
    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void onTickMovement(CallbackInfo ci) {
        if (this.jumpNextTick && ModSettings.edgeJumpEnabled) {
            this.jumpNextTick = false;

            ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
            // Zavoláme normální skok na začátku ticku -> hra to zpracuje úplně stejně,
            // jako bys na klávesnici fyzicky zmáčkl mezerník = dostaneš maximální boost!
            player.jump();
        }
    }

    // KROK 2: Detekce hrany pomocí Vanilla Sneak Enginu
    @Inject(method = "move", at = @At("HEAD"))
    public void onMove(MovementType type, Vec3d movement, CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        if (!ModSettings.edgeJumpEnabled) return;
        if (!player.isOnGround() || player.isSneaking() || player.input.jumping) return;
        if (movement.y > 0) return;

        double moveX = movement.x;
        double moveZ = movement.z;

        double threshold = 0.05;
        double stepX = Math.signum(moveX) * threshold;
        double stepZ = Math.signum(moveZ) * threshold;
        float stepHeight = player.getStepHeight();

        // Posouváme hitbox a ptáme se hry, kdy spadneme
        while (moveX != 0.0 && isDropAhead(player, moveX, 0.0, stepHeight)) {
            if (Math.abs(moveX) <= threshold) { moveX = 0.0; break; }
            moveX -= stepX;
        }

        while (moveZ != 0.0 && isDropAhead(player, 0.0, moveZ, stepHeight)) {
            if (Math.abs(moveZ) <= threshold) { moveZ = 0.0; break; }
            moveZ -= stepZ;
        }

        while (moveX != 0.0 && moveZ != 0.0 && isDropAhead(player, moveX, moveZ, stepHeight)) {
            moveX = (Math.abs(moveX) <= threshold) ? 0.0 : moveX - stepX;
            moveZ = (Math.abs(moveZ) <= threshold) ? 0.0 : moveZ - stepZ;
        }

        // Pokud by nás normální Shift zastavil, znamená to konec bloku!
        if (moveX != movement.x || moveZ != movement.z) {
            // NESKÁČEME HNED! Necháme postavu dojet tento tick až na samotný okraj propasti
            // a připravíme si skok na hned další snímek.
            this.jumpNextTick = true;
        }
    }

    @Unique
    private boolean isDropAhead(ClientPlayerEntity player, double offsetX, double offsetZ, float stepHeight) {
        Box box = player.getBoundingBox();
        Box testBox = new Box(
                box.minX + offsetX,
                box.minY - stepHeight - 1.0E-5,
                box.minZ + offsetZ,
                box.maxX + offsetX,
                box.minY,
                box.maxZ + offsetZ
        );
        return player.getWorld().isSpaceEmpty(player, testBox);
    }
}