# FarClimb Milestone 5.1

Milestone 5.1 refines the two-axe climbing movement introduced in 5.0.

## Added

- Climbing now happens in short movement steps instead of sliding every tick.
- Each step has a small randomized distance.
- Each step is followed by a brief randomized pause.
- Upward climbing remains fastest.
- Sideways traversing is slower than upward movement.
- Downward climbing is the slowest movement.
- Pausing movement resets the step timer, so the next input responds immediately.
- Diagonal input combines one vertical and one sideways step.

## Initial tuning

- Upward step: 0.12 to 0.17 blocks
- Sideways step: 0.08 to 0.12 blocks
- Downward step: 0.06 to 0.09 blocks
- Pause after each step: 1 to 3 ticks

The variation is intentionally narrow. It should look less mechanical without taking precise control away from the player.

## Not included yet

- Pulling over the top edge of a wall (planned for 5.2)
- First-person camera sway (planned for 5.3)
- One-axe hanging or grip loss (planned for 6.0)
