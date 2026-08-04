package com.cjeme26.farclimb.client.mixin;

import com.cjeme26.farclimb.client.FarClimbClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Performs the skin-overlay synchronization only inside the living-entity
 * renderer and only when the current model/state are the local climbing player.
 *
 * This replaces the failed 6.3.2b global Model hook. The injection runs after
 * PlayerEntityModel#setAngles has prepared the complete pose but before the
 * renderer selects/draws the player's base render layer.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Shadow
    protected EntityModel<?> model;

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getRenderLayer(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/render/RenderLayer;",
                    shift = At.Shift.BEFORE
            )
    )
    private void farclimb$syncLocalPlayerOuterLayers(
            LivingEntityRenderState state,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo callbackInfo
    ) {
        if (!(state instanceof PlayerEntityRenderState playerState)) {
            return;
        }

        if (!FarClimbClient.shouldApplyThirdPersonClimbingPose(playerState.id)) {
            return;
        }

        if (!(this.model instanceof PlayerEntityModel playerModel)) {
            return;
        }

        ((FarClimbPlayerModelAccess) playerModel)
                .farclimb$syncOuterLayersForRender();
    }
}
