# FarClimb

A Fabric 1.21.4 mountaineering mod focused on deliberate two-axe climbing.

## Current milestone: 6.3.1

- Climbing Axe item using the vanilla iron pickaxe appearance
- Main-hand and offhand equipment detection
- Climbable wall detection
- Independent left-side and right-side axe attachment
- Unified first-person strike-to-plant animations tied to saved wall contacts
- Third-person arms aimed toward the saved axe contacts
- Corrected sleeve, jacket, and trouser overlay alignment
- Wall-facing look restrictions while axes are planted
- Lowered loose arm and pickaxe during one-axe hanging
- Third-person torso lean and bent-leg climbing pose
- Alternating visible axe repositioning during climbing strides
- One-axe pendulum hanging with body displacement and camera lean
- Collision-aware sway near corners and obstructions
- Smooth stabilization when the second axe reconnects
- Two axes enable smooth W/A/S/D climbing
- Eased climbing strides with larger upward pulls
- Top-edge mantling
- First-person climbing sway

See `FARCLIMB_MILESTONE_6_3_1.md` for testing details.

### Milestone 6.3.1 compatibility fix

This archive replaces the unsupported `Direction.asRotation()` call with an
explicit direction-to-yaw conversion for Yarn 1.21.4.

## Milestone 6.3.2

Polishes the third-person body model and wall-facing camera limits. Skin overlays now follow complete limb transforms, torso/leg seams are reduced, and mouse input stops cleanly at the allowed climbing look angle without repeated snapping.

## Milestone 6.3.2a

Outer skin layers now preserve their own default offsets while inheriting the final
movement of the matching base limb. This is a rendering-only correction; climbing,
camera limits, attachment, mantling and axe contact behavior are unchanged.


## Milestone 6.3.3

Third-person planted pickaxes are now rendered at their saved wall contacts instead of being carried by the rotating hand model. Visual contacts are kept inside block-face margins to reduce edge and corner floating. See `FARCLIMB_MILESTONE_6_3_3.md`.

## Milestone 6.3.3b

World-anchored pickaxes now use separately calibrated left/right poses and newly planted contacts are constrained to reachable shoulder-relative positions.
