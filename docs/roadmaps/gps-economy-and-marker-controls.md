# GPS Economy And Marker Controls Roadmap

## Objective
Add optional GPS economy features and marker limits while keeping GPS loadable without Wallet or Shop.

## Ownership
Primary repository: `rw-plugin-oz-gps`.

Supporting repositories:
- `rw-plugin-oz-tools` for shared UI/settings/admin-tab support.
- `rw-plugin-oz-wallet` for optional currency registration, balances, and withdrawals.
- future `rw-plugin-oz-shop` for optional teleport-token purchase offers.

## Dependencies
- Hard dependency: `rw-plugin-oz-tools`.
- Optional dependency: `rw-plugin-oz-wallet`.
- Optional dependency: future `rw-plugin-oz-shop`.

## Confirmed Decisions
- All prices are whole integers.
- GPS distance pricing uses Manhattan distance.
- Admin settings should use the shared editable/hidden settings metadata once available.
- Teleport-token package prices should be directly configurable rather than calculated from discounts.
- Admin override still shows normal costs and limits; it only bypasses enforcement.
- GPS-specific teleport-token kill rewards stay in GPS for Roadmap Plan 01.

## Work Packages
- [x] Package 1: Add optional integration detection for Wallet and Shop, with all economy features disabled when unavailable.
- [x] Package 2: Add settings and admin data display for enabling/disabling each economy feature independently.
- [x] Package 3: Implement teleport-token currency registration when Wallet exists and `teleportMarken` is enabled.
- [x] Package 4: Register Shop offers for 1, 10, and 50 teleport-token packages with directly configurable package prices when Shop exists and buying tokens is enabled.
- [x] Package 5: Review optional kill rewards for bandits/skeletons; close the GPS-specific implementation as superseded by Rewards-owned generic enemy-NPC rewards.
- [x] Package 6: Implement travel costs with disabled, fixed-price, and Manhattan-distance-based modes.
- [x] Package 7: Show travel costs on GPS marker tiles and withdraw cost before teleport.
- [x] Package 8: Implement marker creation costs for private and group markers.
- [x] Package 9: Implement private and group marker limits with disabled save button and limit text.
- [x] Package 10: Implement global `allowAdminOverride` and per-admin personal override setting.
- [x] Package 11: Update README, HISTORY, i18n, settings defaults, and runtime validation notes.

## Risks
- Reflection-based Wallet/Shop integration must be robust when optional plugins are absent or load after GPS.
- Cost display and withdrawal must match existing cooldown/teleport availability UX.
- Admin override must not accidentally exempt all admins unless they opt in personally.

## Validation Strategy
- Verify GPS works with Tools only.
- Verify Wallet-present and Wallet-absent behavior for token registration, travel costs, and marker creation costs.
- Verify Shop-present and Shop-absent behavior for token offers.
- Verify insufficient funds/tokens blocks teleport or marker creation without side effects.
- Verify marker limits across private and group markers, including deletion then creation.
- Verify admin override combinations: global off, global on/personal off, global on/personal on.

## Affected Repositories/Plugins
- `rw-plugin-oz-gps`
- `rw-plugin-oz-tools`
- `rw-plugin-oz-wallet`
- future `rw-plugin-oz-shop`

## Rollback Considerations
All economy and limit behavior should remain disabled by default. Rollback should preserve existing markers, cooldowns, and settings where possible.

## Open Questions
- None.
