package com.cjeme26.farclimb.client;

import com.cjeme26.farclimb.item.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class FarClimbClient implements ClientModInitializer {
    private static int previousAxeState = -1;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(FarClimbClient::checkClimbingAxes);
    }

    private static void checkClimbingAxes(MinecraftClient client) {
        if (client.player == null) {
            previousAxeState = -1;
            return;
        }

        boolean mainHandAxe = client.player.getMainHandStack().isOf(ModItems.CLIMBING_AXE);
        boolean offhandAxe = client.player.getOffHandStack().isOf(ModItems.CLIMBING_AXE);

        int currentAxeState = (mainHandAxe ? 1 : 0) | (offhandAxe ? 2 : 0);

        if (currentAxeState == previousAxeState) {
            return;
        }

        previousAxeState = currentAxeState;

        Text statusMessage = switch (currentAxeState) {
            case 1 -> Text.literal("Main-hand climbing axe detected");
            case 2 -> Text.literal("Offhand climbing axe detected");
            case 3 -> Text.literal("Two climbing axes detected - ready to climb");
            default -> Text.literal("Climbing axes not equipped");
        };

        client.player.sendMessage(statusMessage, true);
    }
}
