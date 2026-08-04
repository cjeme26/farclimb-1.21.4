package com.cjeme26.farclimb.client.mixin;

/**
 * Player-model bridge used by the player-only render hook.
 */
public interface FarClimbPlayerModelAccess {

    /**
     * Synchronizes the finished climbing pose to the outer skin layers.
     */
    void farclimb$syncOuterLayersForRender();
}
