# FarClimb Milestone 6.3.1

Milestone 6.3.1 corrects the outer player-skin layers and constrains the climber around the planted wall face.

## What changed

- Sleeves, pants overlays, and the jacket now copy only rotation from the posed base model parts.
- Their original pivots and scale remain intact, preventing the outer skin layer from separating into floating cubes.
- With both axes attached, the player's torso remains square to the wall.
- With both axes attached, the head/camera can turn approximately 48 degrees left or right from the wall-facing direction.
- With one axe attached, the head/camera can turn approximately 88 degrees left or right.
- During a one-axe hang, the torso follows 45 percent of the permitted head turn, allowing the body to rotate around the supporting arm without turning completely away.
- Releasing the final axe immediately restores normal unrestricted looking.
- First-person axe planting, third-person arm poses, pendulum movement, climbing strides, and mantling are otherwise unchanged.

## Suggested testing

1. Use a skin with clearly visible sleeves, jacket, and pants overlays.
2. Attach both axes and switch to third person. Confirm the outer layers remain directly on the arms, legs, and torso.
3. Move the mouse left and right. Confirm the torso continues facing the wall and the view stops at a moderate angle on each side.
4. Release one axe. Confirm the permitted turn becomes wider and the torso follows part of the turn.
5. Reattach the second axe. Confirm the torso returns square to the wall.
6. Release both axes. Confirm normal 360-degree looking returns immediately.
7. Test on walls facing north, south, east, and west to confirm the limit is relative to the actual wall face.
8. Climb and mantle to confirm the existing movement remains unchanged.

## Scope

This milestone constrains the player model and fixes skin overlays. The vanilla third-person pickaxe is still held by the hand renderer, so exact world-anchored pickaxe placement from every viewing angle remains planned for Milestone 6.3.2.

## Compatibility correction

The wall-facing yaw calculation now uses an explicit horizontal-direction switch.
This replaces `Direction.asRotation()`, which is not available in the project's
Yarn 1.21.4 mappings.
