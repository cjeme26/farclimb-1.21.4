# FarClimb Milestone 5.2

Milestone 5.2 replaces the abrupt step-and-pause movement from 5.1 with smooth eased climbing strides.

## Behaviour

While attached and holding movement input, FarClimb creates a short target stride and moves the player through it over 7–9 ticks using a smoothstep ease-in/ease-out curve.

Approximate stride sizes:

- Up: 0.30–0.40 blocks
- Sideways: 0.16–0.22 blocks
- Down: 0.10–0.14 blocks

Upward pulls are therefore largest, sideways traverses are smaller, and downward movements are smallest.

Each stride is checked before it begins and throughout its movement. It stops if the destination is obstructed or the supporting wall face is missing.

## Still planned

- Top-edge mantling
- First-person camera sway
- One-axe hanging and grip
