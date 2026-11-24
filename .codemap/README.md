# rw-plugin-oz-gps
<!-- codemap-id: rw-plugin-oz-gps -->

## Purpose

Markers, waypoints, server pins and teleportation. The [plugin descriptor](../src/resources/plugin.yml) names [GPS](../src/de/omegazirkel/risingworld/GPS.java) as its lifecycle entry.

## Modules

- [markers](modules/markers.md) — Marker access, persistence and map export.
- [operations](modules/operations.md) — Teleport, cooldowns, economy and user interface.

## Build and tests

[Maven](../pom.xml) defines the Java build. Follow [repository validation](../AGENTS.md) for packaging, API checks and runtime validation. Relevant automated tests are under [src-test](../src-test/).

## Dependencies

- `rw-plugin-oz-tools/shared-api` — build and runtime shared infrastructure. [Evidence](../pom.xml)
- `rw-plugin-oz-wallet/ledger` — optional payment integration. [Evidence](../src/de/omegazirkel/risingworld/gps/WalletBridge.java)
- `rw-plugin-oz-shop/api` — optional offer integration. [Evidence](../src/de/omegazirkel/risingworld/gps/ShopBridge.java)
- `rw-plugin-oz-discord-connect/discord` — optional notification integration. [Evidence](../src/de/omegazirkel/risingworld/gps/DiscordConnect.java)
