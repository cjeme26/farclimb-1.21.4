package com.cjeme26.farclimb.client;

import com.cjeme26.farclimb.item.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class FarClimbClient implements ClientModInitializer {
    private static final double WALL_REACH = 1.75D;

    private static int previousClimbingState = -1;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(FarClimbClient::checkClimbingState);
    }

    private static void checkClimbingState(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            previousClimbingState = -1;
            return;
        }

        boolean mainHandAxe = client.player.getMainHandStack().isOf(ModItems.CLIMBING_AXE);
        boolean offhandAxe = client.player.getOffHandStack().isOf(ModItems.CLIMBING_AXE);

        int currentClimbingState;

        if (mainHandAxe && offhandAxe) {
            currentClimbingState = hasClimbableWallInReach(client) ? 4 : 3;
        } else if (mainHandAxe) {
            currentClimbingState = 1;
        } else if (offhandAxe) {
            currentClimbingState = 2;
        } else {
            currentClimbingState = 0;
        }

        if (currentClimbingState == previousClimbingState) {
            return;
        }

        previousClimbingState = currentClimbingState;

        Text statusMessage = switch (currentClimbingState) {
            case 1 -> Text.literal("Main-hand climbing axe detected");
            case 2 -> Text.literal("Offhand climbing axe detected");
            case 3 -> Text.literal("Two climbing axes detected - no climbable wall in reach");
            case 4 -> Text.literal("Climbable wall detected - ready to attach");
            default -> Text.literal("Climbing axes not equipped");
        };

        client.player.sendMessage(statusMessage, true);
    }

    private static boolean hasClimbableWallInReach(MinecraftClient client) {
        HitResult hitResult = client.player.raycast(WALL_REACH, 0.0F, false);

        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            return false;
        }

        if (!blockHitResult.getSide().getAxis().isHorizontal()) {
            return false;
        }

        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState blockState = client.world.getBlockState(blockPos);

        if (blockState.isAir() || !blockState.getFluidState().isEmpty()) {
            return false;
        }

        return Block.isFaceFullSquare(
                blockState.getCollisionShape(client.world, blockPos),
                blockHitResult.getSide()
        );
    }
}
