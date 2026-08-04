# FarClimb Milestone 5.3

Milestone 5.3 adds smooth top-edge mantling to the two-axe climbing system.

## Behaviour

While attached, keep holding Sneak and hold W near the upper part of the final wall block.

When the wall ends and the block has a safe, solid top, FarClimb will:

1. Raise the player smoothly above the lip.
2. Pull the player inward onto the top surface.
3. Restore gravity and detach automatically once safely on top.

The mantle completes automatically once it starts, even if W or Sneak is released. Both Climbing Axes must remain equipped.

## Safety checks

Mantling will not begin when:

- The wall continues upward.
- The current block does not have a full solid top.
- There is insufficient room above or on top of the ledge.
- The movement path or destination is obstructed.

If the supporting ledge disappears during the motion, the mantle is cancelled.

## Still planned

- First-person climbing sway
- Independent axe attachment
- One-axe hanging and grip
- Fall-catching and sliding recovery
