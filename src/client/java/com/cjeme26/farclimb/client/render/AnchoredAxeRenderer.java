package com.cjeme26.farclimb.client.render;

import com.cjeme26.farclimb.client.FarClimbClient;
import com.cjeme26.farclimb.item.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

/**
 * Renders attached third-person axes with a purpose-built model whose local
 * origin is the planted pick tip. This avoids every hidden pivot and display
 * transform used by Minecraft's ordinary item renderer.
 */
public final class AnchoredAxeRenderer {
    // The modeled pick crosses the wall plane slightly, so only a tiny outward
    // offset is needed to prevent z-fighting at the contact.
    private static final double WALL_OUTWARD_OFFSET = 0.010D;

    private static final float WALL_PITCH_DEGREES = -7.0F;
    private static final float LEFT_HANDLE_ROLL_DEGREES = -24.0F;
    private static final float RIGHT_HANDLE_ROLL_DEGREES = 24.0F;

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
        Vec3d plantedTip = contact.add(
                wallSide.getOffsetX() * WALL_OUTWARD_OFFSET,
                0.0D,
                wallSide.getOffsetZ() * WALL_OUTWARD_OFFSET
        );

        matrices.push();
        matrices.translate(
                plantedTip.x - cameraPosition.x,
                plantedTip.y - cameraPosition.y,
                plantedTip.z - cameraPosition.z
        );

        // Local +Z points away from the wall. Since the model's spike begins at
        // local Z=0 and extends slightly behind it, the tip remains planted for
        // all four horizontal wall directions.
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(
                getWallYawDegrees(wallSide)
        ));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(
                WALL_PITCH_DEGREES
        ));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(
                leftSide
                        ? LEFT_HANDLE_ROLL_DEGREES
                        : RIGHT_HANDLE_ROLL_DEGREES
        ));

        TemporaryAnchoredAxeModel.render(matrices, consumers, light);
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
