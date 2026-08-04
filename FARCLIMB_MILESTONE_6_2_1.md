# FarClimb Milestone 6.2.1

Milestone 6.2.1 refines the first-person planted-axe animation introduced in 6.2.

## What changed

- The planted pickaxes are raised higher and rotated less steeply so more of the iron head and handle remain visible.
- Left click and right click now use the same FarClimb-controlled strike system for their matching visible axe.
- Minecraft's normal attack/use motion is suppressed while a climbing axe owns that mouse button.
- Each successful plant is one continuous animation: normal held pose, forward strike, wall impact, small recoil, planted pose.
- The metallic impact sound now occurs at the visual contact moment instead of immediately when the click begins.
- Main-hand and offhand animations remain independent and still respect Minecraft's Main Hand setting.
- Existing climbing, one-axe pendulum movement, camera sway, collision checks, and mantling are unchanged.

## Suggested testing

1. With the normal right-handed setting, left-click the wall and confirm the visible left axe performs the full strike and remains planted.
2. Right-click and confirm the visible right axe performs the same animation.
3. Release both axes independently and attach them again several times.
4. Watch whether the impact sound lines up with the moment the pickaxe reaches the wall.
5. Confirm the final planted pose shows noticeably more of the pickaxe than Milestone 6.2.
6. Climb with both axes and confirm the existing alternating stride motion still works.
7. Test with Minecraft's Main Hand setting changed to Left and confirm the physical mouse sides remain correct.

## Scope

This is a first-person presentation correction. Third-person arm and axe posing remains planned for a later milestone.
