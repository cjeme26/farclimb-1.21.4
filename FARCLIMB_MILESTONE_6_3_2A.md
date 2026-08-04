# FarClimb Milestone 6.3.2a

This is a focused outer-skin-layer rendering correction built on Milestone 6.3.2.

## Changes

- Keeps the camera/input limit behavior from 6.3.2 unchanged.
- Keeps the stable torso/leg pose from 6.3.2 unchanged.
- Replaces full `copyTransform` calls for skin overlays.
- Preserves each overlay part's own baked/default pivot, rotation and scale.
- Applies only the base limb's change relative to its own default transform.
- Covers sleeves, trousers, jacket and hat.

## Why

The outer player layers are separate root-level model parts. Copying only angles can
leave moving pivots behind, while copying the entire transform can erase the overlay
part's own default offset. This version combines both requirements by transferring the
base part's transform delta onto the overlay's own default transform.

## Test

Enable every skin customization layer, then test:

1. Both axes attached, viewed from the front and rear.
2. One-axe hanging through several full sway cycles.
3. Slim-arm and normal-arm skins if available.
4. Jacket and trouser layers while climbing upward and sideways.
5. Detaching and returning to ordinary walking.

Axe edge/corner anchoring remains intentionally unchanged for the following milestone.
