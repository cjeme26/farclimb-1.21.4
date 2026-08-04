# FarClimb Milestone 6.3.2c

This is a safer player-only outer-skin-layer synchronization attempt built from the stable Milestone 6.3.2a source.

## Changes

- Removes outer-layer synchronization from `PlayerEntityModel#setAngles`.
- Adds a player-render-stage synchronization point after the complete pose is prepared.
- Runs only when the render state is a player state.
- Runs only when the model is a `PlayerEntityModel`.
- Runs only for the local player while at least one climbing axe is attached.
- Copies the final base arm, leg, torso and head transforms to the matching sleeve, trouser, jacket and hat layers.
- Preserves the user's skin-layer visibility settings.
- Does not mix into the global `Model` renderer used by Milestone 6.3.2b.

## Safety

The failed 6.3.2b build intercepted every model render in the game. This version hooks the living-entity renderer and immediately exits for every non-player or non-climbing render, avoiding the startup/render recursion risk.

## Test

1. Confirm the game reaches the title screen at normal speed.
2. Enable all skin customization layers.
3. Attach both axes and inspect the shoulders, jacket and trousers.
4. Hang from one axe through both ends of the pendulum sway.
5. Detach and confirm normal walking renders normally.
6. Mantle over an edge and confirm no outer layer remains stuck.

Axe edge/corner anchoring remains intentionally unchanged for the next rendering milestone.
