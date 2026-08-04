package com.cjeme26.farclimb.client.mixin;

import com.cjeme26.farclimb.client.FarClimbClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin {

    @Unique
    private static final float ATTACHED_ARM_DEFAULT_PITCH = -1.82F;
    @Unique
    private static final float ATTACHED_ARM_OUTWARD_ROLL = 0.10F;
    @Unique
    private static final float ATTACHED_ARM_STRIDE_LIFT = 0.24F;

    @Unique
    private static final float FREE_ARM_PITCH = 0.24F;
    @Unique
    private static final float FREE_ARM_OUTWARD_ROLL = 0.30F;
    @Unique
    private static final float FREE_ARM_SWAY_LAG = 0.18F;

    @Inject(
            method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V",
            at = @At("TAIL")
    )
    private void farclimb$applyThirdPersonClimbingPose(
            PlayerEntityRenderState state,
            CallbackInfo callbackInfo
    ) {
        if (!FarClimbClient.shouldApplyThirdPersonClimbingPose(state.id)) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        PlayerEntityModel model = (PlayerEntityModel) (Object) this;
        boolean leftAttached = FarClimbClient.isVisibleSideAxeAttached(true);
        boolean rightAttached = FarClimbClient.isVisibleSideAxeAttached(false);
        boolean bothAttached = leftAttached && rightAttached;
        float tickDelta = state.age - (float) Math.floor(state.age);
        float pendulum = FarClimbClient.getThirdPersonPendulumAmount(tickDelta);

        // PlayerEntityModel's body, arms, and legs are sibling parts rather than
        // a true parent/child skeleton. Rotating the torso alone therefore opens
        // visible seams at the shoulders and waist. Keep the torso transform
        // stable and express the climbing lean through the limbs and the actual
        // one-axe body displacement instead.
        model.body.pitch = 0.0F;
        model.body.yaw = 0.0F;
        model.body.roll = 0.0F;

        poseArm(model.leftArm, true, leftAttached, bothAttached, pendulum, tickDelta);
        poseArm(model.rightArm, false, rightAttached, bothAttached, pendulum, tickDelta);

        // Bent legs create a braced silhouette without rotating the torso away
        // from their fixed hip pivots. One-axe hanging gets a small alternating
        // motion, but the values stay restrained to avoid separating at the hips.
        if (bothAttached) {
            model.leftLeg.pitch = 0.25F;
            model.rightLeg.pitch = -0.16F;
            model.leftLeg.yaw = -0.03F;
            model.rightLeg.yaw = 0.03F;
            model.leftLeg.roll = -0.055F;
            model.rightLeg.roll = 0.055F;
        } else {
            float supportSide = leftAttached ? -1.0F : 1.0F;
            model.leftLeg.pitch = 0.29F + pendulum * 0.035F;
            model.rightLeg.pitch = -0.23F - pendulum * 0.035F;
            model.leftLeg.yaw = -supportSide * 0.025F;
            model.rightLeg.yaw = supportSide * 0.025F;
            model.leftLeg.roll = -0.075F + supportSide * 0.025F;
            model.rightLeg.roll = 0.075F + supportSide * 0.025F;
        }

        // Outer skin pieces are sibling parts, not children of the base limbs.
        // Keep each overlay's own baked/default offset (important for slim arms
        // and the slightly expanded jacket/sleeve/pants geometry), then apply the
        // exact delta that FarClimb added to the corresponding base part. This
        // avoids both failure modes from the previous attempts:
        //   - angle-only copying left moving pivots behind;
        //   - full copyTransform erased the overlay's own default offset.
        syncOverlayToBase(model.leftArm, model.leftSleeve);
        syncOverlayToBase(model.rightArm, model.rightSleeve);
        syncOverlayToBase(model.leftLeg, model.leftPants);
        syncOverlayToBase(model.rightLeg, model.rightPants);
        syncOverlayToBase(model.body, model.jacket);
        syncOverlayToBase(model.head, model.hat);
    }

    @Unique
    private static void poseArm(
            ModelPart arm,
            boolean leftSide,
            boolean attached,
            boolean bothAttached,
            float pendulum,
            float tickDelta
    ) {
        float side = leftSide ? -1.0F : 1.0F;

        if (!attached) {
            // The free axe hangs beside the body and lags behind the pendulum.
            arm.pitch = FREE_ARM_PITCH - pendulum * 0.055F;
            arm.yaw = -side * 0.07F;
            arm.roll = side * FREE_ARM_OUTWARD_ROLL - pendulum * FREE_ARM_SWAY_LAG;
            return;
        }

        Vec3d contact = FarClimbClient.getVisibleSideAxeContactPoint(leftSide);
        float targetPitch = ATTACHED_ARM_DEFAULT_PITCH;
        float targetYaw = side * 0.10F;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && contact != null) {
            Vec3d playerPosition = client.player.getPos();
            float bodyYawRadians = (float) Math.toRadians(client.player.getBodyYaw());
            Vec3d forward = new Vec3d(
                    -Math.sin(bodyYawRadians),
                    0.0D,
                    Math.cos(bodyYawRadians)
            );
            Vec3d right = new Vec3d(forward.z, 0.0D, -forward.x);

            Vec3d shoulder = playerPosition.add(
                    right.multiply(leftSide ? -0.22D : 0.22D)
                            .add(0.0D, 1.43D, 0.0D)
            );
            Vec3d toContact = contact.subtract(shoulder);
            double forwardDistance = Math.max(0.08D, toContact.dotProduct(forward));
            double sideDistance = toContact.dotProduct(right);

            targetPitch = (float) (
                    -Math.PI / 2.0D
                            - Math.atan2(toContact.y, forwardDistance)
            );
            targetPitch = clamp(targetPitch, -2.30F, -1.45F);
            targetYaw = clamp(
                    (float) -Math.atan2(sideDistance, forwardDistance),
                    -0.48F,
                    0.48F
            );
        }

        float plant = FarClimbClient.getVisibleSideAxePlantProgress(leftSide, tickDelta);
        float strideLift = FarClimbClient.getVisibleSideStridePulse(leftSide, tickDelta);
        float plantedPitch = targetPitch - strideLift * ATTACHED_ARM_STRIDE_LIFT;
        float plantedRoll = -side * ATTACHED_ARM_OUTWARD_ROLL;

        arm.pitch += (plantedPitch - arm.pitch) * plant;
        arm.yaw += (targetYaw - arm.yaw) * plant;
        arm.roll += (plantedRoll - arm.roll) * plant;

        if (!bothAttached) {
            arm.roll += pendulum * 0.025F;
        }
    }


    @Unique
    private static void syncOverlayToBase(ModelPart base, ModelPart overlay) {
        ModelTransform baseDefault = base.getDefaultTransform();
        ModelTransform overlayDefault = overlay.getDefaultTransform();

        overlay.pivotX = overlayDefault.pivotX()
                + (base.pivotX - baseDefault.pivotX());
        overlay.pivotY = overlayDefault.pivotY()
                + (base.pivotY - baseDefault.pivotY());
        overlay.pivotZ = overlayDefault.pivotZ()
                + (base.pivotZ - baseDefault.pivotZ());

        overlay.pitch = overlayDefault.pitch()
                + (base.pitch - baseDefault.pitch());
        overlay.yaw = overlayDefault.yaw()
                + (base.yaw - baseDefault.yaw());
        overlay.roll = overlayDefault.roll()
                + (base.roll - baseDefault.roll());

        overlay.xScale = overlayDefault.xScale()
                + (base.xScale - baseDefault.xScale());
        overlay.yScale = overlayDefault.yScale()
                + (base.yScale - baseDefault.yScale());
        overlay.zScale = overlayDefault.zScale()
                + (base.zScale - baseDefault.zScale());
    }

    @Unique
    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
