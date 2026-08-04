package com.cjeme26.farclimb.client.mixin;

import com.cjeme26.farclimb.client.FarClimbClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntityLookMixin {

    @ModifyVariable(
            method = "changeLookDirection(DD)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private double farclimb$limitHorizontalLookInput(double cursorDeltaX) {
        return FarClimbClient.clampClimbingLookDeltaX(this, cursorDeltaX);
    }
}
