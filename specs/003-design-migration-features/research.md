# Phase 0 Research: Design Migration + New Features

**Feature**: `003-design-migration-features` | **Date**: 2026-05-29

All decisions below resolve the unknowns in the plan's Technical Context. No `NEEDS CLARIFICATION`
remains (spec Q1/Q2 already resolved: pause = freeze & exclude; language = en+es, English default).

---

## R1 — Mapping design tokens to a Material 3 theme

**Decision**: Translate `colors_and_type.css` into a fixed MD3 `ColorScheme` plus an **extended
semantic palette** exposed via a `LocalDayCounterColors` CompositionLocal. MD3 roles get the brand
mapping; tokens with no MD3 equivalent (sage/success, terracotta/milestone, warning, danger-soft,
sunken surface, brand-soft) live in the extended palette.

- MD3 mapping (light): `primary=#0F5F6E`, `onPrimary=#FFFFFF`, `primaryContainer=#D8EAEC`,
  `onPrimaryContainer=#08404B`, `background/surface=#FBF6EE`, `onBackground/onSurface=#1B2A33`,
  `surfaceContainer/Lowest=#FFFFFF` (cards), `surfaceVariant=#F4EDDF` (sunken), `outline=#D6CCB8`,
  `outlineVariant=#E8E0D2`, `error=#C97062`.
- Extended (`DayCounterColors`): `sage=#6FA88B`, `sageSoft=#E1EDE3`, `milestone=#D9876A`,
  `milestoneSoft=#FBE7DD`, `warning=#D9A05B`, `warningSoft=#FAEED7`, `dangerSoft=#F6DDD8`,
  `brandSoft=#D8EAEC`, `brandSofter=#ECF4F5`, `sunken=#F4EDDF`, ring-gradient `#0F5F6E→#2A8597`.
- Shapes: `Shapes(extraSmall=8.dp, small=12.dp, medium=16.dp, large=24.dp, extraLarge=32.dp)`; pill
  buttons use `RoundedCornerShape(percent=50)`. Cards = `large` (24), inputs/chips = `medium` (16),
  sheets = `extraLarge` top corners (32).
- Elevation: teal-tinted shadows are not native MD3 tonal elevation; implement as a reusable
  `Modifier.cardShadow()` using `shadow(elevation, shape, ambientColor/spotColor = teal)`.

**Rationale**: Keeps "all colors sourced from the theme" (Principle I) while expressing brand
semantics MD3 can't. CompositionLocal is the idiomatic Compose pattern for extended palettes.

**Alternatives rejected**: Forcing terracotta/sage into unused MD3 roles (tertiary/secondary) — loses
semantic clarity and collides with component defaults. A third-party design-token lib — unjustified
dependency (Principle V).

---

## R2 — Dark theme derivation

**Decision**: The handoff ships only light tokens. Derive a dark variant: `background/surface ≈
#14181A` (near-black warm), cards `#1B2226`, `onSurface ≈ #E7EEF0`, brand text/accents shift to the
lighter teal `#2A8597`/`#6FF7F6` for contrast, semantics keep hue but raise lightness
(sage/milestone/warning lighten ~12–18%). Verify every pair meets WCAG AA (≥ 4.5:1 text).

**Rationale**: Constitution Principle I requires dark mode from first release. Deriving a tonal dark
set preserves brand while guaranteeing contrast.

**Alternatives rejected**: Auto-darkening the cream palette via `dynamicDarkColorScheme` — that is
Material You (see R3) and off-brand.

---

## R3 — Dynamic color (Material You) deviation

**Decision**: **Disable** Material You; default to the fixed brand scheme. Surface this as a
Constitution Principle I deviation (plan → Complexity Tracking) and **recommend amending the
constitution** via `/speckit-constitution` to: *"dynamic color MUST be supported where it does not
conflict with a defined brand identity."*

**Rationale**: The handoff explicitly states Material You's mint surface "fights the cream/teal
palette." Brand cohesion is the migration's purpose. Honest, documented deviation > silent breach.

**Alternatives rejected**: (a) Keep dynamic color as default — breaks the brand. (b) Optional toggle —
off-brand risk + double QA for a single-user app.

---

## R4 — In-app language switching (en/es), English default

**Decision**: Persist the selected `AppLanguage` in DataStore. Apply via a `LocaleManager` that wraps
the base `Context` with an updated `Configuration` locale in `MainActivity.attachBaseContext()`, read
synchronously at startup (same one-shot `runBlocking` pattern already used for the onboarding flag).
Changing language updates DataStore then calls `Activity.recreate()`. Ship `res/xml/locales_config.xml`
listing `en, es` and reference it from the manifest (`android:localeConfig`) so the OS per-app language
list is also correct. Base resources stay English; `values-es` is the Spanish variant.

**Rationale**: Works on all API levels (26+) without converting the Compose `ComponentActivity` to
`AppCompatActivity`. English default/fallback satisfies the constitution (no amendment). `recreate()`
gives an immediate, complete switch (SC-004).

**Alternatives rejected**: `AppCompatDelegate.setApplicationLocales` — cleanest on API 33+ but the
pre-33 backport path expects `AppCompatActivity`; converting the Nav3 Compose host is unjustified
churn. Per-string runtime lookup — fragile, violates the externalized-strings model.

---

## R5 — Pause/Resume data model & effective-day math

**Decision**: `Counter` gains `status: CounterStatus {ACTIVE, PAUSED}` and `pausedSince: LocalDate?`.
Completed pauses are rows in a new `PausePeriod(counterId, startDate, endDate)` table (FK CASCADE).
Effective streak:

```
anchor = if (status == PAUSED) pausedSince else today
effectiveDays = max(0, DAYS.between(startDate, anchor) − sumCompletedPausedDays(counter))
```

where `sumCompletedPausedDays = Σ DAYS.between(p.startDate, p.endDate)` over the counter's
`PausePeriod` rows. Pause sets `status=PAUSED, pausedSince=today`. Resume inserts
`PausePeriod(pausedSince, today)`, sets `status=ACTIVE`, clears `pausedSince` — in **one Room
`@Transaction`**. Matches the handoff `components.jsx` helpers (`counterDays`, `totalPausedDays`,
`pauseCount`).

> Note: keep the existing **0-based** convention (same day = 0). The handoff JS uses `max(1,…)`; we
> intentionally stay 0-based to avoid regressing `001`/`002` behavior (documented).

**Rationale**: A per-interval table makes `pausa totales` (count) and `días pausados` (sum) exact,
supports future pause history, and keeps `:domain` math pure/testable.

**Alternatives rejected**: Aggregate-only columns (`totalPausedDays`, `pauseCount`) on `Counter` —
smaller but loses interval provenance and complicates correctness when currently paused; deviates from
the handoff shape.

---

## R6 — Daily reminder scheduling (WorkManager)

**Decision**: `DailyReminderScheduler` enqueues a unique `OneTimeWorkRequest` (`DailyReminderWorker`)
with an `initialDelay` computed to the next occurrence of the chosen `HH:mm`; the worker posts the
reminder notification and **re-enqueues itself** for +24h. Toggling off / changing time cancels and
re-enqueues by unique name. Re-arm on boot and on app start (idempotent unique work). Honor the
POST_NOTIFICATIONS permission and the daily-reminder toggle.

**Rationale**: Constitution mandates WorkManager (no `AlarmManager`). Self-rescheduling one-time work
gives accurate time-of-day delivery (SC-009) and survives reboot via unique `KEEP` work re-enqueued at
startup. The existing `DailyUpdateWorker`/`DailyRefresher` establishes the pattern.

**Alternatives rejected**: `PeriodicWorkRequest` (15-min min interval, drifts off the target minute);
`AlarmManager`/`setExactAndAllowWhileIdle` (prohibited by Principle IV).

---

## R7 — Approaching-milestone notification (FR-025b, SHOULD)

**Decision**: Fold into the existing daily refresh (`DailyUpdateWorker`): on each daily run, for each
**active** counter compute days-to-next-milestone; if it equals a small threshold (e.g., 1–3 days),
post an approaching-milestone notification (gated by the milestone-notifications toggle; deduped per
counter+milestone like existing milestone records). Paused counters excluded.

**Rationale**: Reuses the daily cadence and dedup infra; no new scheduler. Marked SHOULD so it can be
deferred within US4 without blocking.

**Alternatives rejected**: A separate per-counter scheduled worker — over-engineered for a single user.

---

## R8 — "Borrar todo" with undo

**Decision**: `EraseAllDataUseCase` takes an in-memory **snapshot** (counters, pause periods,
milestone records, past streaks) then deletes all in one transaction; returns the snapshot. The
Settings screen shows a toast/snackbar with "Deshacer" for a bounded window (~5 s); tapping it calls
`restore(snapshot)` (re-insert in a transaction). After the window the snapshot is dropped → permanent.

**Rationale**: A snapshot+restore gives a true undo without a "trash" table or soft-delete columns,
matching the handoff's toast affordance. Transactional erase/restore keeps atomicity.

**Alternatives rejected**: Soft-delete flags everywhere — pervasive query changes for one rarely-used
action. No undo (just confirm) — contradicts the design.

---

## R9 — Fonts (Outfit + Plus Jakarta Sans)

**Decision**: Bundle both as `res/font` `.ttf` resources with font-family XML, wired in `Type.kt`
(Outfit → display + hero numeral with `tabular-nums`; Plus Jakarta Sans → body/label). Subset to
Latin (+ Spanish diacritics ¿¡ñáéíóú) to control AAB size.

**Rationale**: Offline-safe, deterministic first paint, no runtime provider. Subsetting protects the
10 MB AAB budget (Principle V / Technical Standards).

**Alternatives rejected**: Downloadable Fonts (network/provider dependency, FOUT risk offline);
system fonts (cannot match the brand).

---

## R10 — Category as a fixed chip set

**Decision**: Move Create/Edit `category` from free-text to a fixed chip set. Define
`Counter.CATEGORIES` in `:domain`; map the handoff set to **localized** labels (handoff Spanish:
Salud, Ejercicio, Ahorro, Estudio, Mente). Persist a stable key, display the localized label.
Pre-existing free-text categories display as-is (graceful degradation) but are not offered as new
choices.

**Rationale**: Matches the handoff Create sheet, bounds input (Principle VI), and localizes cleanly.
Final category list confirmable during `/speckit-tasks` if the user wants different buckets.

**Alternatives rejected**: Keep free-text — diverges from the design and complicates localization.

---

## R11 — Widget re-skin (Glance)

**Decision**: Re-skin the existing Glance widget to brand tokens and add the handoff layouts where
Glance allows (featured banner, single-counter with a 7-bar mini-week, multi-counter list); show a
paused indicator and the frozen value for paused counters; keep the daily update cadence.

**Rationale**: Glance 1.1.1 already in the catalog; reuse the existing widget pipeline
(`DayCounterWidget`, `WidgetStateUpdater`). Glance constrains some visuals (custom Canvas limited) — the
mini-bars use simple sized boxes.

**Alternatives rejected**: RemoteViews rewrite — abandons the existing Glance investment.

---

## Summary of decisions

| # | Topic | Decision |
|---|-------|----------|
| R1 | Tokens → theme | MD3 scheme + `LocalDayCounterColors` extended palette + `Shapes` + tinted shadow modifier |
| R2 | Dark theme | Derive warm near-black dark set; verify AA contrast |
| R3 | Dynamic color | Disable Material You (brand); document deviation + recommend constitution amendment |
| R4 | Language | DataStore + `LocaleManager` context wrap + `recreate()`; en/es; English default; locales_config |
| R5 | Pause math | `status`+`pausedSince` on Counter + `PausePeriod` table; effective = elapsed−paused; 0-based |
| R6 | Reminder | WorkManager self-rescheduling one-time worker at chosen HH:mm; re-arm on boot/start |
| R7 | Approaching milestone | Fold into daily refresh; threshold + dedup; active-only (SHOULD) |
| R8 | Erase + undo | Snapshot → transactional delete → bounded restore window |
| R9 | Fonts | Bundle Outfit + Plus Jakarta Sans (Latin subset) as resources |
| R10 | Category | Fixed localized chip set; stable keys; old free-text degrades gracefully |
| R11 | Widget | Re-skin Glance; add layouts + paused indicator |
