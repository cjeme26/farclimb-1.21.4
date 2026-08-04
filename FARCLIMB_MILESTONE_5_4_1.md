# FarClimb Milestone 5.4.1

This is a camera-sway tuning revision built on Milestone 5.4.

Changes:

- Stationary wall hanging now sways more broadly and much more slowly.
- Stationary sway increases from about 0.65 degrees to about 1.55 degrees.
- Active climbing no longer uses the same continuous sine wave as hanging.
- Every stride now creates one smooth camera-roll pulse and consecutive strides alternate left/right.
- Upward pulls use the strongest roll (about 3.0 degrees), traverses use about 2.55 degrees, and downward steps use about 2.15 degrees.
- The camera still returns smoothly to neutral while detaching or mantling.
- Movement, collision, wall attachment, and mantle behavior are unchanged.
