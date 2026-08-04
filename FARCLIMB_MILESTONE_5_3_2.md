# FarClimb Milestone 5.3.2

This corrective milestone refines wall spacing and the top-edge mantle.

## Changes

- Snaps the player to a consistent small clearance from the wall when attachment begins.
- Prevents attachment if that close position is obstructed.
- Slows the upward pull from 14 ticks to 24 ticks.
- Shortens the inward ledge movement from 18 ticks to 6 ticks.
- Reduces the slight downward settling at the end of the mantle.
- Places the player near the ledge edge instead of sliding to the centre of the block.

## Test

1. Attach from several different distances and confirm the player always hangs close to the wall.
2. Climb to a clear ledge and hold `W`.
3. Confirm the upward pull feels slower and controlled.
4. Confirm the final movement onto the block is brief and does not resemble a long slide.
