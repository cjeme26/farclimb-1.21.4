# FarClimb Milestone 6.3.2b

This is a focused final-render-stage skin-overlay correction built on Milestone 6.3.2a.

## Changes

- Keeps climbing, swaying, mantling, look limits, and axe rendering unchanged.
- Removes outer-layer synchronization from `PlayerEntityModel#setAngles`.
- Records whether the current player model is using FarClimb's climbing pose.
- Synchronizes sleeves, trousers, jacket, and hat immediately before the model renders.
- Copies the completed base-part pivot, rotation, and scale directly.
- Preserves each overlay's independent visibility/hidden flags from skin settings.

## Why

Minecraft can perform model setup and visibility work after pose calculation. Earlier
versions synchronized the overlays at the end of `setAngles`, which could still leave
an overlay with stale transforms by render time. This version hooks the final
`Model#render` call, so the base limbs are the last source of truth before vertices are
drawn.

The inflated size of sleeves, trousers, jacket, and hat is baked into their cuboids;
their runtime pivot and rotation should match the corresponding base part exactly.

## Test

Enable every skin customization layer, then test:

1. Both axes attached from the front, rear, and both sides.
2. One-axe hanging through complete left/right sway cycles.
3. The free arm at both extremes of the sway.
4. Bent legs and trouser overlays while climbing upward and sideways.
5. Detaching, mantling, and returning to ordinary walking.
6. Normal-width and slim-arm skins if available.

Axe edge/corner anchoring remains intentionally unchanged for the next rendering
milestone.
