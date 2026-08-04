# FarClimb Milestone 3.0

## Goal
Detect a climbable wall directly in front of the player while both Climbing Axes are equipped.

## Detection rules
- The wall must be within 1.75 blocks of the player's eyes.
- The player must be aiming at a horizontal face, not a floor or ceiling.
- The targeted block face must have a full collision surface.
- Fluids, air, and thin decorative blocks are not accepted.

## Test
1. Run `./gradlew clean runClient`.
2. Give yourself two axes with `/give @s farclimb:climbing_axe 2`.
3. Equip one axe in each hand.
4. Stand close to a full stone wall and look directly at it.
5. Turn away, step back, look at the floor, and try thin blocks such as torches or grass.

Expected action-bar messages:
- `Two climbing axes detected - no climbable wall in reach`
- `Climbable wall detected - ready to attach`

Messages appear only when the detected state changes. This milestone does not alter gravity or movement.
