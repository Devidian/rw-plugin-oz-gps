# OmegaZirkel GPS Plugin for Rising World

Main Goal for this plugin is to replace ingame teleport system with a cool gps system, were you have to visit a location atleast once before you can teleport to it.

## Features included

- Players can teleport to their spawn (fixed location)
- Players can teleport to their last death (fixed location)
- Players can teleport back to the last position where they teleported from (fixed location)
- Players can set a name for a gps marker
- Players can choose an icon for a gps marker
- Players can remove gps marker
- Players can create custom teleport targets
  - gps can be private, only for the user himself
  - gps can be restricted to group, player in the same group will see it.
  - gps can be public, everyone will have it (admin only)
- settings.properties (for admins)
  - Admins can allow or disallow:
    - home teleport
    - death teleport
    - last position teleport
    - creating/using private custom teleports
    - creating/using group teleports

## Features planned

- settings.properties (for admins)
  - Admins can set:
    - maximum for private custom teleports
    - maximum for group teleports
  - Admins can set a cooldown for teleporting
    - for private,group and global
  - Admins can set a cost for teleporting [to use with a currency plugin]
    - for private,group and global

## Attribution

Uicons by [Flaticon]("https://www.flaticon.com/uicons")

Other icons (`icon-ki-*`) made by copilot
Other icons (`icon-gpt-*`) made by chat-gpt

## Contributor Workflow

- Review `AGENTS.md`, `PLANS.md`, and the role prompts in `agent-prompts/` before making structural changes.
- Verify Rising World API usage with `scripts/verify-plugin-api.sh` when adding or changing API calls.
- Run `mvn -B -DskipTests package` and `mvn -B test` before release-facing changes are merged.
- Use `RUNTIME_TESTING.md` and `scripts/docker-runtime-smoke.sh <PluginFolderName>` for runtime smoke tests when behavior changes need server validation.
- Keep `README.md` and `HISTORY.md` current and use Conventional Commit titles for commits and PRs.
