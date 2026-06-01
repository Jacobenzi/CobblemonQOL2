package cz.tvoje.quiettools.mixin;

import cz.tvoje.quiettools.ModSettings;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ElytraMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void wasdElytraFlight(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        if (ModSettings.elytraFlightEnabled && player.isFallFlying()) {

            // 1. Přečtení vstupu z klávesnice
            // movementForward: W = 1.0, S = -1.0
            // movementSideways: A = 1.0, D = -1.0
            float forwardInput = player.input.movementForward;
            float strafeInput = player.input.movementSideways;

            boolean jumping = player.input.jumping;   // Mezerník
            boolean sneaking = player.input.sneaking; // Shift

            // 2. Rychlost letu (můžeš si upravit podle sebe)
            double speed = 1.2;

            // 3. Vypočítáme, kam se hráč dívá
            float yaw = player.getYaw();
            float pitch = player.getPitch();

            // Dopředný vektor (kam ukazuješ křížkem)
            Vec3d forward = Vec3d.fromPolar(pitch, yaw).normalize();

            // Vektor pro úkrok (strafing A/D) - odečteme 90 stupňů od Yaw, abychom dostali směr do boku
            Vec3d strafe = Vec3d.fromPolar(0, yaw - 90).normalize();

            // 4. Složíme výsledný směr podle toho, co zrovna mačkáš
            double moveX = forward.x * forwardInput + strafe.x * strafeInput;
            double moveY = forward.y * forwardInput;
            double moveZ = forward.z * forwardInput + strafe.z * strafeInput;

            // Vertikální kontrola (Mezerník = nahoru, Shift = dolů)
            if (jumping) moveY += 1.0;
            if (sneaking) moveY -= 1.0;

            // 5. Normalizace a aplikace rychlosti
            Vec3d result = new Vec3d(moveX, moveY, moveZ);

            if (result.lengthSquared() > 0) {
                // Pokud držíme nějakou klávesu, poletíme daným směrem nastavenou rychlostí
                result = result.normalize().multiply(speed);
            } else {
                // Pokud nedržíme vůbec nic, fyzika se vynuluje a my visíme ve vzduchu na místě
                result = Vec3d.ZERO;
            }

            player.setVelocity(result);
        }
    }
}