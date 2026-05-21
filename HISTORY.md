# History / Changelog / Commitlog

<https://www.conventionalcommits.org/en/v1.0.0/>

## [unreleased]

- feat: add optional Wallet and Shop integration detection for GPS economy features
- feat: add GPS teleport-token currency registration and optional Shop token packages
- feat: add disabled, fixed, and distance travel cost modes with cost display
- feat: add optional private/group marker creation costs and marker limits
- feat: add global plus personal admin override for GPS costs and limits

## [0.4.0] - 2026-05-19 | Marker management and cooldowns

- fix: keep GPS marker edit and delete actions from overlapping grid labels and teleport dialog buttons
- fix: save marker edits opened from the teleport overlay reliably
- feat: allow permitted players to edit GPS marker names and icons from grid and radial overlays
- feat: add optional marker delete confirmation with a per-player setting
- feat: add per-marker-type teleport cooldowns with grid and radial menu status
- feat: show GPS cooldown and runtime values in the player plugin data tab
- fix: apply static marker cooldowns to static spawn teleports

## [0.3.2] - 2026-05-19 | GPS UI/UX update

- style: align GPS grid cards and marker dialogs with the shared OZ plugin UI style
- style: refine GPS overlay border weight, teleport dialog buttons, and marker icon picker width
- fix: restore colored one-line plugin welcome message
- build: update OZTools dependency to 0.18.0
- build: align bundled PluginAPI jar and Maven dependency version
- docs: standardize agent prompts, PR checklist, and runtime smoke-test guidance
- build: add API verification helper and stricter CI/release validation flow
- build: package only `README.md` and `HISTORY.md` into release artifacts
- fix: invalid import path for PluginSettings

## [0.3.0] - 2026-01-05 | Player plugin settings implemented

- feat: player plugin settings implemented (gui to change stuff like sortorder)

## [0.2.0] - 2025-12-28 | Change sort order

- feat: marker sort order can be cahnged with  `/gps sortasc` or `/gps sortdesc` [0.2.0]
- fix: delete button was shown in radial view for global markers (for non admins) [0.1.2]

## [0.1.0] - 2025-12-28 | Initial release
