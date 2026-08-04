# FarClimb Milestone 5.3.1

This corrective milestone refines the top-edge mantle and sideways climbing controls.

## Changes

- Slows the mantle from 15 ticks total to 32 ticks total.
- Uses a 14-tick eased lift followed by an 18-tick eased pull onto the ledge.
- Corrects wall-relative sideways movement:
  - `A` moves left.
  - `D` moves right.
- Keeps `S` as downward climbing movement.

## Test

1. Attach to a wall and verify `A` moves left and `D` moves right from the player's view.
2. Reach a clear top edge and continue holding `W`.
3. Confirm the pull-over animation takes about 1.6 seconds and still completes safely.
