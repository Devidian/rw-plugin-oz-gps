# PLANS.md

- [ ] Complete durable recovery for paid marker creation across Wallet and GPS database failures, and validate the flow on Development.

Planning is stored in repository-local docs.

- Active implementation tasks: [docs/active/](docs/active/)
- Roadmaps and larger plans: [docs/roadmaps/](docs/roadmaps/)
- Completed phase summaries: [docs/phase-archive.md](docs/phase-archive.md)
- Planning and documentation standards: [docs/policies/repository-policy.md](docs/policies/repository-policy.md)

Every implementation plan must include objective, ownership, dependencies, risks, validation strategy, affected repositories/plugins, rollback considerations, and a markdown checkbox checklist.

## Player erasure

- [x] Provide a transactional, repeatable local erasure API for GPS-owned player data.
- [ ] Connect the API to the shared erasure coordinator and validate it in the game runtime.

Shared markers, GPS areas, and server pins retain their behavior with creator ID `0`; private markers are removed. A database backup is required before the coordinated erasure because it cannot be undone by the plugin. The local SQLite test covers retained objects and repeatability; runtime acceptance is still pending.

Discord event thread routing is implemented locally; Development Discord validation and the separately authorized release remain open. Configuration is documented in README.md.

## Backlog validation (2026-10-15)

- [x] Implement: Twelve new location marker motifs are available in modern and classic themes for private, group, and global markers.
- [ ] Verify behavior on Development after upload, including persistence and permissions where relevant.
