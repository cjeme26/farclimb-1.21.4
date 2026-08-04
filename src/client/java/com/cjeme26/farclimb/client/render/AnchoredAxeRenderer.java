package com.cjeme26.farclimb.client.render;

import com.cjeme26.farclimb.client.FarClimbClient;
import com.cjeme26.farclimb.item.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.Arm;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

/**
 * Renders planted climbing axes as world objects rather than hand children.
 *
 * The important detail in 6.3.3c is that the item is no longer treated as if
 * its centre were the wall contact. A calibrated point near the iron head is
 * translated to the origin first. All handle rotation then happens around that
 * planted head pivot.
 */
public final class AnchoredAxeRenderer {
    private static final float WORLD_AXE_SCALE = 0.72F;
    private static final double WALL_OUTWARD_OFFSET = 0.105D;

    private static final float WALL_PITCH_DEGREES = -5.0F;
    private static final float LEFT_HANDLE_ROLL_DEGREES = -31.0F;
    private static final float RIGHT_HANDLE_ROLL_DEGREES = 31.0F;

    /*
     * Approximate location of the metal-head contact in the model after the
     * vanilla FIXED transform. Translating by the negative of this vector makes
     * that point the local origin, so the handle rotates around the wall bite.
     * Keeping one common pivot also prevents left/right offsets from drifting
     * independently.
     */
    private static final double PICK_HEAD_PIVOT_X = 0.0D;
    private static final double PICK_HEAD_PIVOT_Y = 0.235D;
    private static final double PICK_HEAD_PIVOT_Z = -0.010D;

    private AnchoredAxeRenderer() {
    }

    public static void initialize() {
        WorldRenderEvents.AFTER_ENTITIES.register(AnchoredAxeRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null
                || client.world == null
                || client.options.getPerspective().isFirstPerson()
                || FarClimbClient.isMantling()) {
            return;
        }

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (matrices == null || consumers == null) {
            return;
        }

        float tickDelta = context.tickCounter().getTickDelta(false);
        renderAxe(context, matrices, consumers, tickDelta, true);
        renderAxe(context, matrices, consumers, tickDelta, false);
    }

    private static void renderAxe(
            WorldRenderContext context,
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            float tickDelta,
            boolean leftSide
    ) {
        if (!FarClimbClient.isVisibleSideAxeAttached(leftSide)) {
            return;
        }

        float plantProgress = FarClimbClient.getVisibleSideAxePlantProgress(
                leftSide,
                tickDelta
        );
        if (plantProgress < 0.98F) {
            return;
        }

        Vec3d contact = FarClimbClient.getVisibleSideAxeContactPoint(leftSide);
        Direction wallSide = FarClimbClient.getVisibleSideAxeWallSide(leftSide);
        BlockPos wallPos = FarClimbClient.getVisibleSideAxeWallPos(leftSide);
        if (contact == null || wallSide == null || wallPos == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        boolean mainHandIsLeftSide = client.player.getMainArm() == Arm.LEFT;
        boolean mainHand = leftSide == mainHandIsLeftSide;
        ItemStack stack = mainHand
                ? client.player.getMainHandStack()
                : client.player.getOffHandStack();
        if (!stack.isOf(ModItems.CLIMBING_AXE)) {
            return;
        }

        Vec3d cameraPosition = context.camera().getPos();
        int light = WorldRenderer.getLightmapCoordinates(context.world(), wallPos);
        Vec3d renderContact = contact.add(
                wallSide.getOffsetX() * WALL_OUTWARD_OFFSET,
                0.0D,
                wallSide.getOffsetZ() * WALL_OUTWARD_OFFSET
        );

        matrices.push();
        matrices.translate(
                renderContact.x - cameraPosition.x,
                renderContact.y - cameraPosition.y,
                renderContact.z - cameraPosition.z
        );

        // Establish a wall-local coordinate system first.
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(
                getWallYawDegrees(wallSide)
        ));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(
                WALL_PITCH_DEGREES
        ));

        // Mirror only the direction in which the handle descends. Both axes use
        // the same contact pivot and the same item-render handedness.
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(
                leftSide
                        ? LEFT_HANDLE_ROLL_DEGREES
                        : RIGHT_HANDLE_ROLL_DEGREES
        ));

        /*
         * Scale before the pivot translation. With MatrixStack's multiplication
         * order, this means the pivot vector is expressed in the transformed
         * FIXED-item coordinate system and is scaled together with the model.
         */
        matrices.scale(WORLD_AXE_SCALE, WORLD_AXE_SCALE, WORLD_AXE_SCALE);
        matrices.translate(
                -PICK_HEAD_PIVOT_X,
                -PICK_HEAD_PIVOT_Y,
                -PICK_HEAD_PIVOT_Z
        );

        client.getItemRenderer().renderItem(
                client.player,
                stack,
                ModelTransformationMode.FIXED,
                false,
                matrices,
                consumers,
                context.world(),
                light,
                OverlayTexture.DEFAULT_UV,
                client.player.getId() + (leftSide ? 0 : 1)
        );
        matrices.pop();
    }

    private static float getWallYawDegrees(Direction wallSide) {
        return switch (wallSide) {
            case SOUTH -> 0.0F;
            case WEST -> -90.0F;
            case NORTH -> 180.0F;
            case EAST -> 90.0F;
            default -> 0.0F;
        };
    }
}
