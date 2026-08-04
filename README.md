# FarClimb

FarClimb is a Fabric mod for Minecraft Java 1.21.4 focused on deliberate two-axe mountaineering.

Current milestone: **5.4.1 — stronger, slower climbing sway**

Implemented:

- Climbing Axe item
- Main-hand and offhand axe detection
- Climbable wall detection
- Sneak-to-attach wall holding
- Metallic attachment sound
- W/A/S/D climbing along continuous walls
- Smooth eased strides with larger upward pulls, smaller traverses, and smallest downward movements
- Smooth top-edge mantling with clearance and collision checks
- Slow first-person hanging sway plus alternating camera-roll pulses for individual climbing strides

## Milestone 5.4.1

- Broadens stationary sway to about 1.55 degrees and slows its full rhythm to roughly four seconds.
- Gives every active climbing stride its own smooth roll pulse.
- Alternates the roll direction between consecutive pulls to suggest left-hand/right-hand movement.
- Uses about 3.0 degrees for upward pulls, 2.55 for traverses, and 2.15 for downward steps.
- Interpolates every rendered frame and settles smoothly to neutral when detached or mantling.
- Does not affect third-person view or depend on Minecraft's View Bobbing setting.
