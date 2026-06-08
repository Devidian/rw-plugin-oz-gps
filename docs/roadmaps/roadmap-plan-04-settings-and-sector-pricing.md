# Roadmap Plan 04 Settings And Sector Pricing

## Objective
Expose GPS cost mode settings correctly and replace excessive distance pricing with sector-distance pricing.

## Ownership
Primary repository: `rw-plugin-oz-gps`

Supporting repositories:
- `rw-plugin-oz-tools` for settings UI, player shortcut visibility, i18n, and shared overlay behavior.
- `rw-plugin-oz-wallet` for optional travel cost payments.

## Dependencies
- Hard runtime dependency: `rw-plugin-oz-tools`.
- Optional economy dependency: `rw-plugin-oz-wallet`.
- Cost mode editing depends on dropdown/enum support in plugin settings.

## Phases
- [x] Phase 1: Verify current cost mode setting metadata and add the dropdown/enum UI path needed to edit it safely.
- [x] Phase 2: Replace distance-cost calculation with sector-distance pricing that matches the roadmap examples: `base + (abs(sectorDistanceX) + abs(sectorDistanceZ)) * base`.
- [x] Phase 3: Validate example outputs from the roadmap for base values 1 and 2.
- [x] Phase 4: Update admin/help/status text that describes distance pricing.
- [x] Phase 5: Add Plan 04 player shortcut visibility setting, document the Escape-close API limitation, verify i18n loading, and migration away from deprecated Tools `SQLite` usage if present.
- [x] Phase 6: Update README/HISTORY and validate.

## Progress Notes
- Tools admin settings now support `AdminSettingsType.SELECT`, rendered through the shared dropdown control.
- GPS `travelCostMode` is editable as a select value with `disabled`, `fixed`, and `distance`.
- GPS distance pricing now uses sector distance and ignores vertical/chunk-level distance: `base + (abs(dx) + abs(dz)) * base`.
- Roadmap examples match the implementation: base 1 same sector = 1, sector `1,1` = 3, sector `2,2` = 5; base 2 same sector = 2, sector `1,1` = 6, sector `2,2` = 10, sector `0,10` = 22.
- GPS marker persistence now opens world-scoped SQLite through `SQLiteConnectionFactory`; direct deprecated Tools `SQLite` usage was removed from GPS.
- GPS now registers player-aware shortcut visibility and adds a GPS player setting to hide the `/ozt` and inventory shortcut entry.
- Custom-overlay Escape behavior is deferred to the future Rising World API layer.
- README/HISTORY and i18n/settings descriptions now describe sector-distance pricing.

## Risks
- The final formula line in the source roadmap conflicts with its examples. Implementation should follow the examples unless product review explicitly confirms a different formula.
- Existing setting keys should remain compatible where possible to avoid admin config churn.

## Validation Strategy
- Run `mvn -B test` and `mvn -B -DskipTests package`.
- Add or run targeted checks for same-sector, diagonal, negative-sector, and long-axis pricing examples.
- Runtime-smoke dropdown editing, Wallet charge amount, shortcut visibility, and explicit close controls.

## Affected Repositories/Plugins
- `rw-plugin-oz-gps`
- `rw-plugin-oz-tools`
- `rw-plugin-oz-wallet`

## Rollback Considerations
Keep existing setting keys where practical. If sector pricing misbehaves in runtime smoke, disable only the distance-cost mode rather than unrelated GPS marker behavior.
