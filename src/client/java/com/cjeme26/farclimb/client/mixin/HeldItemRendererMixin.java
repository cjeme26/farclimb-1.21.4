package com.cjeme26.farclimb.client.mixin;

import com.cjeme26.farclimb.client.FarClimbClient;
import com.cjeme26.farclimb.item.ModItems;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Unique
    private boolean farclimb$plantedTransformPushed;

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void farclimb$beginPlantedAxeTransform(
            AbstractClientPlayerEntity player,
            float tickDelta,
            float pitch,
            Hand hand,
            float swingProgress,
            ItemStack item,
            float equipProgress,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo callbackInfo
    ) {
        farclimb$plantedTransformPushed = false;

        if (!item.isOf(ModItems.CLIMBING_AXE) || !FarClimbClient.isAxeAttached(hand)) {
            return;
        }

        matrices.push();
        FarClimbClient.applyFirstPersonAxeTransform(matrices, hand, tickDelta);
        farclimb$plantedTransformPushed = true;
    }

    @Inject(method = "renderFirstPersonItem", at = @At("RETURN"))
    private void farclimb$endPlantedAxeTransform(
            AbstractClientPlayerEntity player,
            float tickDelta,
            float pitch,
            Hand hand,
            float swingProgress,
            ItemStack item,
            float equipProgress,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo callbackInfo
    ) {
        if (!farclimb$plantedTransformPushed) {
            return;
        }

        matrices.pop();
        farclimb$plantedTransformPushed = false;
    }
}
