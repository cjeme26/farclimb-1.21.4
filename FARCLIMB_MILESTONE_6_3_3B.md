# FarClimb Milestone 6.3.3b

This rendition refines the world-anchored third-person pickaxes introduced in 6.3.3.

## Changes

- Gives the visible left and right axes separately calibrated local transforms.
- Renders both world objects with the same item handedness, avoiding a second hidden mirror from Minecraft's item renderer.
- Slightly reduces the world-axe scale.
- Constrains new attachment points to a believable reach area around the matching shoulder.
- Limits vertical reattachment reach so an axe cannot jump far above the player after a drop.
- Biases left/right contacts toward their matching body sides while still allowing aim within a small reach window.
- Keeps contacts inset from block-face edges.

## Test

- Plant both axes near the centre of a flat wall.
- Release and replant each axe while aiming high and low.
- Drop briefly, catch the wall again, and compare axe height with the shoulders.
- Test near block edges and corners.
