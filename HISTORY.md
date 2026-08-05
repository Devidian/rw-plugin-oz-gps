# History / Changelog / Commitlog

<https://www.conventionalcommits.org/en/v1.0.0/>

## [0.7.9] - 2026-08-05 | CI maintenance

- build: maintain the GitHub Actions release workflow.

## [0.7.8] - 2026-07-24 | Shared runtime bridges

- fix: send GPS Discord events in the configured Discord bot language
- refactor: use the synchronized optional Discord bridge
- refactor: keep the plugin entry point limited to lifecycle wiring and event delegation
- change: update the shared OZ Tools dependency to version 0.23.8

## [0.7.7] - 2026-07-24 | Marker action icons

- fix: render dedicated edit and delete icons on marker action buttons
- change: update the shared OZ Tools dependency to version 0.23.7

## [0.7.5] - 2026-07-21 | Marker dialog stability

- fix: keep Save and Abort separated in marker create and edit dialogs
- fix: remove the Discord Connect build dependency while preserving optional bridge behavior
- change: update the shared OZ Tools dependency to version 0.23.1

## [0.7.4] - 2026-07-20 | Advanced button controls

- change: use the stable shared OZ button controls in GPS overlays

## [0.7.3] - 2026-07-20 | Discord channel settings

- change: Discord GPS events use their configured channel ID directly

## [0.7.2] - 2026-07-20 | Release metadata

- change: publish the canonical GitHub release source used by OZ Tools to show
  release notes and safely install GPS updates

## [0.7.1] - 2026-07-17 | Mail bridge and localized shop offers

- feat: use compatible Wallet and Shop bridge contracts for localized mail-related offers

## [0.7.0] - 2026-07-15 | GPS sector and zone access rules

- feat: add configurable sector restrictions for private, group, and global marker teleports
- feat: add GPS-area restrictions for marker use and creation, including per-area administrator marking
- feat: show unavailable marker cards as disabled red cards with localized explanations
- feat: allow static markers to explicitly ignore or honor GPS-area restrictions

## [0.6.2] - 2026-07-14 | Icon set and export polish

- change: use the shared Tools exit icon and remove the obsolete GPS exit asset
- change: rename GPS marker and menu icon keys to their final semantic names
- feat: add route-ready GPS global marker export DTOs/services with `lastChange` filtering
- feat: add future native route exposure flag for global marker export

## [0.6.0] - 2026-06-08 | Access rules and sector pricing

- feat: require configurable total playtime for non-static GPS features while keeping static markers available
- feat: make `travelCostMode` editable through the shared admin settings select control
- change: calculate distance travel costs from sector distance with `base + sectorDistance * base`
- refactor: migrate GPS marker persistence from deprecated Tools `SQLite` to `SQLiteConnectionFactory`
- feat: add GPS shortcut visibility setting for `/ozt` and inventory shortcuts
- change: remove obsolete shared escape-close registrations pending future API support

## [0.5.0] - 2026-05-26 | Economy integrations and shared menu polish

- feat: calculate GPS distance travel costs from Manhattan chunk distance instead of block distance
- feat: render GPS grid markers on one page without pager cards
- feat: place marker creation first in the GPS grid regardless of marker sort order
- feat: add GPS radial Info/Status menu action with the shared Tools info icon
- feat: add shared Tools Info/Status panel content for GPS and route `/gps status` to it
- feat: complete grouped admin settings metadata and i18n labels for GPS settings
- feat: add player preference for the shared GPS entry and default it to the grid view
- refactor: remove the redundant grid-view entry from the GPS radial menu
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
