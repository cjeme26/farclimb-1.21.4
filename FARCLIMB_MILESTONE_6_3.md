# FarClimb Milestone 6.3

Milestone 6.3 adds the first third-person climbing pose while keeping the vanilla iron-pickaxe appearance.

## What changed

- The local player's third-person arms now aim toward each axe's saved wall contact point.
- A planted axe follows the same short strike-to-plant blend used by the first-person animation.
- With both axes planted, both arms reach toward the wall and alternate slightly during climbing strides.
- With only one axe planted, the supporting arm stays raised while the free arm and pickaxe lower beside the body.
- The free arm lags behind the one-axe pendulum slightly, giving the loose pickaxe a sense of weight.
- The torso leans toward the supporting side during a one-axe hang.
- The legs use an uneven bent pose instead of the normal mid-air standing pose.
- Sleeves, pants layers, and the jacket copy the new body-part transforms.
- First-person axe planting, climbing movement, camera sway, collisions, and mantling are unchanged.

## Suggested testing

1. Switch to third person with F5 and plant both axes.
2. Confirm both arms and iron pickaxes appear raised toward the wall.
3. Climb upward and watch the arms alternate subtly with each stride.
4. Release the visible left axe and confirm that arm and pickaxe lower beside the body.
5. Reattach it, release the visible right axe, and compare the mirrored pose.
6. Watch several one-axe pendulum cycles and confirm the loose arm trails the body slightly.
7. Reattach the second axe and confirm both arms return to the stable pose.
8. Mantle over a ledge and confirm the model returns to its normal pose afterward.

## Scope

This is the first third-person pose pass. It uses the vanilla player model and vanilla iron-pickaxe item model. Exact hand-to-wall alignment and custom climbing-axe art can be refined later.
