# FarClimb Milestone 6.3.2

Rendering-polish pass for the third-person climbing pose and wall-facing look limits.

## Changes

- Outer skin layers now copy the complete final transform of their base body parts.
- Fixes detached sleeves, jacket pieces, and trouser overlays during climbing and one-axe sway.
- Keeps the torso transform stable so the independently rendered legs do not separate at the waist.
- Retunes the leg pose to preserve a climbing silhouette without opening visible model seams.
- Horizontal mouse input is clamped before vanilla applies it.
- Removes the repeated camera snap/jitter when pushing against a look limit.
- When the second axe narrows the allowed look range, the view eases back inside the new range.
- Body yaw eases separately from head/camera yaw.

## Not included yet

Pickaxes are still rendered from the player's hands in third person. World-anchored axe rendering, face-edge clamping, and corner handling are planned for Milestone 6.3.3.
