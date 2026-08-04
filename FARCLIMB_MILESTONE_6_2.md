# FarClimb Milestone 6.2

Milestone 6.2 adds first-person planted-axe visuals while continuing to use the vanilla iron pickaxe model.

## What changed

- An attached axe moves from the ordinary held-item pose into a raised, forward planted pose.
- Main-hand and offhand axes are transformed independently.
- The stored wall contact point adds small horizontal and vertical corrections to the pose.
- The plant transition eases in over roughly five game ticks instead of snapping instantly.
- During a two-axe climbing stride, one axe briefly lifts and retracts; the active visual hand alternates on the next stride.
- Releasing an axe immediately returns that hand to Minecraft's normal held-item rendering.
- One-axe pendulum behavior, movement, camera sway, attachment logic, and mantling are unchanged.

## Suggested testing

1. Plant only the left-side axe and confirm only the left visible pickaxe moves into the wall pose.
2. Plant only the right-side axe and repeat.
3. Attach both axes at slightly different points on the same wall and confirm their visible positions differ slightly.
4. Hold W for several strides and check that the small repositioning pulse alternates between hands.
5. Test A, D, and S climbing.
6. Release each axe independently and confirm its normal held pose returns.
7. Hang from one axe and confirm the planted pickaxe remains visually raised while the body and camera sway.
8. Mantle a ledge and confirm the existing mantle behavior still works.

## Scope

This milestone changes first-person rendering only. A third-person arm and body pose remains planned for a later milestone.
