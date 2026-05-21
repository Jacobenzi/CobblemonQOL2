package cz.tvoje.quiettools.mixin;

import cz.tvoje.quiettools.ModSettings;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void removeBoatDrift(CallbackInfo ci) {
        BoatEntity boat = (BoatEntity) (Object) this;

        // Aplikujeme to pouze, když je funkce zapnutá v GUI a zároveň v lodi sedí hráč, který ji řídí.
        // Nechceme, aby prázdné lodě ignorovaly vodní proudy.
        if (ModSettings.BoatControlEnabled && boat.hasPassengers() && boat.getControllingPassenger() != null) {

            Vec3d velocity = boat.getVelocity();

            // 1. Převedeme aktuální rotaci lodě (Yaw) na úhel v radiánech
            float yawRadians = boat.getYaw() * ((float) Math.PI / 180F);

            // 2. Vypočítáme směrový vektor (Forward Vector), kam přesně loď míří
            double forwardX = -MathHelper.sin(yawRadians);
            double forwardZ = MathHelper.cos(yawRadians);

            // 3. Spočítáme skalární součin (Dot Product) rychlosti a směru.
            // Tohle je to kouzlo: Zjistí to, jak moc aktuálně jedeme dopředu/dozadu
            // a kompletně to odignoruje jakékoliv "klouzání" do stran.
            double forwardSpeed = velocity.x * forwardX + velocity.z * forwardZ;

            // 4. Nastavíme lodi novou čistou rychlost pouze po ose, kam se dívá
            double newX = forwardX * forwardSpeed;
            double newZ = forwardZ * forwardSpeed;

            // Osu Y (velocity.y) necháme původní, aby fungovala gravitace a houpání na vodě
            boat.setVelocity(newX, velocity.y, newZ);
        }
    }
}