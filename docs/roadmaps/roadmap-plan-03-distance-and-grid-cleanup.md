# Roadmap Plan 03 Distance And Grid Cleanup

## Objective
Adjust GPS distance pricing to use chunk distance instead of block distance and simplify the marker grid overlay so all entries are shown on one page with marker creation always first.

## Ownership
Primary repository: `rw-plugin-oz-gps`

Supporting repository:
- `rw-plugin-oz-tools` for shared UI contracts already used by GPS.

## Dependencies
- Hard runtime dependency: `rw-plugin-oz-tools`.
- Existing optional economy behavior must continue to use Wallet only through the established integration path.

## Phases
- [x] Phase 1: Change distance-price calculation from block distance to chunk distance while preserving settings semantics as much as possible.
- [x] Phase 2: Review price-related labels/help text so admins understand chunk-based pricing.
- [x] Phase 3: Remove pager mechanics from the grid overlay and render all marker entries on one page.
- [x] Phase 4: Ensure `Marker erstellen` is always the first grid action independent of ascending/descending marker sort.
- [x] Phase 5: Add a radial-menu Info/Status button in the GPS main menu.
- [x] Phase 6: Update README/HISTORY and validate.

## Risks
- Changing distance units can significantly reduce prices; admins may need release notes that explain the behavior change.
- Removing pagination can create very large overlays for players with many markers.
- Marker-create ordering must not break existing marker sort preferences.

## Validation Strategy
- Run `mvn -B -DskipTests package`.
- Run `mvn -B test`.
- Runtime-smoke short, medium, and long-distance teleports; many-marker grid rendering; marker-create ordering under both sort directions.

## Affected Repositories/Plugins
- `rw-plugin-oz-gps`
- `rw-plugin-oz-tools`

## Rollback Considerations
Keep pricing calculation isolated. If all-on-one grid rendering is too heavy, pagination can be restored without reverting chunk-based pricing.

## Progress Notes
- Phase 1 complete: `GPSEconomy.distanceCost` now converts player/target positions to API chunk coordinates and charges Manhattan chunk distance times the existing `travelDistanceCostPerBlock` value.
- Phase 2 complete: README, default settings comments, and admin setting labels describe chunk-based pricing while keeping the legacy setting key.
- Phase 3 complete: `GPSGridOverlay` loads all markers for the active type without pager cards.
- Phase 4 complete: private, group, and global marker creation cards are inserted before sorted marker cards.
- Phase 5 complete: the GPS radial menu includes an Info/Status action using the shared Tools `icon-ki-info-status` asset, and `/gps info` routes to the same panel as `/gps status`.
- Phase 6 complete: README/HISTORY were updated and validation passed with `mvn -B test` and `mvn -B -DskipTests package`.
