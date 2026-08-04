# FarClimb Milestone 5.4

Adds subtle first-person camera sway while climbing with two axes.

## Behaviour

- The camera gently rolls left and right while attached.
- Stationary sway is limited to roughly 0.65 degrees.
- Active climbing strides can reach roughly 1.35 degrees.
- The camera smoothly returns to neutral when detaching or beginning a mantle.
- Third-person cameras are not affected.
- The effect works independently of Minecraft's View Bobbing setting.

This milestone changes presentation only. Climbing movement, collision checks,
attachment rules, and mantle behaviour are unchanged from Milestone 5.3.3.
