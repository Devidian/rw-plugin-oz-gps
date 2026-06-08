# GPS UI Design Alignment

## Objective
Align the GPS raster view, marker creation overlay, and radial teleport dialog with the established Wallet and LandClaim UI styling while preserving existing UI element dimensions and behavior.

## Ownership
Owning repository/plugin: `rw-plugin-oz-gps`
Supporting repositories/plugins: `rw-plugin-oz-tools`

## Dependencies
- Runtime: `rw-plugin-oz-tools`
- Build: existing Maven/Java 20 setup
- Optional integrations: none

## Risks
- Visual-only changes can still affect usability if labels or buttons become harder to scan; mitigate with compile validation and runtime visual review.
- The raster header must remain unchanged; styling changes are limited to panel, scroll body, grid alignment, and grid cards.

## Validation Strategy
- [x] `mvn -B -DskipTests package`
- [x] `mvn -B test` when tests exist
- [x] Runtime review of GPS raster view, create-marker overlay, and radial teleport dialog

## Affected Repositories/Plugins
- `rw-plugin-oz-gps`

## Rollback Considerations
Revert the styling changes in the GPS UI classes and the matching `HISTORY.md` entry. No config, persistence, or runtime dependency migration is involved.

## Implementation Checklist
- [x] Left-align raster grid cards without changing card dimensions
- [x] Apply Wallet/LandClaim colors and rounded corners to raster cards
- [x] Restyle marker creation panels and icon buttons
- [x] Restyle radial teleport dialog
- [x] Apply UI/UX corrections for thicker grid border, shorter teleport buttons, and wider icon picker
- [x] Update `HISTORY.md`
- [x] Run Maven validation

## Runtime Review
- Screenshots in root `local.res` were reviewed on 2026-06-08. Raster cards, marker creation, icon selection, and the teleport dialog are readable, aligned, and visually consistent.
