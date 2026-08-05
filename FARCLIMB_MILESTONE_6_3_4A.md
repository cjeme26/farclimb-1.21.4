# FarClimb Milestone 6.3.4a

This is a temporary diagnostic-and-correction build for the custom anchored axe coordinate system.

## Changes

- Corrects the missing ModelPart world-space Y-axis flip.
- Removes the previous wall pitch assumption.
- Aligns the temporary axe only from the wall normal and one left/right handle angle.
- Draws a white cube at the exact saved contact point.
- Draws local axes from the contact:
  - red = local +X across the wall
  - green = corrected local +Y down the wall
  - blue = local +Z outward from the wall
- Keeps anchored axes hidden during mantling.

## Test plan

Test on north-, south-, east-, and west-facing walls. For every wall:

1. The white cube should remain at the block face contact.
2. The blue rod should point outward from the wall.
3. The green rod should point downward.
4. The red rod should run sideways across the wall.
5. The axe handle should extend downward from the contact rather than upward.

The colored markers are intentionally temporary and will be removed after the coordinate system is confirmed.
