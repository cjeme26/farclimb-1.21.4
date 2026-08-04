# FarClimb Milestone 6.3.3

## Goal

Make planted third-person climbing axes belong to the wall instead of the player's hand.

## Added

- Renders each attached vanilla pickaxe at its saved world-space wall contact.
- Hides the normal hand-bound third-person pickaxe only after the strike reaches the wall.
- Keeps the first-person planting animation unchanged.
- Clamps visual contact points inward from block edges without changing gameplay attachment data.
- Uses the wall face to orient the pickaxe, so body and camera turning no longer drag it away from the block.
- Keeps each arm aiming toward the same clamped contact used by the world renderer.

## Test

1. Attach one axe and orbit the camera in third person.
2. Attach both and turn to every allowed look limit.
3. Hang at the edge of a block and in an inside corner.
4. Climb up, down, and sideways while watching both contacts.
5. Release either axe and confirm it returns to the normal hand immediately.
6. Confirm first-person planting remains unchanged.

The skin-overlay issue from 6.3.2a is intentionally not changed in this milestone.
