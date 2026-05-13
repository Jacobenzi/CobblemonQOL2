package cz.tvoje.quiettools;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.client.MinecraftClient;

public class FullbrightModule {

    private static double savedGamma = 1.0;

    private static boolean wasEnabled = false;

    public static void register() {

        ClientTickEvents.END_CLIENT_TICK.register(
                FullbrightModule::update
        );
    }

    private static void update(
            MinecraftClient client
    ) {

        if (
                client.player == null
                        || client.world == null
        ) {
            return;
        }

        boolean isEnabled =
                ModSettings.fullbrightEnabled;

        // =============================================
        // TOGGLE CHANGED
        // =============================================

        if (isEnabled != wasEnabled) {

            if (isEnabled) {

                savedGamma =
                        client.options
                                .getGamma()
                                .getValue();

                // 1.0 = MAX BRIGHTNESS
                client.options
                        .getGamma()
                        .setValue(1.0);

            } else {

                client.options
                        .getGamma()
                        .setValue(savedGamma);
            }

            wasEnabled = isEnabled;
        }
    }
}