# Roadmap Plan 02 UI Entry Preference

## Objective
Make the GPS `/ozt` entry open the player's preferred GPS UI, defaulting to the grid view, and remove the redundant grid-view entry from the GPS radial menu.

## Ownership
Primary repository: `rw-plugin-oz-gps`.

Supporting repositories:
- `rw-plugin-oz-tools` provides player settings UI, shared menu ordering, and optional info/status panel support.

## Dependencies
- Tools shared settings/menu updates should be available before implementation.
- GPS remains a Tools-dependent feature plugin.

## Work Packages
- [x] Package 1: Add a per-player GPS setting for default entry behavior: grid view or GPS radial menu.
- [x] Package 2: Change the GPS button in `/ozt` to open the selected player preference, with grid view as default.
- [x] Package 3: Remove the grid-view menu point from the GPS plugin radial menu once `/ozt` covers that entry path.
- [x] Package 4: Add GPS info/status panel content and wire existing info/status commands to the shared Tools panel.
- [x] Package 5: Complete GPS logger cleanup, settings metadata coverage, grouped settings labels, and i18n labels as part of the all-plugin rollout.

## Progress Notes
- Packages 1-3 are complete: GPS now persists a per-player default entry mode, `/ozt` opens the selected GPS entry with grid view as the default, and the GPS radial menu no longer contains the redundant grid-view item.
- Package 5 is complete for Root Steps 8-9: GPS admin settings now cover every safe default key, group related settings, use integer input types where supported, and provide English/German setting labels.
- Package 4 is complete for Root Step 10: GPS now registers a shared Tools Info/Status provider and routes `/gps status` to the shared panel.

## Risks
- Existing players may expect the old GPS radial flow; the default grid view should be documented.
- The GPS radial menu should not lose any command path except the now-redundant grid-view entry.

## Validation Strategy
- Verify `/ozt` opens grid view by default.
- Verify player setting switches `/ozt` behavior to the GPS radial menu.
- Verify direct `/gps` workflows still work after removing the redundant grid entry.
- Run Maven package and tests.

## Affected Repositories/Plugins
- `rw-plugin-oz-gps`
- `rw-plugin-oz-tools`

## Rollback Considerations
Keep the original GPS radial entry behavior recoverable by reverting the player preference wiring.
