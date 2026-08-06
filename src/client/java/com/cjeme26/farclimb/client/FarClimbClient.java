package com.cjeme26.farclimb.client;

import com.cjeme26.farclimb.item.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class FarClimbClient implements ClientModInitializer {
    private static final double WALL_REACH = 1.75D;

    // Each climbing input creates one stride. The stride is eased smoothly
    // from its start to its target over several ticks.
    private static final double MIN_UP_STRIDE = 0.30D;
    private static final double MAX_UP_STRIDE = 0.40D;
    private static final double MIN_SIDEWAYS_STRIDE = 0.16D;
    private static final double MAX_SIDEWAYS_STRIDE = 0.22D;
    private static final double MIN_DOWN_STRIDE = 0.10D;
    private static final double MAX_DOWN_STRIDE = 0.14D;

    private static final int MIN_STRIDE_DURATION_TICKS = 7;
    private static final int MAX_STRIDE_DURATION_TICKS = 9;

    // Mantling is split into two eased phases. The first raises
    // the player's feet above the ledge and the second one pulls them onto the top.
    private static final int MANTLE_LIFT_DURATION_TICKS = 24;
    private static final int MANTLE_PULL_DURATION_TICKS = 6;
    private static final double MANTLE_TOP_CLEARANCE = 0.03D;
    private static final double MANTLE_EDGE_PROGRESS_REQUIRED = 0.65D;
    private static final double PLAYER_HALF_WIDTH = 0.30D;
    private static final double ATTACHMENT_WALL_GAP = 0.035D;
    private static final double MANTLE_EDGE_INSET = 0.31D;
    private static final double PLAYER_EDGE_MARGIN = 0.31D;

    private static final double WALL_FACE_EPSILON = 0.01D;

    // Stationary hanging uses a slow broad pendulum motion. Active climbing
    // Active climbing uses a separate alternating pulse tied to each stride.
    private static final float STATIONARY_SWAY_DEGREES = 1.55F;
    private static final double STATIONARY_SWAY_SPEED = 0.075D;
    private static final float UP_STRIDE_SWAY_DEGREES = 3.00F;
    private static final float SIDEWAYS_STRIDE_SWAY_DEGREES = 2.55F;
    private static final float DOWN_STRIDE_SWAY_DEGREES = 2.15F;
    private static final float STATIONARY_SWAY_RESPONSE = 0.12F;
    private static final float MOVING_SWAY_RESPONSE = 0.30F;
    private static final float SWAY_RETURN_RESPONSE = 0.18F;

    // One-axe hanging treats the attached axe as a fixed pivot. The player's
    // The player's body moves in a slow arc below it while the camera leans less than the
    // less than the body displacement, suggesting that the lower body swings farther.
    private static final double ONE_AXE_SWAY_SPEED = 0.065D;
    private static final double ONE_AXE_SWAY_DISTANCE = 0.25D;
    private static final double ONE_AXE_HAND_BIAS = 0.06D;
    private static final double ONE_AXE_BASE_DROP = 0.08D;
    private static final double ONE_AXE_ARC_RISE = 0.025D;
    private static final double ONE_AXE_SETTLE_SPEED = 0.09D;
    private static final float ONE_AXE_CAMERA_SWAY_DEGREES = 4.25F;
    private static final float ONE_AXE_CAMERA_BIAS_DEGREES = 1.35F;
    private static final float ONE_AXE_CAMERA_RESPONSE = 0.16F;
    private static final double ONE_AXE_CAMERA_LATERAL_FOLLOW = 0.38D;
    private static final double ONE_AXE_CAMERA_VERTICAL_FOLLOW = 0.65D;
    private static final double CAMERA_POSITION_RESPONSE = 0.24D;
    private static final int TWO_AXE_STABILIZE_DURATION_TICKS = 10;

    // While axes are planted, the wall contact constrains how far the climber
    // can turn. Two axes keep the torso square to the wall and allow mostly
    // head movement. One axe allows a wider look range and lets the torso
    // follow part of that turn around the supporting arm.
    private static final float TWO_AXE_LOOK_YAW_LIMIT_DEGREES = 48.0F;
    private static final float ONE_AXE_LOOK_YAW_LIMIT_DEGREES = 88.0F;
    private static final float ONE_AXE_BODY_YAW_FOLLOW = 0.45F;
    private static final double MOUSE_LOOK_DELTA_TO_DEGREES = 0.15D;
    private static final float OUT_OF_RANGE_LOOK_RETURN_DEGREES_PER_TICK = 6.0F;
    private static final float BODY_YAW_RESPONSE = 0.28F;

    // First-person axe presentation. Each successful click now runs one custom
    // strike timeline from the normal held pose into the planted pose. Vanilla's
    // separate attack/use swing is suppressed while climbing axes own the clicks.
    private static final int AXE_STRIKE_DURATION_TICKS = 8;
    private static final double AXE_IMPACT_PROGRESS = 0.76D;
    private static final double AXE_BASE_INWARD_SHIFT = 0.25D;
    private static final double AXE_BASE_RAISE = 0.54D;
    private static final double AXE_BASE_FORWARD_SHIFT = -0.12D;
    private static final double AXE_CONTACT_HORIZONTAL_FOLLOW = 0.30D;
    private static final double AXE_CONTACT_VERTICAL_FOLLOW = 0.18D;
    private static final double AXE_STRIKE_ARC_RAISE = 0.13D;
    private static final double AXE_STRIKE_FORWARD_PUNCH = 0.10D;
    private static final double AXE_IMPACT_RECOIL = 0.035D;
    private static final double AXE_STRIDE_LIFT = 0.10D;
    private static final double AXE_STRIDE_RETRACT = 0.07D;
    private static final float AXE_PLANTED_X_ROTATION = -35.0F;
    private static final float AXE_PLANTED_Y_ROTATION = 8.0F;
    private static final float AXE_PLANTED_Z_ROTATION = 10.0F;
    private static final float AXE_STRIKE_EXTRA_X_ROTATION = -18.0F;
    private static final float AXE_STRIKE_EXTRA_Z_ROTATION = 8.0F;

    private static final Random STRIDE_RANDOM = new Random();

    private static int previousClimbingState = -1;
    private static boolean previousAttackKeyPressed = false;
    private static boolean previousUseKeyPressed = false;

    // Milestone 6.0 tracks each axe separately. The shared attachment position
    // is the player's body position; each axe keeps its own wall contact.
    private static boolean mainAxeAttached = false;
    private static Vec3d mainAxeContactPoint;
    private static BlockPos mainAxeWallPos;
    private static Direction mainAxeWallSide;

    private static boolean offhandAxeAttached = false;
    private static Vec3d offhandAxeContactPoint;
    private static BlockPos offhandAxeWallPos;
    private static Direction offhandAxeWallSide;

    private static boolean previousNoGravity = false;
    private static Vec3d attachmentPosition;
    private static Vec3d attachmentContactPoint;
    private static BlockPos attachmentWallPos;
    private static Direction attachmentWallSide;

    private static Vec3d strideStartPosition;
    private static Vec3d strideStartMainContactPoint;
    private static Vec3d strideStartOffhandContactPoint;
    private static Vec3d strideTargetPosition;
    private static Vec3d strideTargetMainContactPoint;
    private static Vec3d strideTargetOffhandContactPoint;
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
    private static Vec3d previousCameraPositionOffset = Vec3d.ZERO;
    private static Vec3d cameraPositionOffset = Vec3d.ZERO;

    private static Vec3d oneAxeHangBasePosition;
    private static double oneAxeSwayPhase = 0.0D;
    private static double oneAxeSettleProgress = 0.0D;

    private static Vec3d stabilizationStartPosition;
    private static Vec3d stabilizationTargetPosition;
    private static int stabilizationElapsedTicks = 0;

    private static float previousMainAxeStrikeProgress = 0.0F;
    private static float mainAxeStrikeProgress = 0.0F;
    private static boolean mainAxeImpactPlayed = false;
    private static float previousOffhandAxeStrikeProgress = 0.0F;
    private static float offhandAxeStrikeProgress = 0.0F;
    private static boolean offhandAxeImpactPlayed = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(FarClimbClient::tickClimbing);

        // Mouse buttons are mapped to the hands as they appear on screen:
        // left click controls the visible left-side axe and right click controls
        // the visible right-side axe. This also respects Minecraft's Main Hand
        // setting for players who choose a left-handed character.
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            boolean mainHandIsLeftSide = player.getMainArm() == Arm.LEFT;
            boolean leftSideAxeEquipped = (mainHandIsLeftSide
                    ? player.getMainHandStack()
                    : player.getOffHandStack()).isOf(ModItems.CLIMBING_AXE);

            if (player == client.player && leftSideAxeEquipped) {
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });

        // Reserve right click when the axe visible on the right side is equipped,
        // preventing normal block use before FarClimb handles the toggle.
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            boolean mainHandIsRightSide = player.getMainArm() == Arm.RIGHT;
            boolean rightSideAxeEquipped = (mainHandIsRightSide
                    ? player.getMainHandStack()
                    : player.getOffHandStack()).isOf(ModItems.CLIMBING_AXE);

            if (player == client.player && rightSideAxeEquipped) {
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });
    }

    private static void tickClimbing(MinecraftClient client) {
        tickClimbingLogic(client);
        updateWallFacingRotation(client);
        updateAxeStrikeAnimation(client);
        updateCameraSway(client);
    }

    private static void tickClimbingLogic(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            resetAttachmentState();
            previousClimbingState = -1;
            previousAttackKeyPressed = false;
            previousUseKeyPressed = false;
            return;
        }

        boolean attackKeyPressed = client.options.attackKey.isPressed();
        boolean useKeyPressed = client.options.useKey.isPressed();
        boolean leftSideClick = attackKeyPressed && !previousAttackKeyPressed;
        boolean rightSideClick = useKeyPressed && !previousUseKeyPressed;
        previousAttackKeyPressed = attackKeyPressed;
        previousUseKeyPressed = useKeyPressed;

        // Internally Minecraft tracks MAIN_HAND and OFF_HAND, but the player sees
        // a left and right hand. Convert the physical mouse side into the correct
        // Minecraft hand according to the player's configured main arm.
        boolean mainHandIsLeftSide = client.player.getMainArm() == Arm.LEFT;
        boolean mainAxeClick = mainHandIsLeftSide ? leftSideClick : rightSideClick;
        boolean offhandAxeClick = mainHandIsLeftSide ? rightSideClick : leftSideClick;

        // Never interpret clicks made in inventories, menus, or chat as axe input.
        if (client.currentScreen != null) {
            return;
        }

        boolean mainHandAxeEquipped = client.player.getMainHandStack().isOf(ModItems.CLIMBING_AXE);
        boolean offhandAxeEquipped = client.player.getOffHandStack().isOf(ModItems.CLIMBING_AXE);

        // Removing an axe from its hand releases only that axe. The other axe,
        // if still attached, continues to support the player.
        if (mainAxeAttached && !mainHandAxeEquipped) {
            releaseAxe(client, true, getAxeDisplayName(client, true) + " axe released - item removed");
        }
        if (offhandAxeAttached && !offhandAxeEquipped) {
            releaseAxe(client, false, getAxeDisplayName(client, false) + " axe released - item removed");
        }

        validateAttachedSurfaces(client);

        // A mantle is already committed movement. Ignore click toggles until it
        // finishes, but still cancel it if its ledge becomes invalid.
        if (mantling) {
            if (!hasBothAxesAttached()) {
                detachCompletely(client, "Mantle cancelled - both axes are required");
                return;
            }

            if (!isMantleSurfaceStillValid(client)) {
                detachCompletely(client, "Mantle cancelled - ledge lost");
                return;
            }

            advanceMantle(client);
            holdPlayerAtAttachment(client);
            return;
        }

        if (mainAxeClick && mainHandAxeEquipped) {
            toggleAxe(client, true);
        }
        if (offhandAxeClick && offhandAxeEquipped) {
            toggleAxe(client, false);
        }

        if (!hasAnyAxeAttached()) {
            BlockHitResult climbableWall = mainHandAxeEquipped || offhandAxeEquipped
                    ? getClimbableWallHit(client)
                    : null;
            int currentClimbingState = getClimbingState(
                    mainHandAxeEquipped,
                    offhandAxeEquipped,
                    climbableWall != null
            );

            if (currentClimbingState != previousClimbingState) {
                previousClimbingState = currentClimbingState;
            }
            return;
        }

        if (hasBothAxesAttached()) {
            if (isTwoAxeStabilizationActive()) {
                advanceTwoAxeStabilization(client);
            } else {
                moveWhileAttached(client);
            }
        } else {
            // One axe acts as a fixed pivot. The player's body follows a slow
            // collision-aware pendulum arc below the attached hand.
            resetStrideState();
            updateOneAxeHang(client);
        }

        holdPlayerAtAttachment(client);
    }

    /**
     * Restricts the local player around the normal of the planted wall face.
     * The camera/head may still look around inside the allowed arc, but the
     * torso remains physically constrained by the attached axes.
     */
    private static void updateWallFacingRotation(MinecraftClient client) {
        if (client.player == null
                || !hasAnyAxeAttached()
                || attachmentWallSide == null
                || !attachmentWallSide.getAxis().isHorizontal()) {
            return;
        }

        float wallFacingYaw = MathHelper.wrapDegrees(
                getHorizontalDirectionYaw(attachmentWallSide.getOpposite())
        );
        float lookLimit = getCurrentLookYawLimit();
        float currentOffset = MathHelper.wrapDegrees(
                client.player.getYaw() - wallFacingYaw
        );

        // Input is normally clamped before Entity.changeLookDirection applies
        // it. The only time the player can still be outside the range is when a
        // second axe is planted and the allowed arc becomes narrower. Ease that
        // transition back inside the new range instead of snapping in one tick.
        float targetOffset = MathHelper.clamp(
                currentOffset,
                -lookLimit,
                lookLimit
        );
        if (Math.abs(MathHelper.wrapDegrees(targetOffset - currentOffset)) > 0.001F) {
            float easedOffset = approachAngleDegrees(
                    currentOffset,
                    targetOffset,
                    OUT_OF_RANGE_LOOK_RETURN_DEGREES_PER_TICK
            );
            float easedYaw = MathHelper.wrapDegrees(wallFacingYaw + easedOffset);
            client.player.setYaw(easedYaw);
            client.player.setHeadYaw(easedYaw);
            currentOffset = easedOffset;
        } else {
            // Keep the rendered head synchronized with the already-clamped
            // camera yaw without changing the camera itself.
            client.player.setHeadYaw(client.player.getYaw());
        }

        // With two axes, the torso remains square to the wall. With one axe it
        // follows part of the head turn. Ease the body independently so the
        // model does not jerk when the player reaches either look limit.
        float bodyOffset = hasBothAxesAttached()
                ? 0.0F
                : currentOffset * ONE_AXE_BODY_YAW_FOLLOW;
        float targetBodyYaw = MathHelper.wrapDegrees(wallFacingYaw + bodyOffset);
        client.player.setBodyYaw(lerpAngleDegrees(
                client.player.getBodyYaw(),
                targetBodyYaw,
                BODY_YAW_RESPONSE
        ));
    }

    /**
     * Limits horizontal mouse input before vanilla applies it. This prevents
     * the old boundary jitter where the mouse rotated past the limit and the
     * end-of-tick climbing code repeatedly snapped the camera back.
     */
    public static double clampClimbingLookDeltaX(Object entity, double cursorDeltaX) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null
                || entity != client.player
                || !hasAnyAxeAttached()
                || attachmentWallSide == null
                || !attachmentWallSide.getAxis().isHorizontal()) {
            return cursorDeltaX;
        }

        float wallFacingYaw = MathHelper.wrapDegrees(
                getHorizontalDirectionYaw(attachmentWallSide.getOpposite())
        );
        float currentOffset = MathHelper.wrapDegrees(
                client.player.getYaw() - wallFacingYaw
        );
        float lookLimit = getCurrentLookYawLimit();
        double requestedDegrees = cursorDeltaX * MOUSE_LOOK_DELTA_TO_DEGREES;

        // If a newly tightened two-axe limit leaves the player temporarily
        // outside the range, block only input that would move farther out. The
        // tick update eases the view back toward the boundary.
        if (currentOffset > lookLimit && requestedDegrees >= 0.0D) {
            return 0.0D;
        }
        if (currentOffset < -lookLimit && requestedDegrees <= 0.0D) {
            return 0.0D;
        }
        if (currentOffset > lookLimit || currentOffset < -lookLimit) {
            return cursorDeltaX;
        }

        double targetOffset = currentOffset + requestedDegrees;
        double clampedTargetOffset = MathHelper.clamp(
                targetOffset,
                -lookLimit,
                lookLimit
        );
        return (clampedTargetOffset - currentOffset)
                / MOUSE_LOOK_DELTA_TO_DEGREES;
    }

    private static float getCurrentLookYawLimit() {
        return hasBothAxesAttached()
                ? TWO_AXE_LOOK_YAW_LIMIT_DEGREES
                : ONE_AXE_LOOK_YAW_LIMIT_DEGREES;
    }

    private static float approachAngleDegrees(float current, float target, float maximumStep) {
        float difference = MathHelper.wrapDegrees(target - current);
        if (difference > maximumStep) {
            difference = maximumStep;
        } else if (difference < -maximumStep) {
            difference = -maximumStep;
        }
        return MathHelper.wrapDegrees(current + difference);
    }

    private static float lerpAngleDegrees(float current, float target, float response) {
        float difference = MathHelper.wrapDegrees(target - current);
        return MathHelper.wrapDegrees(current + difference * response);
    }

    /**
     * Converts a horizontal block direction to Minecraft's yaw convention.
     * Yarn 1.21.4 does not expose Direction.asRotation(), so FarClimb keeps
     * the conversion explicit and mapping-independent.
     */
    private static float getHorizontalDirectionYaw(Direction direction) {
        return switch (direction) {
            case SOUTH -> 0.0F;
            case WEST -> 90.0F;
            case NORTH -> 180.0F;
            case EAST -> -90.0F;
            default -> 0.0F;
        };
    }

    private static void toggleAxe(MinecraftClient client, boolean mainHand) {
        if (mainHand ? mainAxeAttached : offhandAxeAttached) {
            releaseAxe(
                    client,
                    mainHand,
                    getAxeDisplayName(client, mainHand) + " axe released"
            );
            return;
        }

        attachAxe(client, mainHand);
    }

    private static void attachAxe(MinecraftClient client, boolean mainHand) {
        BlockHitResult wallHit = getClimbableWallHit(client);
        String axeName = getAxeDisplayName(client, mainHand);

        if (wallHit == null) {
            client.player.sendMessage(Text.literal(axeName + " axe missed - no wall in reach"), true);
            syncPreviousClimbingState(client);
            return;
        }

        if (hasAnyAxeAttached()) {
            Direction supportingSide = getRemainingAttachedWallSide();
            if (supportingSide != null && wallHit.getSide() != supportingSide) {
                client.player.sendMessage(
                        Text.literal("Both axes must attach to the same wall face"),
                        true
                );
                return;
            }
        } else {
            Vec3d snappedAttachmentPosition = getSnappedAttachmentPosition(client, wallHit);
            if (!isDestinationClear(client, snappedAttachmentPosition)) {
                client.player.sendMessage(Text.literal("Unable to attach - position blocked"), true);
                syncPreviousClimbingState(client);
                return;
            }

            previousNoGravity = client.player.hasNoGravity();
            attachmentPosition = snappedAttachmentPosition;
            attachmentWallSide = wallHit.getSide();
        }

        if (mainHand) {
            mainAxeAttached = true;
            mainAxeContactPoint = wallHit.getPos();
            mainAxeWallPos = wallHit.getBlockPos().toImmutable();
            mainAxeWallSide = wallHit.getSide();
        } else {
            offhandAxeAttached = true;
            offhandAxeContactPoint = wallHit.getPos();
            offhandAxeWallPos = wallHit.getBlockPos().toImmutable();
            offhandAxeWallSide = wallHit.getSide();
        }

        refreshSharedAttachmentReference();
        resetStrideState();

        if (hasBothAxesAttached()) {
            beginTwoAxeStabilization();
        } else {
            beginOneAxeHang();
        }

        previousClimbingState = 5;
        holdPlayerAtAttachment(client);
        beginAxeStrike(mainHand);

    }

    private static void releaseAxe(MinecraftClient client, boolean mainHand, String message) {
        if (mainHand) {
            mainAxeAttached = false;
            mainAxeContactPoint = null;
            mainAxeWallPos = null;
            mainAxeWallSide = null;
        } else {
            offhandAxeAttached = false;
            offhandAxeContactPoint = null;
            offhandAxeWallPos = null;
            offhandAxeWallSide = null;
        }

        resetStrideState();

        if (!hasAnyAxeAttached()) {
            detachCompletely(client, message + " - falling");
            return;
        }

        refreshSharedAttachmentReference();
        beginOneAxeHang();
        holdPlayerAtAttachment(client);

    }

    private static void validateAttachedSurfaces(MinecraftClient client) {
        if (mainAxeAttached && !isClimbableFace(client, mainAxeWallPos, mainAxeWallSide)) {
            releaseAxe(client, true, getAxeDisplayName(client, true) + " axe lost its grip");
        }

        if (offhandAxeAttached && !isClimbableFace(client, offhandAxeWallPos, offhandAxeWallSide)) {
            releaseAxe(client, false, getAxeDisplayName(client, false) + " axe lost its grip");
        }
    }

    private static boolean hasAnyAxeAttached() {
        return mainAxeAttached || offhandAxeAttached;
    }

    private static boolean hasBothAxesAttached() {
        return mainAxeAttached && offhandAxeAttached;
    }

    private static Direction getRemainingAttachedWallSide() {
        if (mainAxeAttached) {
            return mainAxeWallSide;
        }
        if (offhandAxeAttached) {
            return offhandAxeWallSide;
        }
        return null;
    }

    private static void refreshSharedAttachmentReference() {
        if (hasBothAxesAttached()) {
            attachmentContactPoint = mainAxeContactPoint.add(offhandAxeContactPoint).multiply(0.5D);
            attachmentWallSide = mainAxeWallSide;
            attachmentWallPos = getWallBlockAtContact(
                    attachmentContactPoint,
                    attachmentWallSide
            ).toImmutable();
        } else if (mainAxeAttached) {
            attachmentContactPoint = mainAxeContactPoint;
            attachmentWallPos = mainAxeWallPos;
            attachmentWallSide = mainAxeWallSide;
        } else if (offhandAxeAttached) {
            attachmentContactPoint = offhandAxeContactPoint;
            attachmentWallPos = offhandAxeWallPos;
            attachmentWallSide = offhandAxeWallSide;
        } else {
            attachmentContactPoint = null;
            attachmentWallPos = null;
            attachmentWallSide = null;
        }
    }

    private static void beginAxeStrike(boolean mainHand) {
        if (mainHand) {
            previousMainAxeStrikeProgress = 0.0F;
            mainAxeStrikeProgress = 0.0F;
            mainAxeImpactPlayed = false;
        } else {
            previousOffhandAxeStrikeProgress = 0.0F;
            offhandAxeStrikeProgress = 0.0F;
            offhandAxeImpactPlayed = false;
        }
    }

    private static void updateAxeStrikeAnimation(MinecraftClient client) {
        previousMainAxeStrikeProgress = mainAxeStrikeProgress;
        previousOffhandAxeStrikeProgress = offhandAxeStrikeProgress;

        if (mainAxeAttached) {
            mainAxeStrikeProgress = advanceStrikeProgress(mainAxeStrikeProgress);
            if (!mainAxeImpactPlayed && mainAxeStrikeProgress >= AXE_IMPACT_PROGRESS) {
                playAxeImpact(client, true);
                mainAxeImpactPlayed = true;
            }
        } else {
            mainAxeStrikeProgress = 0.0F;
            mainAxeImpactPlayed = false;
        }

        if (offhandAxeAttached) {
            offhandAxeStrikeProgress = advanceStrikeProgress(offhandAxeStrikeProgress);
            if (!offhandAxeImpactPlayed && offhandAxeStrikeProgress >= AXE_IMPACT_PROGRESS) {
                playAxeImpact(client, false);
                offhandAxeImpactPlayed = true;
            }
        } else {
            offhandAxeStrikeProgress = 0.0F;
            offhandAxeImpactPlayed = false;
        }
    }

    private static float advanceStrikeProgress(float current) {
        return Math.min(1.0F, current + 1.0F / AXE_STRIKE_DURATION_TICKS);
    }

    private static void playAxeImpact(MinecraftClient client, boolean mainHand) {
        if (client.player == null) {
            return;
        }

        client.player.playSoundToPlayer(
                SoundEvents.BLOCK_ANVIL_PLACE,
                SoundCategory.PLAYERS,
                0.55F,
                mainHand ? 1.42F : 1.52F
        );
    }

    /**
     * Returns whether the supplied Minecraft hand currently has an axe planted.
     */
    public static boolean isAxeAttached(Hand hand) {
        return hand == Hand.MAIN_HAND ? mainAxeAttached : offhandAxeAttached;
    }



    /**
     * Returns whether the local player model should use FarClimb's third-person
     * climbing pose for this rendered entity.
     */
    public static boolean shouldApplyThirdPersonClimbingPose(int renderedEntityId) {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player != null
                && client.player.getId() == renderedEntityId
                && hasAnyAxeAttached()
                && !mantling;
    }

    /**
     * Returns whether the axe visibly held on the requested screen/body side is
     * currently planted. This respects Minecraft's configurable main arm.
     */
    public static boolean isVisibleSideAxeAttached(boolean leftSide) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }

        boolean mainHandIsLeftSide = client.player.getMainArm() == Arm.LEFT;
        boolean mainHand = leftSide == mainHandIsLeftSide;
        return mainHand ? mainAxeAttached : offhandAxeAttached;
    }

    /**
     * Returns the saved wall contact for the axe on the requested visible side.
     */
    public static Vec3d getVisibleSideAxeContactPoint(boolean leftSide) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return null;
        }

        boolean mainHandIsLeftSide = client.player.getMainArm() == Arm.LEFT;
        boolean mainHand = leftSide == mainHandIsLeftSide;
        return mainHand ? mainAxeContactPoint : offhandAxeContactPoint;
    }

    /**
     * Returns a 0-1 planting blend for the axe on the requested visible side.
     * Third-person arms use the same strike timeline as the first-person tools.
     */
    public static float getVisibleSideAxePlantProgress(boolean leftSide, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return 0.0F;
        }

        boolean mainHandIsLeftSide = client.player.getMainArm() == Arm.LEFT;
        boolean mainHand = leftSide == mainHandIsLeftSide;
        float previousProgress = mainHand
                ? previousMainAxeStrikeProgress
                : previousOffhandAxeStrikeProgress;
        float currentProgress = mainHand
                ? mainAxeStrikeProgress
                : offhandAxeStrikeProgress;
        float clampedTickDelta = Math.max(0.0F, Math.min(1.0F, tickDelta));
        float progress = previousProgress
                + (currentProgress - previousProgress) * clampedTickDelta;
        return (float) smoothStep(clamp(
                progress / (float) AXE_IMPACT_PROGRESS,
                0.0D,
                1.0D
        ));
    }

    /**
     * Returns the current one-axe pendulum phase in the -1 to 1 range.
     */
    public static float getThirdPersonPendulumAmount(float tickDelta) {
        if (!hasAnyAxeAttached() || hasBothAxesAttached() || mantling) {
            return 0.0F;
        }

        double clampedTickDelta = Math.max(0.0D, Math.min(1.0D, tickDelta));
        double interpolatedPhase = oneAxeSwayPhase + ONE_AXE_SWAY_SPEED * clampedTickDelta;
        double interpolatedSettle = Math.min(
                1.0D,
                oneAxeSettleProgress + ONE_AXE_SETTLE_SPEED * clampedTickDelta
        );
        return (float) (
                Math.sin(interpolatedPhase) * smoothStep(interpolatedSettle)
        );
    }

    /**
     * Returns the current climbing-stride lift for one visible axe. Only the axe
     * being repositioned during this stride receives a non-zero pulse.
     */
    public static float getVisibleSideStridePulse(boolean leftSide, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !hasBothAxesAttached() || !isStrideActive()) {
            return 0.0F;
        }

        boolean mainHandIsLeftSide = client.player.getMainArm() == Arm.LEFT;
        boolean mainHand = leftSide == mainHandIsLeftSide;
        boolean thisAxeRepositioning = (strideSwayDirection > 0) == mainHand;
        if (!thisAxeRepositioning) {
            return 0.0F;
        }

        double clampedTickDelta = Math.max(0.0D, Math.min(1.0D, tickDelta));
        double progress = Math.min(
                1.0D,
                (strideElapsedTicks + clampedTickDelta)
                        / (double) Math.max(1, strideDurationTicks)
        );
        return (float) Math.sin(Math.PI * progress);
    }

    /**
     * Applies the first-person planted pose to the existing vanilla item model.
     * The contact point influences the small horizontal and vertical corrections,
     * while the majority of the transform keeps the handle readable on screen.
     */
    public static void applyFirstPersonAxeTransform(
            MatrixStack matrices,
            Hand hand,
            float tickDelta
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !isAxeAttached(hand)) {
            return;
        }

        boolean mainHand = hand == Hand.MAIN_HAND;
        Vec3d contactPoint = mainHand ? mainAxeContactPoint : offhandAxeContactPoint;
        if (contactPoint == null) {
            return;
        }

        float clampedTickDelta = Math.max(0.0F, Math.min(1.0F, tickDelta));
        float previousProgress = mainHand
                ? previousMainAxeStrikeProgress
                : previousOffhandAxeStrikeProgress;
        float currentProgress = mainHand
                ? mainAxeStrikeProgress
                : offhandAxeStrikeProgress;
        double rawStrikeProgress = previousProgress
                + (currentProgress - previousProgress) * clampedTickDelta;
        rawStrikeProgress = clamp(rawStrikeProgress, 0.0D, 1.0D);

        // Reach the wall quickly, then use the remaining frames for a tiny
        // impact recoil and settle. This makes the click, strike, sound, and
        // final planted pose read as one continuous movement.
        double travelProgress = smoothStep(clamp(
                rawStrikeProgress / AXE_IMPACT_PROGRESS,
                0.0D,
                1.0D
        ));
        double impactSettleProgress = smoothStep(clamp(
                (rawStrikeProgress - AXE_IMPACT_PROGRESS)
                        / (1.0D - AXE_IMPACT_PROGRESS),
                0.0D,
                1.0D
        ));
        double strikeArc = Math.sin(Math.PI * travelProgress);
        double impactRecoil = Math.sin(Math.PI * impactSettleProgress);

        boolean mainHandIsLeftSide = client.player.getMainArm() == Arm.LEFT;
        boolean handIsLeftSide = mainHand == mainHandIsLeftSide;
        double side = handIsLeftSide ? -1.0D : 1.0D;

        Vec3d eyePosition = client.gameRenderer.getCamera().getPos();
        Vec3d toContact = contactPoint.subtract(eyePosition);
        float cameraYaw = client.gameRenderer.getCamera().getYaw();
        float cameraPitch = client.gameRenderer.getCamera().getPitch();
        Vec3d forward = Vec3d.fromPolar(cameraPitch, cameraYaw).normalize();
        Vec3d right = new Vec3d(0.0D, 1.0D, 0.0D).crossProduct(forward).normalize();
        Vec3d up = forward.crossProduct(right).normalize();

        double contactHorizontal = clamp(
                toContact.dotProduct(right),
                -0.45D,
                0.45D
        );
        double contactVertical = clamp(
                toContact.dotProduct(up),
                -0.35D,
                0.45D
        );

        double stridePulse = 0.0D;
        if (hasBothAxesAttached() && isStrideActive()) {
            boolean thisAxeRepositioning = (strideSwayDirection > 0) == mainHand;
            if (thisAxeRepositioning) {
                double progress = Math.min(
                        1.0D,
                        (strideElapsedTicks + clampedTickDelta)
                                / (double) Math.max(1, strideDurationTicks)
                );
                stridePulse = Math.sin(Math.PI * progress);
            }
        }

        double translateX = (
                -side * AXE_BASE_INWARD_SHIFT
                        + contactHorizontal * AXE_CONTACT_HORIZONTAL_FOLLOW
        ) * travelProgress;
        double translateY = (
                AXE_BASE_RAISE
                        + contactVertical * AXE_CONTACT_VERTICAL_FOLLOW
                        + stridePulse * AXE_STRIDE_LIFT
        ) * travelProgress
                + strikeArc * AXE_STRIKE_ARC_RAISE;
        double translateZ = (
                AXE_BASE_FORWARD_SHIFT
                        + stridePulse * AXE_STRIDE_RETRACT
        ) * travelProgress
                - strikeArc * AXE_STRIKE_FORWARD_PUNCH
                + impactRecoil * AXE_IMPACT_RECOIL;

        matrices.translate(translateX, translateY, translateZ);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) (
                AXE_PLANTED_X_ROTATION * travelProgress
                        + AXE_STRIKE_EXTRA_X_ROTATION * strikeArc
        )));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(
                (float) (side * AXE_PLANTED_Y_ROTATION * travelProgress)
        ));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) (
                -side * AXE_PLANTED_Z_ROTATION * travelProgress
                        - side * AXE_STRIKE_EXTRA_Z_ROTATION * strikeArc
        )));
    }

    private static void updateCameraSway(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            cameraSwayPhase = 0.0D;
            previousCameraRollDegrees = 0.0F;
            cameraRollDegrees = 0.0F;
            previousCameraPositionOffset = Vec3d.ZERO;
            cameraPositionOffset = Vec3d.ZERO;
            return;
        }

        previousCameraRollDegrees = cameraRollDegrees;
        previousCameraPositionOffset = cameraPositionOffset;

        boolean swayActive = hasAnyAxeAttached() && !mantling;
        boolean oneAxeHanging = hasAnyAxeAttached() && !hasBothAxesAttached() && !mantling;
        boolean moving = hasBothAxesAttached() && isStrideActive();
        float targetRoll = 0.0F;
        float response = SWAY_RETURN_RESPONSE;
        Vec3d targetCameraPositionOffset = Vec3d.ZERO;

        if (oneAxeHanging) {
            float attachedHandBias = mainAxeAttached
                    ? ONE_AXE_CAMERA_BIAS_DEGREES
                    : -ONE_AXE_CAMERA_BIAS_DEGREES;
            float pendulumRoll = (float) -Math.sin(oneAxeSwayPhase)
                    * ONE_AXE_CAMERA_SWAY_DEGREES;

            targetRoll = attachedHandBias + pendulumRoll;
            response = ONE_AXE_CAMERA_RESPONSE;

            if (oneAxeHangBasePosition != null && attachmentPosition != null) {
                Vec3d bodyOffset = attachmentPosition.subtract(oneAxeHangBasePosition);
                targetCameraPositionOffset = new Vec3d(
                        bodyOffset.x * (ONE_AXE_CAMERA_LATERAL_FOLLOW - 1.0D),
                        bodyOffset.y * (ONE_AXE_CAMERA_VERTICAL_FOLLOW - 1.0D),
                        bodyOffset.z * (ONE_AXE_CAMERA_LATERAL_FOLLOW - 1.0D)
                );
            }
        } else if (moving) {
            double progress = Math.min(
                    1.0D,
                    strideElapsedTicks / (double) Math.max(1, strideDurationTicks)
            );
            double stridePulse = Math.sin(Math.PI * progress);
            targetRoll = (float) stridePulse
                    * strideSwayAmplitudeDegrees
                    * strideSwayDirection;
            response = MOVING_SWAY_RESPONSE;
        } else if (swayActive && !isTwoAxeStabilizationActive()) {
            cameraSwayPhase += STATIONARY_SWAY_SPEED;
            targetRoll = (float) Math.sin(cameraSwayPhase) * STATIONARY_SWAY_DEGREES;
            response = STATIONARY_SWAY_RESPONSE;
        }

        cameraRollDegrees += (targetRoll - cameraRollDegrees) * response;
        cameraPositionOffset = cameraPositionOffset.lerp(
                targetCameraPositionOffset,
                CAMERA_POSITION_RESPONSE
        );

        if (!swayActive && Math.abs(cameraRollDegrees) < 0.01F) {
            cameraRollDegrees = 0.0F;
        }
        if (!oneAxeHanging && cameraPositionOffset.lengthSquared() < 0.000001D) {
            cameraPositionOffset = Vec3d.ZERO;
        }
    }

    /**
     * Returns the smoothly interpolated first-person roll for the current frame.
     */
    public static float getCameraRollDegrees(float tickDelta) {
        float clampedTickDelta = Math.max(0.0F, Math.min(1.0F, tickDelta));
        return previousCameraRollDegrees
                + (cameraRollDegrees - previousCameraRollDegrees) * clampedTickDelta;
    }

    /**
     * Returns a first-person positional correction that keeps the camera closer
     * to the axe pivot while the lower body follows the wider pendulum arc.
     */
    public static Vec3d getCameraPositionOffset(float tickDelta) {
        double clampedTickDelta = Math.max(0.0D, Math.min(1.0D, tickDelta));
        return previousCameraPositionOffset.lerp(cameraPositionOffset, clampedTickDelta);
    }

    private static void beginOneAxeHang() {
        oneAxeHangBasePosition = attachmentPosition;
        oneAxeSwayPhase = 0.0D;
        oneAxeSettleProgress = 0.0D;
        resetTwoAxeStabilization();
    }

    private static void updateOneAxeHang(MinecraftClient client) {
        if (hasBothAxesAttached() || !hasAnyAxeAttached() || attachmentWallSide == null) {
            resetOneAxeHangState();
            return;
        }

        if (oneAxeHangBasePosition == null) {
            beginOneAxeHang();
        }

        oneAxeSwayPhase += ONE_AXE_SWAY_SPEED;
        oneAxeSettleProgress = Math.min(
                1.0D,
                oneAxeSettleProgress + ONE_AXE_SETTLE_SPEED
        );

        double settle = smoothStep(oneAxeSettleProgress);
        double swing = Math.sin(oneAxeSwayPhase);
        double handBias = mainAxeAttached ? -ONE_AXE_HAND_BIAS : ONE_AXE_HAND_BIAS;
        double sidewaysOffset = (handBias + swing * ONE_AXE_SWAY_DISTANCE) * settle;

        // The body hangs lowest near the middle of the arc and rises slightly
        // toward either side, approximating a short pendulum under the axe.
        double verticalOffset = (
                -ONE_AXE_BASE_DROP
                        + ONE_AXE_ARC_RISE * swing * swing
        ) * settle;

        Direction rightAlongWall = attachmentWallSide.rotateYCounterclockwise();
        Vec3d candidatePosition = oneAxeHangBasePosition.add(
                rightAlongWall.getOffsetX() * sidewaysOffset,
                verticalOffset,
                rightAlongWall.getOffsetZ() * sidewaysOffset
        );

        attachmentPosition = getSafeOneAxeHangPosition(
                client,
                oneAxeHangBasePosition,
                candidatePosition
        );
    }

    private static Vec3d getSafeOneAxeHangPosition(
            MinecraftClient client,
            Vec3d basePosition,
            Vec3d desiredPosition
    ) {
        if (isDestinationClear(client, desiredPosition)) {
            return desiredPosition;
        }

        // Near corners or protrusions, keep the pendulum feeling but reduce its
        // amplitude instead of clipping the player into the terrain.
        Vec3d reducedPosition = basePosition.lerp(desiredPosition, 0.55D);
        if (isDestinationClear(client, reducedPosition)) {
            return reducedPosition;
        }

        Vec3d minimalPosition = basePosition.lerp(desiredPosition, 0.20D);
        if (isDestinationClear(client, minimalPosition)) {
            return minimalPosition;
        }

        return attachmentPosition != null ? attachmentPosition : basePosition;
    }

    private static void beginTwoAxeStabilization() {
        if (attachmentPosition == null) {
            resetTwoAxeStabilization();
            resetOneAxeHangState();
            return;
        }

        stabilizationStartPosition = attachmentPosition;
        stabilizationTargetPosition = oneAxeHangBasePosition != null
                ? oneAxeHangBasePosition
                : attachmentPosition;
        stabilizationElapsedTicks = 0;
        resetOneAxeHangState();

        if (stabilizationStartPosition.squaredDistanceTo(stabilizationTargetPosition) < 0.000001D) {
            resetTwoAxeStabilization();
        }
    }

    private static void advanceTwoAxeStabilization(MinecraftClient client) {
        if (!isTwoAxeStabilizationActive() || !hasBothAxesAttached()) {
            resetTwoAxeStabilization();
            return;
        }

        stabilizationElapsedTicks++;
        double progress = Math.min(
                1.0D,
                stabilizationElapsedTicks / (double) TWO_AXE_STABILIZE_DURATION_TICKS
        );
        Vec3d nextPosition = stabilizationStartPosition.lerp(
                stabilizationTargetPosition,
                smoothStep(progress)
        );

        if (!isDestinationClear(client, nextPosition)) {
            resetTwoAxeStabilization();
            return;
        }

        attachmentPosition = nextPosition;

        if (progress >= 1.0D) {
            attachmentPosition = stabilizationTargetPosition;
            resetTwoAxeStabilization();
        }
    }

    private static boolean isTwoAxeStabilizationActive() {
        return stabilizationStartPosition != null
                && stabilizationTargetPosition != null;
    }

    private static void resetOneAxeHangState() {
        oneAxeHangBasePosition = null;
        oneAxeSwayPhase = 0.0D;
        oneAxeSettleProgress = 0.0D;
    }

    private static void resetTwoAxeStabilization() {
        stabilizationStartPosition = null;
        stabilizationTargetPosition = null;
        stabilizationElapsedTicks = 0;
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
        if (!hasBothAxesAttached()) {
            resetStrideState();
            return;
        }

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

        if (!strideStarted && forward && tryBeginMantle(client)) {
            advanceMantle(client);
            return;
        }

        if (isStrideActive()) {
            advanceStride(client);
        }
    }

    private static boolean beginStride(MinecraftClient client, Vec3d movement) {
        if (!hasBothAxesAttached()
                || attachmentPosition == null
                || attachmentWallSide == null
                || mainAxeContactPoint == null
                || offhandAxeContactPoint == null) {
            return false;
        }

        Vec3d candidatePosition = attachmentPosition.add(movement);
        Vec3d candidateMainContact = mainAxeContactPoint.add(movement);
        Vec3d candidateOffhandContact = offhandAxeContactPoint.add(movement);
        BlockPos candidateMainWallPos = getWallBlockAtContact(
                candidateMainContact,
                mainAxeWallSide
        );
        BlockPos candidateOffhandWallPos = getWallBlockAtContact(
                candidateOffhandContact,
                offhandAxeWallSide
        );

        if (!isClimbableFace(client, candidateMainWallPos, mainAxeWallSide)
                || !isClimbableFace(client, candidateOffhandWallPos, offhandAxeWallSide)) {
            return false;
        }

        if (!isDestinationClear(client, candidatePosition)) {
            return false;
        }

        strideStartPosition = attachmentPosition;
        strideStartMainContactPoint = mainAxeContactPoint;
        strideStartOffhandContactPoint = offhandAxeContactPoint;
        strideTargetPosition = candidatePosition;
        strideTargetMainContactPoint = candidateMainContact;
        strideTargetOffhandContactPoint = candidateOffhandContact;
        strideElapsedTicks = 0;
        strideDurationTicks = randomIntInclusive(
                MIN_STRIDE_DURATION_TICKS,
                MAX_STRIDE_DURATION_TICKS
        );

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
        if (!isStrideActive() || !hasBothAxesAttached()) {
            resetStrideState();
            return;
        }

        strideElapsedTicks++;

        double progress = Math.min(1.0D, strideElapsedTicks / (double) strideDurationTicks);
        double easedProgress = smoothStep(progress);

        Vec3d nextPosition = strideStartPosition.lerp(strideTargetPosition, easedProgress);
        Vec3d nextMainContact = strideStartMainContactPoint.lerp(
                strideTargetMainContactPoint,
                easedProgress
        );
        Vec3d nextOffhandContact = strideStartOffhandContactPoint.lerp(
                strideTargetOffhandContactPoint,
                easedProgress
        );
        BlockPos nextMainWallPos = getWallBlockAtContact(nextMainContact, mainAxeWallSide);
        BlockPos nextOffhandWallPos = getWallBlockAtContact(nextOffhandContact, offhandAxeWallSide);

        if (!isClimbableFace(client, nextMainWallPos, mainAxeWallSide)
                || !isClimbableFace(client, nextOffhandWallPos, offhandAxeWallSide)
                || !isDestinationClear(client, nextPosition)) {
            resetStrideState();
            return;
        }

        attachmentPosition = nextPosition;
        mainAxeContactPoint = nextMainContact;
        mainAxeWallPos = nextMainWallPos.toImmutable();
        offhandAxeContactPoint = nextOffhandContact;
        offhandAxeWallPos = nextOffhandWallPos.toImmutable();
        refreshSharedAttachmentReference();

        if (progress >= 1.0D) {
            attachmentPosition = strideTargetPosition;
            mainAxeContactPoint = strideTargetMainContactPoint;
            mainAxeWallPos = getWallBlockAtContact(
                    strideTargetMainContactPoint,
                    mainAxeWallSide
            ).toImmutable();
            offhandAxeContactPoint = strideTargetOffhandContactPoint;
            offhandAxeWallPos = getWallBlockAtContact(
                    strideTargetOffhandContactPoint,
                    offhandAxeWallSide
            ).toImmutable();
            refreshSharedAttachmentReference();
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
        return progress * progress * (3.0D - 2.0D * progress);
    }

    private static boolean isStrideActive() {
        return strideStartPosition != null
                && strideStartMainContactPoint != null
                && strideStartOffhandContactPoint != null
                && strideTargetPosition != null
                && strideTargetMainContactPoint != null
                && strideTargetOffhandContactPoint != null
                && strideDurationTicks > 0;
    }

    private static void resetStrideState() {
        strideStartPosition = null;
        strideStartMainContactPoint = null;
        strideStartOffhandContactPoint = null;
        strideTargetPosition = null;
        strideTargetMainContactPoint = null;
        strideTargetOffhandContactPoint = null;
        strideElapsedTicks = 0;
        strideDurationTicks = 0;
        strideSwayAmplitudeDegrees = 0.0F;
    }

    private static boolean tryBeginMantle(MinecraftClient client) {
        if (!hasBothAxesAttached()
                || attachmentPosition == null
                || attachmentContactPoint == null
                || attachmentWallPos == null
                || attachmentWallSide == null) {
            return false;
        }

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

        Vec3d targetPosition = getMantleTargetPosition(
                attachmentPosition,
                attachmentWallPos,
                attachmentWallSide
        );
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

        return true;
    }

    private static Vec3d getMantleTargetPosition(
            Vec3d currentPosition,
            BlockPos ledgePos,
            Direction wallSide
    ) {
        double targetX = currentPosition.x;
        double targetZ = currentPosition.z;

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
            detachCompletely(client, "Mantle cancelled - path blocked");
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
        if (attachmentPosition == null || !hasAnyAxeAttached()) {
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

    private static void detachCompletely(MinecraftClient client, String message) {
        boolean restoreNoGravity = previousNoGravity;

        resetAttachmentState();

        client.player.setNoGravity(restoreNoGravity);
        client.player.setVelocity(0.0D, 0.0D, 0.0D);
        client.player.fallDistance = 0.0F;
        syncPreviousClimbingState(client);
    }

    private static void syncPreviousClimbingState(MinecraftClient client) {
        boolean mainHandAxeEquipped = client.player.getMainHandStack().isOf(ModItems.CLIMBING_AXE);
        boolean offhandAxeEquipped = client.player.getOffHandStack().isOf(ModItems.CLIMBING_AXE);
        BlockHitResult climbableWall = mainHandAxeEquipped || offhandAxeEquipped
                ? getClimbableWallHit(client)
                : null;

        previousClimbingState = getClimbingState(
                mainHandAxeEquipped,
                offhandAxeEquipped,
                climbableWall != null
        );
    }

    private static void resetAttachmentState() {
        mainAxeAttached = false;
        mainAxeContactPoint = null;
        mainAxeWallPos = null;
        mainAxeWallSide = null;

        offhandAxeAttached = false;
        offhandAxeContactPoint = null;
        offhandAxeWallPos = null;
        offhandAxeWallSide = null;

        previousNoGravity = false;
        attachmentPosition = null;
        attachmentContactPoint = null;
        attachmentWallPos = null;
        attachmentWallSide = null;
        resetStrideState();
        resetMantleState();
        resetOneAxeHangState();
        resetTwoAxeStabilization();
        previousMainAxeStrikeProgress = 0.0F;
        mainAxeStrikeProgress = 0.0F;
        mainAxeImpactPlayed = false;
        previousOffhandAxeStrikeProgress = 0.0F;
        offhandAxeStrikeProgress = 0.0F;
        offhandAxeImpactPlayed = false;
    }

    private static boolean isClimbableFace(
            MinecraftClient client,
            BlockPos blockPos,
            Direction wallSide
    ) {
        if (blockPos == null || wallSide == null) {
            return false;
        }

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
            return climbableWallDetected ? 6 : 1;
        }

        if (offhandAxe) {
            return climbableWallDetected ? 7 : 2;
        }

        return 0;
    }

    private static Text getStatusMessage(MinecraftClient client, int climbingState) {
        String mainAxeName = getAxeDisplayName(client, true);
        String offhandAxeName = getAxeDisplayName(client, false);
        String mainAxeClick = getClickNameForHand(client, true);
        String offhandAxeClick = getClickNameForHand(client, false);

        return switch (climbingState) {
            case 1 -> Text.literal(mainAxeName + " climbing axe equipped - no wall in reach");
            case 2 -> Text.literal(offhandAxeName + " climbing axe equipped - no wall in reach");
            case 3 -> Text.literal("Two climbing axes equipped - no wall in reach");
            case 4 -> Text.literal("Wall in reach - left click and right click to plant axes");
            case 6 -> Text.literal("Wall in reach - " + mainAxeClick
                    + " to plant " + mainAxeName.toLowerCase() + " axe");
            case 7 -> Text.literal("Wall in reach - " + offhandAxeClick
                    + " to plant " + offhandAxeName.toLowerCase() + " axe");
            default -> Text.literal("Climbing axes not equipped");
        };
    }

    private static String getAxeDisplayName(MinecraftClient client, boolean mainHand) {
        boolean mainHandIsLeftSide = client.player.getMainArm() == Arm.LEFT;
        boolean handIsLeftSide = mainHand == mainHandIsLeftSide;
        return handIsLeftSide ? "Left-side" : "Right-side";
    }

    private static String getClickNameForHand(MinecraftClient client, boolean mainHand) {
        boolean mainHandIsLeftSide = client.player.getMainArm() == Arm.LEFT;
        boolean handIsLeftSide = mainHand == mainHandIsLeftSide;
        return handIsLeftSide ? "left click" : "right click";
    }

    /**
     * Returns whether FarClimb currently owns the physical left mouse button.
     * Used by a MinecraftClient mixin to prevent vanilla's main-hand swing from
     * playing on top of the custom left-side axe strike.
     */
    public static boolean shouldCaptureLeftClick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.currentScreen != null) {
            return false;
        }

        boolean mainHandIsLeftSide = client.player.getMainArm() == Arm.LEFT;
        return (mainHandIsLeftSide
                ? client.player.getMainHandStack()
                : client.player.getOffHandStack()).isOf(ModItems.CLIMBING_AXE);
    }

    /**
     * Returns whether FarClimb currently owns the physical right mouse button.
     * This prevents vanilla item-use motion from competing with the custom
     * right-side axe strike.
     */
    public static boolean shouldCaptureRightClick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.currentScreen != null) {
            return false;
        }

        boolean mainHandIsRightSide = client.player.getMainArm() == Arm.RIGHT;
        return (mainHandIsRightSide
                ? client.player.getMainHandStack()
                : client.player.getOffHandStack()).isOf(ModItems.CLIMBING_AXE);
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
