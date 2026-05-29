# Quickstart: Design Migration + New Features

**Feature**: `003-design-migration-features` | **Date**: 2026-05-29

How to build, run, and verify this iteration. No toolchain change vs. `002` (compileSdk 36, AGP
8.9.1, Gradle 8.11.1, JDK 17).

---

## Build & test

```powershell
# Unit tests (domain + data)
.\gradlew :domain:test :data:testDebugUnitTest

# Presentation unit/Compose tests
.\gradlew :presentation:testDebugUnitTest

# Domain coverage gate (≥ 80%, must not decrease)
.\gradlew :domain:koverVerify

# Static analysis + lint
.\gradlew detekt :app:lintDebug

# Assemble debug
.\gradlew :app:assembleDebug
```

TDD order: write the failing test first (use case → DAO/repo → Compose screen), then implement.

## Manual verification (maps to Success Criteria)

1. **Design migration (US1, SC-001/006/007)** — open every surface (Contadores, Estadísticas,
   Ajustes, Detail, History, Create/Edit/Reset, Celebration, Onboarding, Empty, Widget) in light and
   dark: cream bg, white 24px cards, teal brand, pill buttons, Outfit numeral. Run a TalkBack pass.
2. **Pause (US2, SC-002/003/008)** — Detail → "Pausar"; confirm dashed/muted ring + "En pausa" banner;
   change device date +2 days → count unchanged; "Reanudar" → continues from frozen day. Verify the
   handoff example (start 1 Jan, pause 10 Jan, resume 20 Jan ⇒ 9 días). Home → filter Pausados.
3. **Language (US3, SC-004/005)** — Ajustes → Idioma → Español; UI switches; cold-restart → still
   Spanish; switch back to English.
4. **Daily reminder (US4, SC-009)** — enable + set a near-future time; confirm a reminder fires;
   disable → none. (Use a 1-minute-ahead time for a quick check.)
5. **Stats (US5, SC-008)** — with mixed active/paused counters, verify Total acumulado (effective),
   Pausas card, weekly bars; Contadores activos excludes paused.
6. **Borrar todo (US6, SC-010)** — Ajustes → Datos → Borrar todo → confirm → toast "Deshacer" →
   tap Deshacer restores all; repeat and let the toast dismiss → permanent.
7. **Appearance (US7)** — toggle dark mode; verify brand dark palette; cold-restart persists.

## Notes
- Fresh install assumed; bumping Room to v3 destroys existing data (no migration).
- Daily-reminder quick test: set the time to `now + 1 min`; the worker posts then re-arms for +24h.
- Fonts: if the AAB exceeds 10 MB, subset Outfit/Plus Jakarta Sans to Latin glyphs.
