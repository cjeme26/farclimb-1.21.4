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

import java.util.Random;

public class FarClimbClient implements ClientModInitializer {
    private static final double WALL_REACH = 1.75D;

    // Each climbing input creates one stride. The stride is then eased smoothly
    // from its start to its target over several ticks.
    private static final double MIN_UP_STRIDE = 0.30D;
    private static final double MAX_UP_STRIDE = 0.40D;
    private static final double MIN_SIDEWAYS_STRIDE = 0.16D;
    private static final double MAX_SIDEWAYS_STRIDE = 0.22D;
    private static final double MIN_DOWN_STRIDE = 0.10D;
    private static final double MAX_DOWN_STRIDE = 0.14D;

    private static final int MIN_STRIDE_DURATION_TICKS = 7;
    private static final int MAX_STRIDE_DURATION_TICKS = 9;

    // Mantling is deliberately split into two eased phases. The first raises
    // the player's feet above the ledge; the second pulls them onto the top.
    private static final int MANTLE_LIFT_DURATION_TICKS = 24;
    private static final int MANTLE_PULL_DURATION_TICKS = 6;
    private static final double MANTLE_TOP_CLEARANCE = 0.03D;
    private static final double MANTLE_EDGE_PROGRESS_REQUIRED = 0.65D;
    private static final double PLAYER_HALF_WIDTH = 0.30D;
    private static final double ATTACHMENT_WALL_GAP = 0.035D;
    private static final double MANTLE_EDGE_INSET = 0.31D;
    private static final double PLAYER_EDGE_MARGIN = 0.31D;

    private static final double WALL_FACE_EPSILON = 0.01D;

    // Stationary hanging uses a slow, broad pendulum motion. Active climbing
    // uses a separate alternating pulse tied to each stride, so the camera lean
    // remains visible even when a large upward pull dominates the screen motion.
    private static final float STATIONARY_SWAY_DEGREES = 1.55F;
    private static final double STATIONARY_SWAY_SPEED = 0.075D;
    private static final float UP_STRIDE_SWAY_DEGREES = 3.00F;
    private static final float SIDEWAYS_STRIDE_SWAY_DEGREES = 2.55F;
    private static final float DOWN_STRIDE_SWAY_DEGREES = 2.15F;
    private static final float STATIONARY_SWAY_RESPONSE = 0.12F;
    private static final float MOVING_SWAY_RESPONSE = 0.30F;
    private static final float SWAY_RETURN_RESPONSE = 0.18F;

    private static final Random STRIDE_RANDOM = new Random();

    private static int previousClimbingState = -1;

    private static boolean attached = false;
    private static boolean previousNoGravity = false;
    private static Vec3d attachmentPosition;
    private static Vec3d attachmentContactPoint;
    private static BlockPos attachmentWallPos;
    private static Direction attachmentWallSide;

    private static Vec3d strideStartPosition;
    private static Vec3d strideStartContactPoint;
    private static Vec3d strideTargetPosition;
    private static Vec3d strideTargetContactPoint;
    private static int strideElapsedTicks = 0;
    private static int strideDurationTicks = 0;
    private static int strideSwayDirection = 1;
    private static float strideSwayAmplitudeDegrees = 0.0F;

    private static boolean mantling = false;
    private static Vec3d mantleStartPosition;
    private static Vec3d mantleLiftPosition;
    private static Vec3d mantleTargetPosition;
    private static BlockPos mantleLedgePos;
    private static int mantleElapsedTicks = 0;

    private static double cameraSwayPhase = 0.0D;
    private static float previousCameraRollDegrees = 0.0F;
    private static float cameraRollDegrees = 0.0F;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(FarClimbClient::tickClimbing);
    }

    private static void tickClimbing(MinecraftClient client) {
        tickClimbingLogic(client);
        updateCameraSway(client);
    }

    private static void tickClimbingLogic(MinecraftClient client) {
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

            // Once a mantle begins, it finishes automatically even if Sneak or W
            // is released. This avoids dropping the player in the middle of the lip.
            if (mantling) {
                if (!isMantleSurfaceStillValid(client)) {
                    detach(client, "Mantle cancelled - ledge lost", mainHandAxe, offhandAxe);
                    return;
                }

                advanceMantle(client);
                holdPlayerAtAttachment(client);
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

            moveWhileAttached(client);
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

    private static void updateCameraSway(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            cameraSwayPhase = 0.0D;
            previousCameraRollDegrees = 0.0F;
            cameraRollDegrees = 0.0F;
            return;
        }

        previousCameraRollDegrees = cameraRollDegrees;

        boolean swayActive = attached && !mantling;
        boolean moving = swayActive && isStrideActive();
        float targetRoll = 0.0F;
        float response = SWAY_RETURN_RESPONSE;

        if (moving) {
            // One half-wave per stride: lean out during the pull and return to
            // centre at the target. The sign alternates so consecutive pulls
            // resemble left-hand/right-hand climbing movement.
            double progress = Math.min(
                    1.0D,
                    strideElapsedTicks / (double) Math.max(1, strideDurationTicks)
            );
            double stridePulse = Math.sin(Math.PI * progress);
            targetRoll = (float) stridePulse
                    * strideSwayAmplitudeDegrees
                    * strideSwayDirection;
            response = MOVING_SWAY_RESPONSE;
        } else if (swayActive) {
            cameraSwayPhase += STATIONARY_SWAY_SPEED;
            targetRoll = (float) Math.sin(cameraSwayPhase) * STATIONARY_SWAY_DEGREES;
            response = STATIONARY_SWAY_RESPONSE;
        }

        cameraRollDegrees += (targetRoll - cameraRollDegrees) * response;

        if (!swayActive && Math.abs(cameraRollDegrees) < 0.01F) {
            cameraRollDegrees = 0.0F;
        }
    }

    /**
     * Returns the smoothly interpolated first-person roll for the current frame.
     * The camera mixin calls this after Minecraft has built its normal camera
     * rotation, so FarClimb does not depend on the vanilla View Bobbing option.
     */
    public static float getCameraRollDegrees(float tickDelta) {
        float clampedTickDelta = Math.max(0.0F, Math.min(1.0F, tickDelta));
        return previousCameraRollDegrees
                + (cameraRollDegrees - previousCameraRollDegrees) * clampedTickDelta;
    }

    private static void attach(MinecraftClient client, BlockHitResult wallHit) {
        Vec3d snappedAttachmentPosition = getSnappedAttachmentPosition(client, wallHit);

        // Keep the climber close enough that the axes appear to reach the wall.
        // If the snapped position is obstructed, do not begin attachment.
        if (!isDestinationClear(client, snappedAttachmentPosition)) {
            client.player.sendMessage(Text.literal("Unable to attach - position blocked"), true);
            return;
        }

        attached = true;
        previousNoGravity = client.player.hasNoGravity();
        attachmentPosition = snappedAttachmentPosition;
        attachmentContactPoint = wallHit.getPos();
        attachmentWallPos = wallHit.getBlockPos().toImmutable();
        attachmentWallSide = wallHit.getSide();
        resetStrideState();
        previousClimbingState = 5;

        holdPlayerAtAttachment(client);

        client.player.playSoundToPlayer(
                SoundEvents.BLOCK_ANVIL_PLACE,
                SoundCategory.PLAYERS,
                0.65F,
                1.35F
        );
        client.player.sendMessage(Text.literal("Attached - use W/A/S/D to climb"), true);
    }

    private static Vec3d getSnappedAttachmentPosition(
            MinecraftClient client,
            BlockHitResult wallHit
    ) {
        Vec3d currentPosition = client.player.getPos();
        BlockPos wallPos = wallHit.getBlockPos();
        Direction wallSide = wallHit.getSide();
        double wallClearance = PLAYER_HALF_WIDTH + ATTACHMENT_WALL_GAP;

        return switch (wallSide) {
            case WEST -> new Vec3d(
                    wallPos.getX() - wallClearance,
                    currentPosition.y,
                    currentPosition.z
            );
            case EAST -> new Vec3d(
                    wallPos.getX() + 1.0D + wallClearance,
                    currentPosition.y,
                    currentPosition.z
            );
            case NORTH -> new Vec3d(
                    currentPosition.x,
                    currentPosition.y,
                    wallPos.getZ() - wallClearance
            );
            case SOUTH -> new Vec3d(
                    currentPosition.x,
                    currentPosition.y,
                    wallPos.getZ() + 1.0D + wallClearance
            );
            default -> currentPosition;
        };
    }

    private static void moveWhileAttached(MinecraftClient client) {
        if (isStrideActive()) {
            advanceStride(client);
            return;
        }

        boolean forward = client.options.forwardKey.isPressed();
        boolean backward = client.options.backKey.isPressed();
        boolean left = client.options.leftKey.isPressed();
        boolean right = client.options.rightKey.isPressed();

        boolean verticalInput = forward != backward;
        boolean sidewaysInput = left != right;

        if (!verticalInput && !sidewaysInput) {
            return;
        }

        Vec3d movement = Vec3d.ZERO;

        if (verticalInput) {
            double verticalStride = forward
                    ? randomBetween(MIN_UP_STRIDE, MAX_UP_STRIDE)
                    : -randomBetween(MIN_DOWN_STRIDE, MAX_DOWN_STRIDE);
            movement = movement.add(0.0D, verticalStride, 0.0D);
        }

        if (sidewaysInput && attachmentWallSide != null) {
            // The hit face points from the wall toward the player, so counterclockwise
            // gives the player's visual right while they face into the wall.
            Direction rightAlongWall = attachmentWallSide.rotateYCounterclockwise();
            double sidewaysStride = randomBetween(MIN_SIDEWAYS_STRIDE, MAX_SIDEWAYS_STRIDE);

            if (left) {
                sidewaysStride = -sidewaysStride;
            }

            movement = movement.add(
                    rightAlongWall.getOffsetX() * sidewaysStride,
                    0.0D,
                    rightAlongWall.getOffsetZ() * sidewaysStride
            );
        }

        boolean strideStarted = beginStride(client, movement);

        // If upward movement has reached the end of the wall, try to pull the
        // player over the current block's top instead of simply stopping.
        if (!strideStarted && forward && tryBeginMantle(client)) {
            advanceMantle(client);
            return;
        }

        // Start the first eased increment immediately so input feels responsive.
        if (isStrideActive()) {
            advanceStride(client);
        }
    }

    private static boolean beginStride(MinecraftClient client, Vec3d movement) {
        if (attachmentPosition == null || attachmentContactPoint == null || attachmentWallSide == null) {
            return false;
        }

        Vec3d candidatePosition = attachmentPosition.add(movement);
        Vec3d candidateContactPoint = attachmentContactPoint.add(movement);
        BlockPos candidateWallPos = getWallBlockAtContact(candidateContactPoint, attachmentWallSide);

        if (!isClimbableFace(client, candidateWallPos, attachmentWallSide)) {
            return false;
        }

        if (!isDestinationClear(client, candidatePosition)) {
            return false;
        }

        strideStartPosition = attachmentPosition;
        strideStartContactPoint = attachmentContactPoint;
        strideTargetPosition = candidatePosition;
        strideTargetContactPoint = candidateContactPoint;
        strideElapsedTicks = 0;
        strideDurationTicks = randomIntInclusive(
                MIN_STRIDE_DURATION_TICKS,
                MAX_STRIDE_DURATION_TICKS
        );

        // Alternate the visible body lean for every new pull. Vertical pulls
        // receive the clearest accent, traverses slightly less, and careful
        // downward steps the least.
        strideSwayDirection *= -1;
        if (movement.y > 0.0001D) {
            strideSwayAmplitudeDegrees = UP_STRIDE_SWAY_DEGREES;
        } else if (movement.y < -0.0001D) {
            strideSwayAmplitudeDegrees = DOWN_STRIDE_SWAY_DEGREES;
        } else {
            strideSwayAmplitudeDegrees = SIDEWAYS_STRIDE_SWAY_DEGREES;
        }
        return true;
    }

    private static void advanceStride(MinecraftClient client) {
        if (!isStrideActive() || attachmentWallSide == null) {
            resetStrideState();
            return;
        }

        strideElapsedTicks++;

        double progress = Math.min(1.0D, strideElapsedTicks / (double) strideDurationTicks);
        double easedProgress = smoothStep(progress);

        Vec3d nextPosition = strideStartPosition.lerp(strideTargetPosition, easedProgress);
        Vec3d nextContactPoint = strideStartContactPoint.lerp(strideTargetContactPoint, easedProgress);
        BlockPos nextWallPos = getWallBlockAtContact(nextContactPoint, attachmentWallSide);

        // Recheck every interpolated point so a stride cannot carry the player
        // through a newly created obstruction or across a missing wall face.
        if (!isClimbableFace(client, nextWallPos, attachmentWallSide)
                || !isDestinationClear(client, nextPosition)) {
            resetStrideState();
            return;
        }

        attachmentPosition = nextPosition;
        attachmentContactPoint = nextContactPoint;
        attachmentWallPos = nextWallPos.toImmutable();

        if (progress >= 1.0D) {
            attachmentPosition = strideTargetPosition;
            attachmentContactPoint = strideTargetContactPoint;
            attachmentWallPos = getWallBlockAtContact(
                    strideTargetContactPoint,
                    attachmentWallSide
            ).toImmutable();
            resetStrideState();
        }
    }

    private static boolean isDestinationClear(MinecraftClient client, Vec3d destinationPosition) {
        Vec3d offsetFromActualPosition = destinationPosition.subtract(client.player.getPos());

        return client.world.isSpaceEmpty(
                client.player,
                client.player.getBoundingBox().offset(
                        offsetFromActualPosition.x,
                        offsetFromActualPosition.y,
                        offsetFromActualPosition.z
                )
        );
    }

    private static double smoothStep(double progress) {
        // Smoothstep: ease in, move fastest in the middle, then ease out.
        return progress * progress * (3.0D - 2.0D * progress);
    }

    private static boolean isStrideActive() {
        return strideStartPosition != null
                && strideStartContactPoint != null
                && strideTargetPosition != null
                && strideTargetContactPoint != null
                && strideDurationTicks > 0;
    }

    private static void resetStrideState() {
        strideStartPosition = null;
        strideStartContactPoint = null;
        strideTargetPosition = null;
        strideTargetContactPoint = null;
        strideElapsedTicks = 0;
        strideDurationTicks = 0;
        strideSwayAmplitudeDegrees = 0.0F;
    }

    private static boolean tryBeginMantle(MinecraftClient client) {
        if (attachmentPosition == null
                || attachmentContactPoint == null
                || attachmentWallPos == null
                || attachmentWallSide == null) {
            return false;
        }

        // Do not mantle from the middle or bottom of a block. The axes need to
        // have reached the upper portion of the final wall block first.
        double contactProgressThroughBlock = attachmentContactPoint.y - attachmentWallPos.getY();
        if (contactProgressThroughBlock < MANTLE_EDGE_PROGRESS_REQUIRED) {
            return false;
        }

        BlockPos blockAbove = attachmentWallPos.up();
        if (isClimbableFace(client, blockAbove, attachmentWallSide)) {
            return false;
        }

        if (!hasStandableTop(client, attachmentWallPos)) {
            return false;
        }

        Vec3d targetPosition = getMantleTargetPosition(attachmentPosition, attachmentWallPos, attachmentWallSide);
        double ledgeTopY = attachmentWallPos.getY() + 1.0D;
        Vec3d liftPosition = new Vec3d(
                attachmentPosition.x,
                Math.max(attachmentPosition.y, ledgeTopY + MANTLE_TOP_CLEARANCE),
                attachmentPosition.z
        );

        if (!isDestinationClear(client, liftPosition) || !isDestinationClear(client, targetPosition)) {
            return false;
        }

        mantling = true;
        mantleStartPosition = attachmentPosition;
        mantleLiftPosition = liftPosition;
        mantleTargetPosition = targetPosition;
        mantleLedgePos = attachmentWallPos.toImmutable();
        mantleElapsedTicks = 0;
        resetStrideState();

        client.player.sendMessage(Text.literal("Pulling over ledge..."), true);
        return true;
    }

    private static Vec3d getMantleTargetPosition(
            Vec3d currentPosition,
            BlockPos ledgePos,
            Direction wallSide
    ) {
        double targetX = currentPosition.x;
        double targetZ = currentPosition.z;

        // End with the player's centre barely inside the ledge. This keeps most of
        // the player near the outside edge while still leaving enough support to stand.
        // Along the wall, preserve the player's position where possible.
        if (wallSide.getAxis() == Direction.Axis.X) {
            targetX = wallSide == Direction.WEST
                    ? ledgePos.getX() + MANTLE_EDGE_INSET
                    : ledgePos.getX() + 1.0D - MANTLE_EDGE_INSET;
            targetZ = clamp(
                    currentPosition.z,
                    ledgePos.getZ() + PLAYER_EDGE_MARGIN,
                    ledgePos.getZ() + 1.0D - PLAYER_EDGE_MARGIN
            );
        } else {
            targetZ = wallSide == Direction.NORTH
                    ? ledgePos.getZ() + MANTLE_EDGE_INSET
                    : ledgePos.getZ() + 1.0D - MANTLE_EDGE_INSET;
            targetX = clamp(
                    currentPosition.x,
                    ledgePos.getX() + PLAYER_EDGE_MARGIN,
                    ledgePos.getX() + 1.0D - PLAYER_EDGE_MARGIN
            );
        }

        return new Vec3d(targetX, ledgePos.getY() + 1.0D, targetZ);
    }

    private static void advanceMantle(MinecraftClient client) {
        if (!mantling
                || mantleStartPosition == null
                || mantleLiftPosition == null
                || mantleTargetPosition == null) {
            resetMantleState();
            return;
        }

        mantleElapsedTicks++;
        Vec3d nextPosition;

        if (mantleElapsedTicks <= MANTLE_LIFT_DURATION_TICKS) {
            double progress = mantleElapsedTicks / (double) MANTLE_LIFT_DURATION_TICKS;
            nextPosition = mantleStartPosition.lerp(mantleLiftPosition, smoothStep(progress));
        } else {
            int pullElapsed = mantleElapsedTicks - MANTLE_LIFT_DURATION_TICKS;
            double progress = Math.min(1.0D, pullElapsed / (double) MANTLE_PULL_DURATION_TICKS);
            nextPosition = mantleLiftPosition.lerp(mantleTargetPosition, smoothStep(progress));
        }

        if (!isDestinationClear(client, nextPosition)) {
            detach(client, "Mantle cancelled - path blocked", true, true);
            return;
        }

        attachmentPosition = nextPosition;

        if (mantleElapsedTicks >= MANTLE_LIFT_DURATION_TICKS + MANTLE_PULL_DURATION_TICKS) {
            finishMantle(client);
        }
    }

    private static void finishMantle(MinecraftClient client) {
        Vec3d finalPosition = mantleTargetPosition;
        boolean restoreNoGravity = previousNoGravity;

        resetAttachmentState();

        client.player.setNoGravity(restoreNoGravity);
        client.player.setVelocity(0.0D, 0.0D, 0.0D);
        client.player.setPosition(finalPosition.x, finalPosition.y, finalPosition.z);
        client.player.fallDistance = 0.0F;
        previousClimbingState = -1;
        client.player.sendMessage(Text.literal("Mantled onto ledge"), true);
    }

    private static boolean isMantleSurfaceStillValid(MinecraftClient client) {
        return mantleLedgePos != null
                && hasStandableTop(client, mantleLedgePos)
                && mantleTargetPosition != null
                && isDestinationClear(client, mantleTargetPosition);
    }

    private static boolean hasStandableTop(MinecraftClient client, BlockPos blockPos) {
        BlockState blockState = client.world.getBlockState(blockPos);

        if (blockState.isAir() || !blockState.getFluidState().isEmpty()) {
            return false;
        }

        return Block.isFaceFullSquare(
                blockState.getCollisionShape(client.world, blockPos),
                Direction.UP
        );
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static void resetMantleState() {
        mantling = false;
        mantleStartPosition = null;
        mantleLiftPosition = null;
        mantleTargetPosition = null;
        mantleLedgePos = null;
        mantleElapsedTicks = 0;
    }

    private static double randomBetween(double minimum, double maximum) {
        return minimum + STRIDE_RANDOM.nextDouble() * (maximum - minimum);
    }

    private static int randomIntInclusive(int minimum, int maximum) {
        return minimum + STRIDE_RANDOM.nextInt(maximum - minimum + 1);
    }

    private static BlockPos getWallBlockAtContact(Vec3d contactPoint, Direction wallSide) {
        Vec3d inwardOffset = new Vec3d(
                -wallSide.getOffsetX() * WALL_FACE_EPSILON,
                0.0D,
                -wallSide.getOffsetZ() * WALL_FACE_EPSILON
        );

        return BlockPos.ofFloored(contactPoint.add(inwardOffset));
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
        attachmentContactPoint = null;
        attachmentWallPos = null;
        attachmentWallSide = null;
        resetStrideState();
        resetMantleState();
    }

    private static boolean isAttachmentSurfaceStillValid(MinecraftClient client) {
        return attachmentWallPos != null
                && attachmentWallSide != null
                && isClimbableFace(client, attachmentWallPos, attachmentWallSide);
    }

    private static boolean isClimbableFace(
            MinecraftClient client,
            BlockPos blockPos,
            Direction wallSide
    ) {
        BlockState blockState = client.world.getBlockState(blockPos);

        if (blockState.isAir() || !blockState.getFluidState().isEmpty()) {
            return false;
        }

        return Block.isFaceFullSquare(
                blockState.getCollisionShape(client.world, blockPos),
                wallSide
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

        return isClimbableFace(client, blockPos, blockHitResult.getSide())
                ? blockHitResult
                : null;
    }
}
