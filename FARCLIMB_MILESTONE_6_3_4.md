# FarClimb Milestone 6.3.4

This milestone replaces only the third-person wall-anchored copy of the vanilla
iron pickaxe with a temporary purpose-built climbing-axe model.

## Why

Minecraft's normal item renderer rotates around a hidden display pivot intended
for inventories and item frames. That prevented the wall contact from becoming
the true pick-tip pivot, no matter how many offsets were calibrated.

## Changes

- Adds a simple code-built handle and metal head.
- Places the model origin exactly at the pick tip.
- Rotates the handle around the saved wall contact.
- Keeps inventory and first-person models unchanged.
- Keeps anchored models hidden during mantling.

## Test

- Plant left and right axes separately.
- Observe the model from several third-person angles.
- Test all four horizontal wall directions.
- Release and reattach after a short drop.
- Mantle and confirm wall copies disappear immediately.
