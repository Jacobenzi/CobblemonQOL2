package cz.tvoje.quiettools.mixin;

import cz.tvoje.quiettools.ModSettings;
import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Cílíme na třídu, která určuje vlastnosti všech bloků ve hře
@Mixin(AbstractBlock.AbstractBlockState.class)
public class FullbrightMixin {

    // Napíchneme se na úplný začátek metody getLuminance (HEAD)
    @Inject(method = "getLuminance", at = @At("HEAD"), cancellable = true)
    public void injectGetLuminance(CallbackInfoReturnable<Integer> cir) {

        // Pokud má hráč zapnutý Fullbright přes tvé GUI nebo klávesu (F)
        if (ModSettings.fullbrightEnabled) {
            // Zrušíme původní výpočet a vnutíme hře maximální úroveň světla (15)
            cir.setReturnValue(15);
        }
    }
}