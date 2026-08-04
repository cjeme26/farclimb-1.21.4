# FarClimb Milestone 6.3.3a

This follow-up adjusts the new wall-anchored third-person pickaxe renderer.

## Fixes

- Moves each planted pickaxe outward from the wall face before rendering.
- Adds a small local depth correction so the iron head appears embedded instead of the whole model being buried inside the block.
- Keeps the 6.3.3 behavior where attached third-person pickaxes stay anchored to the wall instead of rotating with the player.

## What to test

- One attached axe on a flat wall.
- Two attached axes on a flat wall.
- Hanging near a block edge.
- Hanging in a corner.
- Detaching one axe and confirming it returns to the player hand.
