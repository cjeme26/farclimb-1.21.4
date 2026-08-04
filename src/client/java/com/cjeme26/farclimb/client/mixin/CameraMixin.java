package com.cjeme26.farclimb.client.mixin;

import com.cjeme26.farclimb.client.FarClimbClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    @Final
    private Quaternionf rotation;

    @Shadow
    public abstract Vec3d getPos();

    @Shadow
    protected abstract void setPos(Vec3d pos);

    @Inject(method = "update", at = @At("TAIL"))
    private void farclimb$applyClimbingSway(
            BlockView area,
            Entity focusedEntity,
            boolean thirdPerson,
            boolean inverseView,
            float tickDelta,
            CallbackInfo callbackInfo
    ) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Keep FarClimb sway first-person-only and tied to the local player.
        if (thirdPerson || focusedEntity != client.player) {
            return;
        }

        Vec3d positionOffset = FarClimbClient.getCameraPositionOffset(tickDelta);
        if (positionOffset.lengthSquared() >= 0.000001D) {
            this.setPos(this.getPos().add(positionOffset));
        }

        float rollDegrees = FarClimbClient.getCameraRollDegrees(tickDelta);
        if (Math.abs(rollDegrees) >= 0.001F) {
            this.rotation.rotateZ((float) Math.toRadians(rollDegrees));
        }
    }
}
