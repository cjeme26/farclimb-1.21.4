# FarClimb Milestone 2.0

## Goal
Detect which hands are holding the FarClimb Climbing Axe.

## Test
1. Run `./gradlew clean runClient`.
2. Give yourself two axes with `/give @s farclimb:climbing_axe 2`.
3. Equip and remove the axes in different hands.

The action bar should report:
- Climbing axes not equipped
- Main-hand climbing axe detected
- Offhand climbing axe detected
- Two climbing axes detected - ready to climb

Messages appear only when the equipped state changes.
