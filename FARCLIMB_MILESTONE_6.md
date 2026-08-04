# FarClimb Milestone 6.0

Milestone 6.0 separates the two climbing axes into independent attachment states.

## Controls

- Left click toggles the main-hand Climbing Axe.
- Right click toggles the offhand Climbing Axe.
- Both axes attached: W/A/S/D climbing and mantling are enabled.
- One axe attached: the player hangs in place, but cannot climb yet.
- Neither axe attached: gravity returns and the player falls.
- Sneak is no longer required to attach or remain on the wall.

Each axe remembers its own wall contact. Removing an axe from its hand or losing
its supporting block releases only that axe. The remaining axe continues to hold
the player.

## Intentionally not included yet

- One-axe pendulum movement
- Strong one-sided camera lean
- Grip/stamina loss
- Sliding and failed fall catches
- Custom hand or axe animations
