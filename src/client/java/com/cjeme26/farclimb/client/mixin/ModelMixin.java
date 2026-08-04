package com.cjeme26.farclimb.client.mixin;

import net.minecraft.client.model.Model;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Runs after every pose calculation but immediately before Model renders its
 * root parts. This is deliberately later than PlayerEntityModel#setAngles, so
 * vanilla visibility/setup work cannot leave the skin overlays with stale
 * transforms.
 */
@Mixin(Model.class)
public abstract class ModelMixin {

    @Inject(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V",
            at = @At("HEAD")
    )
    private void farclimb$syncPlayerOuterLayersAtRenderTime(
            MatrixStack matrices,
            VertexConsumer vertices,
            int light,
            int overlay,
            int color,
            CallbackInfo callbackInfo
    ) {
        if ((Object) this instanceof FarClimbPlayerModelAccess playerModelAccess) {
            playerModelAccess.farclimb$syncOuterLayersForRender();
        }
    }
}
