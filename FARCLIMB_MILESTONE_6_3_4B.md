# FarClimb Milestone 6.3.4b

Corrects the temporary anchored climbing-axe model's local depth layout.

## Changes

- Keeps local +Z aligned with the wall's outward normal.
- Rebuilds the pick spike so it extends mostly along local -Z into the wall.
- Keeps the head and handle only slightly outside the wall surface.
- Retains the diagnostic contact marker and local axes from 6.3.4a.
- Leaves climbing movement, first-person rendering, sway, camera limits, and mantling unchanged.

## Test

- Attach one axe to a flat wall and inspect it from behind and from the side.
- Attach both axes.
- Test walls facing north, south, east, and west.
- Confirm the handle no longer sits inside the player's body.
