# OZ GPS 0.4.0

This release adds marker management and per-marker teleport cooldowns.

## Highlights

- Permitted players can edit marker names and icons from the GPS grid and teleport overlay.
- Marker deletion now uses an optional confirmation dialog with a per-player "do not ask again" setting.
- Admins can configure teleport cooldowns per marker type for static, private, group, and global markers.
- The GPS grid and teleport overlay show cooldown status so players can see when a marker can be used again.
- The GPS player data tab now shows cooldown and runtime values for support and administration.
- Static marker cooldowns now apply to static spawn teleports.
- Grid marker actions, teleport dialog layout, fullscreen delete confirmation, and edit-save reopening from the teleport overlay were fixed.

## Installation

Update both plugins:

- `OZTools` `0.18.0`
- `OZGPS` `0.4.0`

No database migration is required.
