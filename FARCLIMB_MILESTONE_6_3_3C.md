# FarClimb Milestone 6.3.3c

This revision replaces centre-based item placement with a calibrated pick-head
pivot for world-anchored third-person pickaxes.

## Changes

- Uses one common contact pivot near the iron head for both axes.
- Rotates each handle around that planted head instead of around the centre of
  the vanilla item model.
- Mirrors only the handle roll for left and right sides.
- Reduces the world-model size slightly.
- Stops rendering wall-anchored axes immediately when mantling begins.
- Leaves first-person axes, climbing movement, contact selection, camera limits,
  sway and skin-layer behavior unchanged.

## Test

1. Plant both axes on a flat wall.
2. Release and replant each side separately.
3. Drop a short distance and reattach.
4. Mantle over a ledge and confirm the wall copies disappear immediately.
5. Test walls facing north, south, east and west.
