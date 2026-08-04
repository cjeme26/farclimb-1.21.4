# FarClimb Milestone 5.0

Milestone 5 adds basic movement while attached to a continuous wall.

## Added

- Hold Sneak to remain attached, as in Milestone 4.
- Press W to climb upward at 0.10 blocks per tick.
- Press S to climb downward at 0.05 blocks per tick.
- Press A or D to traverse sideways at 0.07 blocks per tick.
- Upward movement is deliberately faster than sideways or downward movement.
- The player remains stationary when no movement key is pressed.
- Movement follows the wall face that was originally attached to, even if the camera turns.
- The contact point advances across neighboring full blocks while climbing.
- Movement stops at gaps, wall edges, ceilings, floors, and other collision obstructions.
- Releasing Sneak, removing either axe, or losing the current supporting wall still detaches the player.

## Deliberately not included yet

- Independent left/right axe strikes
- One-axe hanging or swaying
- Grip or stamina
- Mantling over the top of a wall
- Turning around outside corners
- Custom climbing animations

## Test

1. Build a flat wall at least five blocks wide and five blocks tall.
2. Equip a Climbing Axe in each hand.
3. Face the wall from close range and hold Sneak to attach.
4. Hold W and confirm upward movement is the fastest.
5. Hold S and confirm downward movement is slower.
6. Hold A and D separately and confirm sideways traversal follows the wall.
7. Release all movement keys while continuing to hold Sneak; the player should remain fixed.
8. Move toward a gap or edge; movement should stop rather than allowing the player to float.
9. Move toward a ceiling or obstruction; movement should stop before clipping into it.
10. Release Sneak or remove an axe and confirm normal detachment still works.
