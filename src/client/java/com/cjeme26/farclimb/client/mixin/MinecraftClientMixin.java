package com.cjeme26.farclimb.client.mixin;

import com.cjeme26.farclimb.client.FarClimbClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void farclimb$captureLeftAxeClick(CallbackInfoReturnable<Boolean> callbackInfo) {
        if (FarClimbClient.shouldCaptureLeftClick()) {
            callbackInfo.setReturnValue(false);
        }
    }

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void farclimb$captureRightAxeClick(CallbackInfo callbackInfo) {
        if (FarClimbClient.shouldCaptureRightClick()) {
            callbackInfo.cancel();
        }
    }
}
