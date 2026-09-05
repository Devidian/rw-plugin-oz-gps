# Server warp markers

## Objective

Add an administrator-managed `Warp Marker` GridView tab after global markers. Players can select a server pin to transfer to its configured address and optional password.

## Ownership and dependencies

- Owner: `rw-plugin-oz-gps` (GPS marker UI, persistence, permissions, and transfer workflow).
- Dependency: existing Rising World `Player.connectToOtherServer(String, String, Callback<Boolean>)` API, verified against the bundled PluginAPI.
- No new plugin or external dependency.

## Design

- Store server pins in a dedicated SQLite table; they have no local coordinates and are not normal `Marker` rows.
- Keep the tab independent of teleport costs, marker limits, cooldowns, and Area restrictions.
- Administrators create, edit, and remove pins; all players who can open the GridView can select them.
- Provide ten dedicated planet/moon marker assets per modern and classic icon theme, a server-pin icon, and a world-transfer tab icon.

## Risks and rollback

- A malformed address or an unavailable target server can make a transfer fail; validate the required address before persistence and surface the callback result through i18n.
- The migration is additive. Rollback removes the UI workflow while leaving the isolated table harmless.

## Validation

- [x] Add migration and CRUD coverage for server pins.
- [x] Add the GridView tab, card actions, and admin management overlay.
- [x] Add DE/EN strings for all labels and transfer feedback.
- [x] Verify theme assets and all new API use.
- [x] Run tests, package, API and entry-point checks, then Dev-upload GPS only.
- [x] Verify reload/startup logs; keep manual transfer acceptance separate.

## In-game acceptance

- [x] Confirm `Warp Marker` follows `Globale Marker` in GridView with both icon themes.
- [x] As an administrator, create, edit, and delete a server pin.
- [x] Select a pin with a valid target and verify the connection hand-off.
- [x] Verify malformed targets are rejected and an unavailable target returns the localized failure message.

Production acceptance across multiple servers was confirmed on 2026-09-04.

## Affected repositories

- `rw-plugin-oz-gps` only.
