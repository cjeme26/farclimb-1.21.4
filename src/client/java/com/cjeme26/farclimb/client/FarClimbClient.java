package com.cjeme26.farclimb.client;

import com.cjeme26.farclimb.item.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class FarClimbClient implements ClientModInitializer {
    private static final double WALL_REACH = 1.75D;

    private static int previousClimbingState = -1;

    private static boolean attached = false;
    private static boolean previousNoGravity = false;
    private static Vec3d attachmentPosition;
    private static BlockPos attachmentWallPos;
    private static Direction attachmentWallSide;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(FarClimbClient::tickClimbing);
    }

    private static void tickClimbing(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            resetAttachmentState();
            previousClimbingState = -1;
            return;
        }

        boolean mainHandAxe = client.player.getMainHandStack().isOf(ModItems.CLIMBING_AXE);
        boolean offhandAxe = client.player.getOffHandStack().isOf(ModItems.CLIMBING_AXE);
        boolean attachKeyHeld = client.options.sneakKey.isPressed();

        if (attached) {
            if (!mainHandAxe || !offhandAxe) {
                detach(client, "Detached - both climbing axes are required", mainHandAxe, offhandAxe);
                return;
            }

            if (!attachKeyHeld) {
                detach(client, "Detached from wall", mainHandAxe, offhandAxe);
                return;
            }

            if (!isAttachmentSurfaceStillValid(client)) {
                detach(client, "Detached - climbing surface lost", mainHandAxe, offhandAxe);
                return;
            }

            holdPlayerAtAttachment(client);
            return;
        }

        BlockHitResult climbableWall = mainHandAxe && offhandAxe
                ? getClimbableWallHit(client)
                : null;

        if (mainHandAxe && offhandAxe && attachKeyHeld && climbableWall != null) {
            attach(client, climbableWall);
            return;
        }

        int currentClimbingState = getClimbingState(mainHandAxe, offhandAxe, climbableWall != null);

        if (currentClimbingState == previousClimbingState) {
            return;
        }

        previousClimbingState = currentClimbingState;
        client.player.sendMessage(getStatusMessage(currentClimbingState), true);
    }

    private static void attach(MinecraftClient client, BlockHitResult wallHit) {
        attached = true;
        previousNoGravity = client.player.hasNoGravity();
        attachmentPosition = client.player.getPos();
        attachmentWallPos = wallHit.getBlockPos().toImmutable();
        attachmentWallSide = wallHit.getSide();
        previousClimbingState = 5;

        holdPlayerAtAttachment(client);

        client.player.playSoundToPlayer(
                SoundEvents.BLOCK_ANVIL_PLACE,
                SoundCategory.PLAYERS,
                0.65F,
                1.35F
        );
        client.player.sendMessage(Text.literal("Attached to wall - hold Sneak to stay secured"), true);
    }

    private static void holdPlayerAtAttachment(MinecraftClient client) {
        if (attachmentPosition == null) {
            return;
        }

        client.player.setNoGravity(true);
        client.player.setVelocity(0.0D, 0.0D, 0.0D);
        client.player.setPosition(
                attachmentPosition.x,
                attachmentPosition.y,
                attachmentPosition.z
        );
        client.player.fallDistance = 0.0F;
    }

    private static void detach(
            MinecraftClient client,
            String message,
            boolean mainHandAxe,
            boolean offhandAxe
    ) {
        client.player.setNoGravity(previousNoGravity);
        client.player.setVelocity(0.0D, 0.0D, 0.0D);
        client.player.fallDistance = 0.0F;

        resetAttachmentState();

        BlockHitResult climbableWall = mainHandAxe && offhandAxe
                ? getClimbableWallHit(client)
                : null;
        previousClimbingState = getClimbingState(
                mainHandAxe,
                offhandAxe,
                climbableWall != null
        );

        client.player.sendMessage(Text.literal(message), true);
    }

    private static void resetAttachmentState() {
        attached = false;
        previousNoGravity = false;
        attachmentPosition = null;
        attachmentWallPos = null;
        attachmentWallSide = null;
    }

    private static boolean isAttachmentSurfaceStillValid(MinecraftClient client) {
        if (attachmentWallPos == null || attachmentWallSide == null) {
            return false;
        }

        BlockState blockState = client.world.getBlockState(attachmentWallPos);

        if (blockState.isAir() || !blockState.getFluidState().isEmpty()) {
            return false;
        }

        return Block.isFaceFullSquare(
                blockState.getCollisionShape(client.world, attachmentWallPos),
                attachmentWallSide
        );
    }

    private static int getClimbingState(
            boolean mainHandAxe,
            boolean offhandAxe,
            boolean climbableWallDetected
    ) {
        if (mainHandAxe && offhandAxe) {
            return climbableWallDetected ? 4 : 3;
        }

        if (mainHandAxe) {
            return 1;
        }

        if (offhandAxe) {
            return 2;
        }

        return 0;
    }

    private static Text getStatusMessage(int climbingState) {
        return switch (climbingState) {
            case 1 -> Text.literal("Main-hand climbing axe detected");
            case 2 -> Text.literal("Offhand climbing axe detected");
            case 3 -> Text.literal("Two climbing axes detected - no climbable wall in reach");
            case 4 -> Text.literal("Climbable wall detected - hold Sneak to attach");
            default -> Text.literal("Climbing axes not equipped");
        };
    }

    private static BlockHitResult getClimbableWallHit(MinecraftClient client) {
        HitResult hitResult = client.player.raycast(WALL_REACH, 0.0F, false);

        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            return null;
        }

        if (!blockHitResult.getSide().getAxis().isHorizontal()) {
            return null;
        }

        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState blockState = client.world.getBlockState(blockPos);

        if (blockState.isAir() || !blockState.getFluidState().isEmpty()) {
            return null;
        }

        boolean fullCollisionFace = Block.isFaceFullSquare(
                blockState.getCollisionShape(client.world, blockPos),
                blockHitResult.getSide()
        );

        return fullCollisionFace ? blockHitResult : null;
    }
}
