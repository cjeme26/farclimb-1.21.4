# FarClimb Milestone 4.0

Milestone 4 adds the first climbing attachment behavior.

## Added

- Hold the normal Sneak key (Left Shift by default) while both Climbing Axes are equipped and a climbable wall is in reach.
- The player attaches at their current position.
- Velocity and gravity are suppressed while attached.
- A metallic impact sound plays once when attachment succeeds.
- Looking away from the wall does not detach the player.
- Releasing Sneak detaches the player.
- Removing either axe detaches the player.
- Breaking or replacing the supporting wall block detaches the player.
- Fall distance is cleared while attached and when detaching.

## Deliberately not included yet

- W/A/S/D climbing movement
- Individual axe placement
- One-axe hanging
- Grip or stamina
- Custom sounds or animations

## Test

1. Equip a Climbing Axe in each hand.
2. Face a full solid wall from close range.
3. Hold Sneak.
4. Confirm the metallic attachment sound plays and the player stays fixed.
5. Look away while continuing to hold Sneak; attachment should remain.
6. Release Sneak; the player should detach.
7. Attach again and remove either axe; the player should detach.
8. Attach again and break the supporting block in Creative mode; the player should detach.
