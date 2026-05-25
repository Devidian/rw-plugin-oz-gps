# OmegaZirkel GPS Plugin for Rising World

Main Goal for this plugin is to replace ingame teleport system with a cool gps system, were you have to visit a location atleast once before you can teleport to it.

## Features included

- Players can teleport to their spawn (fixed location)
- Players can teleport to their last death (fixed location)
- Players can teleport back to the last position where they teleported from (fixed location)
- Admins can configure per-type teleport cooldowns for static, private, group, and global markers
- Admins can optionally require Wallet costs for teleports and marker creation
- Admins can optionally register GPS teleport-token currency and Shop token packages
- Admins can enforce private and group marker limits with a two-step admin override
- Players can set a name for a gps marker
- Players can choose an icon for a gps marker
- Players can edit marker names and icons when they are allowed to manage the marker
- Players can remove gps markers with an optional confirmation dialog
- Players can create custom teleport targets
  - gps can be private, only for the user himself
  - gps can be restricted to group, players in the same group will see and manage it
  - gps can be public, everyone will have it and admins can manage it
- Player plugin settings
  - Players can choose whether the shared `/ozt` GPS entry opens the grid view or the GPS radial menu
  - Players can change marker sort order
  - Players can re-enable or disable the marker delete confirmation
- settings.properties (for admins)
  - Admins can allow or disallow:
    - home teleport
    - death teleport
    - last position teleport
    - creating/using private custom teleports
    - creating/using group teleports
  - Admins can set teleport cooldowns in seconds for static, group, private, and global markers.
    - Set a cooldown value to `0` to disable it for that marker type.

## Economy and limits

Economy behavior is disabled by default. GPS remains loadable with only `rw-plugin-oz-tools`.

Optional Wallet-backed settings:

```properties
travelCostMode=disabled
travelCostCurrencyIdentifier=
travelDistanceCostPerBlock=1
useStaticMarkerCost=10
usePrivateMarkerCost=10
useGroupMarkerCost=10
useGlobalMarkerCost=10
enableMarkerCreateCosts=false
createPrivateMarkerCost=25
createGroupMarkerCost=75
markerCreateCostCurrencyIdentifier=
enableTeleportTokens=false
teleportTokenCurrencyIdentifier=GPSTP
teleportTokenCurrencyName=GPS Teleport Token
teleportTokenIcon=icon-ki-gps-coin
enableTeleportTokenShopOffers=false
teleportTokenShopCurrencyIdentifier=
teleportTokenPackage1Price=25
teleportTokenPackage10Price=200
teleportTokenPackage50Price=900
allowAdminOverride=false
```

`travelCostMode` supports `disabled`, `fixed`, and `distance`.
`distance` uses Manhattan distance from the player's current position to the target.
`fixed` uses the per-marker-type `use*MarkerCost` settings.
Marker limits use `maxPrivateMarkers` and `maxGroupMarkers`; `-1` means unlimited.

Admin override is two-step: `allowAdminOverride=true` enables the feature globally, and each admin must also enable the personal GPS admin override in player plugin settings.

## Commands

- `/gps` or `/gps open`: open the GPS radial menu.
- `/gps opengrid`: open the GPS grid view.
- `/gps status`: open the shared Tools Info/Status panel.
- `/gps help`: show command help.

## Attribution

Uicons by [Flaticon]("https://www.flaticon.com/uicons")

Other icons (`icon-ki-*`) made by copilot
Other icons (`icon-gpt-*`) made by chat-gpt

## Contributor Workflow

- Review `AGENTS.md`, `PLANS.md`, `.codex/agents.toml`, and `.codex/skills/` before making structural changes.
- Verify Rising World API usage with `scripts/verify-plugin-api.sh` when adding or changing API calls.
- Run `mvn -B -DskipTests package` and `mvn -B test` before release-facing changes are merged.
- Use `RUNTIME_TESTING.md` and `scripts/docker-runtime-smoke.sh <PluginFolderName>` for runtime smoke tests when behavior changes need server validation.
- Keep `README.md` and `HISTORY.md` current and use Conventional Commit titles for commits and PRs.
