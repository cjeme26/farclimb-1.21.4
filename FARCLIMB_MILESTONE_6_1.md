# FarClimb Milestone 6.1

Milestone 6.1 replaces the stationary one-axe hold with a slow pendulum-like hang.

## One-axe behavior

- The attached axe remains the fixed wall contact.
- The player settles slightly lower beneath that contact.
- The body sways up to about 0.25 blocks sideways over a roughly five-second cycle.
- The body rises a small amount near either end of the swing arc.
- Main-hand-only and offhand-only hangs use opposite resting biases.
- The first-person camera follows only part of the body displacement and adds a smaller roll, helping the lower body feel looser than the upper attachment point.
- Nearby collision reduces the sway distance instead of allowing the player to clip into blocks.
- W/A/S/D climbing still requires both axes.

## Reattaching the second axe

When the second axe is planted, the player eases back to the stable two-axe position over about half a second. Normal climbing resumes after that short stabilization.

## Suggested testing

1. Attach only the main-hand axe and watch several complete sway cycles.
2. Repeat with only the offhand axe and compare the opposite resting lean.
3. Reattach the second axe at the middle and edge of a swing.
4. Release one axe while actively climbing and confirm the pendulum begins smoothly.
5. Test beside corners and protruding blocks to confirm the sway shortens rather than clipping.
6. Release the final axe and confirm gravity returns immediately.

## Intentionally not included yet

- Grip or stamina loss
- Automatic one-axe release
- Player-controlled pumping of the swing
- Sliding and failed fall catches
- Custom arm or axe anchoring animations
