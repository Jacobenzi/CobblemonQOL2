package cz.tvoje.quiettools;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class FullbrightModule {

    private static double savedGamma = 1.0;
    private static boolean wasEnabled = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(FullbrightModule::update);
    }

    private static void update(MinecraftClient client) {

        if (client.player == null || client.world == null) return;

        boolean isEnabled = ModSettings.fullbrightEnabled;

        if (isEnabled != wasEnabled) {

            if (isEnabled) {
                // Uložíme původní gamma
                savedGamma = client.options.getGamma().getValue();
                // 10000.0 = skutečný fullbright
                client.options.getGamma().setValue(10000.0);
            } else {
                // Obnovíme původní gamma
                client.options.getGamma().setValue(savedGamma);
            }

            wasEnabled = isEnabled;
        }
    }
}