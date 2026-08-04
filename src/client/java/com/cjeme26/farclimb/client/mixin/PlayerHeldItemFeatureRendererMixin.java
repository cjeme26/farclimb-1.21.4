package com.cjeme26.farclimb.client.mixin;

import com.cjeme26.farclimb.client.FarClimbClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.PlayerHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides only the local player's normal hand-bound pickaxe after the custom
 * world-anchored version reaches the wall. First-person rendering is separate
 * and remains unchanged.
 */
@Mixin(PlayerHeldItemFeatureRenderer.class)
public abstract class PlayerHeldItemFeatureRendererMixin {

    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    private void farclimb$hideHandBoundAttachedAxe(
            PlayerEntityRenderState state,
            ItemRenderState itemState,
            Arm arm,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo callbackInfo
    ) {
        boolean leftSide = arm == Arm.LEFT;
        if (FarClimbClient.shouldHideVanillaThirdPersonAxe(state.id, leftSide)) {
            callbackInfo.cancel();
        }
    }
}
