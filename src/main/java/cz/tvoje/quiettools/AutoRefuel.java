package cz.tvoje.quiettools;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class AutoRefuel {

    private static long lastActionTime = 0;

    // Kdy se má spustit poplach a začít doplňovat (10 %)
    private static final int REFUEL_START = 400;
    // Kdy má přestat doplňovat (těsně pod 4000, abychom neplýtvali)
    private static final int REFUEL_STOP = 3900;

    // Pamatuje si, jestli zrovna probíhá plnění do plna
    private static boolean isRefueling = false;

    private enum State {
        IDLE, WAITING_FOR_BAG, REFUELING_IN_BAG
    }

    private static State currentState = State.IDLE;
    private static int stateTicks = 0;

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.interactionManager == null || !ModSettings.autoRefuelEnabled) {
            isRefueling = false; // Pokud funkci vypneš v GUI, resetuje se to
            return;
        }

        // Získáme aktuální stav paliva
        ScreenHandler currentHandler = client.player.currentScreenHandler;
        int currentFuel = getDrillFuel(currentHandler);

        // Pokud vůbec nemáme Drill u sebe, nic neděláme
        if (currentFuel == -1) {
            isRefueling = false;
            return;
        }

        // Rozhodnutí o ZAČÁTKU / KONCI doplňování
        if (currentFuel <= REFUEL_START) {
            if (!isRefueling) {
                client.player.sendMessage(Text.literal("§e[AutoRefuel] Drill je vybitý, plním do plna!"), false);
            }
            isRefueling = true;
        } else if (currentFuel >= REFUEL_STOP) {
            if (isRefueling) {
                client.player.sendMessage(Text.literal("§a[AutoRefuel] Drill je plný!"), false);
            }
            isRefueling = false;
        }

        // Samotný proces plnění
        switch (currentState) {
            case IDLE:
                if (client.currentScreen != null) return; // Zrovna se hrabeš v truhle
                if (!isRefueling) return; // Nemusíme doplňovat
                if (System.currentTimeMillis() - lastActionTime < 500) return; // Pauza půl vteřiny mezi kliknutími

                if (hasItem(currentHandler, "mythicmetals:morkite")) {
                    // Máme Morkite v normálním inventáři - klikneme a počkáme do dalšího ticku
                    doRefuel(client, currentHandler);
                    lastActionTime = System.currentTimeMillis();
                } else {
                    // Nemáme ho u sebe, otevřeme batoh
                    client.player.sendMessage(Text.literal("§b[AutoRefuel] Otevírám bag " + ModSettings.autoRefuelBagNumber), false);
                    client.getNetworkHandler().sendCommand("bag " + ModSettings.autoRefuelBagNumber);
                    currentState = State.WAITING_FOR_BAG;
                    stateTicks = 0;
                }
                break;

            case WAITING_FOR_BAG:
                stateTicks++;
                if (stateTicks > 60) {
                    client.player.sendMessage(Text.literal("§c[AutoRefuel] Timeout - server neotevřel bag!"), false);
                    currentState = State.IDLE;
                    isRefueling = false;
                    lastActionTime = System.currentTimeMillis();
                    return;
                }

                if (client.currentScreen instanceof HandledScreen) {
                    // Batoh se otevřel, jdeme plnit
                    currentState = State.REFUELING_IN_BAG;
                    stateTicks = 0;
                }
                break;

            case REFUELING_IN_BAG:
                stateTicks++;
                // Čekáme 10 ticků (půl vteřiny) po otevření nebo po předchozím kliknutí, ať se to synchronizuje
                if (stateTicks >= 10) {

                    if (!isRefueling) {
                        // Už je plný! Můžeme zavřít.
                        client.player.closeHandledScreen();
                        currentState = State.IDLE;
                        lastActionTime = System.currentTimeMillis();
                        return;
                    }

                    if (hasItem(currentHandler, "mythicmetals:morkite")) {
                        // Cvakne Morkite do Drillu
                        doRefuel(client, currentHandler);
                        // Vynulujeme časovač, takže to za další půl vteřiny zkusí znovu (pokud ještě není plný)
                        stateTicks = 0;
                    } else {
                        client.player.sendMessage(Text.literal("§c[AutoRefuel] V bagu už došel Morkite!"), false);
                        client.player.closeHandledScreen();
                        isRefueling = false;
                        currentState = State.IDLE;
                        lastActionTime = System.currentTimeMillis();
                    }
                }
                break;
        }
    }

    // Vrací přesné číslo paliva, nebo -1 pokud nenašel Drill
    private static int getDrillFuel(ScreenHandler handler) {
        for (Slot slot : handler.slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            String itemId = Registries.ITEM.getId(stack.getItem()).toString();
            if (itemId.equals("mythicmetals:mythril_drill")) {
                String components = stack.getComponents().toString();
                int fuelIndex = components.indexOf("fuel=");
                if (fuelIndex != -1) {
                    int endIndex = components.indexOf("]", fuelIndex);
                    if (endIndex != -1) {
                        try {
                            return Integer.parseInt(components.substring(fuelIndex + 5, endIndex));
                        } catch (NumberFormatException e) {}
                    }
                }
                return 0; // Drill tam je, ale asi má úplně 0 paliva a hra to smazala
            }
        }
        return -1;
    }

    private static boolean hasItem(ScreenHandler handler, String targetId) {
        for (Slot slot : handler.slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;
            if (Registries.ITEM.getId(stack.getItem()).toString().equals(targetId)) {
                return true;
            }
        }
        return false;
    }

    private static void doRefuel(MinecraftClient client, ScreenHandler handler) {
        int drillSlot = -1;
        int morkiteSlot = -1;

        for (Slot slot : handler.slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            String itemId = Registries.ITEM.getId(stack.getItem()).toString();
            if (itemId.equals("mythicmetals:mythril_drill")) {
                drillSlot = slot.id;
            } else if (itemId.equals("mythicmetals:morkite")) {
                morkiteSlot = slot.id;
            }
        }

        if (drillSlot != -1 && morkiteSlot != -1) {
            client.interactionManager.clickSlot(handler.syncId, morkiteSlot, 0, SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(handler.syncId, drillSlot, 1, SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(handler.syncId, morkiteSlot, 0, SlotActionType.PICKUP, client.player);
        }
    }
}