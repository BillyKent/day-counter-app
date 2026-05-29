# Phase 0 — Research & Decisions: Screens and Navigation Overhaul

**Feature**: `002-screens-and-navigation` | **Date**: 2026-05-28

All five clarification questions were resolved in the spec's 2026-05-28 session, so there are
no open `NEEDS CLARIFICATION` items. This document records the **technical decisions** that
the design implies, with rationale and rejected alternatives, so Phase 1 and `/speckit-tasks`
have a single source of truth.

---

## Decision 0 — Migrate to Jetpack Navigation 3 (`NavDisplay`)

**Decision**: Per the user's explicit request, migrate the app from Navigation 2 (Navigation
Compose / `NavHost`) to **Jetpack Navigation 3**. Replace `NavHost`/`NavController` with a
single `NavDisplay` driven by a developer-owned, observable back stack. Routes become
type-safe `@Serializable … : NavKey` keys (`NavKeys.kt`); the `NavGraphBuilder` lambda becomes
an `entryProvider { entry<Key> { … } }`. Follow the official Nav2→Nav3 migration guide as a
single atomic change (no incremental coexistence). KotlinX Serialization (already on the
classpath) backs the `@Serializable` keys.

**Toolchain requirement**: Navigation 3 requires **`compileSdk = 36`**, which the current AGP
8.7.3 cannot target — so AGP is bumped to the **minimal** version supporting compileSdk 36
(AGP 8.9+), keeping Kotlin 2.0.21 / KSP / Hilt 2.52 as-is. `minSdk` stays at 26 (constitution);
`targetSdk` → 36. **AGP 9 is explicitly out of scope** (decided with the user): it forces
built-in Kotlin (KGP 2.2.10), KSP/Hilt bumps, Gradle 9.1, the new DSL, and an interactive
Upgrade Assistant step — deferred to a separate maintenance workstream using the
`agp-9-upgrade` skill. The exact 8.x version + Detekt/Kover compatibility must be verified
before pinning.

**Library posture**: add `androidx.navigation3:navigation3-runtime` + `navigation3-ui`
(`1.0.0`, stable) and the ViewModel add-on `androidx.lifecycle:lifecycle-viewmodel-navigation3`
(`2.10.0-rc01` — the only RC deviation, recorded in plan Complexity Tracking); remove
`navigation-compose`.

**Alternatives rejected**:
- *Stay on Navigation 2*: contradicts the explicit user request.
- *Incremental Nav2+Nav3 coexistence*: the migration guide recommends a single atomic change;
  partial migration adds two navigation systems and bridging code.

---

## Decision 1 — Three-tab shell via the Navigation 3 multi-back-stack pattern

**Decision**: Use the Navigation 3 "common UI" pattern: a `TopLevelBackStack` holder keeps one
`SnapshotStateList` back stack **per tab** (Contadores, Estadísticas, Ajustes), tracks the
current top-level key, and exposes a **flattened** back stack to `NavDisplay`. `MainScaffold`
wraps `NavDisplay` in a `Scaffold` whose `bottomBar` is a `material3 NavigationBar`. The bar is
rendered **only when the currently visible entry is one of the three tab keys** — so Detail,
History, and Celebration (pushed onto the active tab's stack) hide it (FR-001/FR-002).
`NavigationBarItem.selected = (key == topLevelBackStack.topLevelKey)`. To survive process death
and config changes, the back stacks use the saveable `rememberNavBackStack`/`rememberSerializable`
approach from the migration guide's `NavigationState`.

**Rationale**: This is the canonical Navigation 3 bottom-nav recipe; it gives each tab its own
history and a single source of truth for "where am I" without a `NavController`. Back behaviour
maps directly to FR-031: popping the active tab's stack returns Detail → Contadores and
History → Detail; the app exits from the start (Contadores) tab.

**Alternatives rejected**:
- *Nav2 nested graph + `NavController`* (the previous plan): removed because the app is moving
  to Navigation 3.
- *One `NavDisplay` per tab*: breaks a single flattened back stack and complicates the
  "deep screen hides the bar" rule.

---

## Decision 2 — Create / Edit / Reset as Navigation 3 bottom-sheet entries

**Decision**: Model Create, Edit, and Reset-confirm as **NavKeys whose `NavEntry` carries
`BottomSheetSceneStrategy.bottomSheet()` metadata**, rendered by a `BottomSheetSceneStrategy`
added to `NavDisplay.sceneStrategies`. Because that strategy is **not yet in core Navigation
3**, copy its source into the project (`navigation/BottomSheetSceneStrategy.kt`) per the
official recipe. Navigating to the sheet pushes its key; backdrop tap / system back / Cancelar
pops it (FR-030). Edit reuses the Create form composable with the start-date field read-only
(FR-016).

**Rationale**: FR-013 mandates sheets, not full-screen destinations. Modelling them as entries
(rather than hoisted `ModalBottomSheet` state) keeps the back stack the single source of truth
in Navigation 3, so back/predictive-back and the scrim dismissal behave consistently with the
rest of navigation. The strategy still uses `material3 ModalBottomSheet` under the hood (IME +
inset handling for free).

**Alternatives rejected**:
- *Hoisted `ModalBottomSheet` visibility state*: diverges from the Navigation 3 back-stack model
  and double-tracks "is the sheet open".
- *Keep full-screen Create/Edit routes*: violates FR-013.

---

## Decision 2b — Deep link rebuilt as a synthetic back stack

**Decision**: The existing `daycounter://counter/{counterId}` deep link (widget/notification
taps) is handled the Navigation 3 way: a `DeepLinkResolver` parses the incoming `Intent` URI,
validates `counterId` to a `Long`, and **builds a synthetic back stack** `[Contadores, Detail(id)]`
on the Contadores tab so that "back"/"up" from Detail lands on Contadores (FR-031). A missing or
malformed `counterId`, or a counter that no longer exists, resolves to just `[Contadores]` (no
implicit trust of the extra — Principle VI).

**Rationale**: Deep links are not covered by the core Nav3 migration; the recipe approach is an
explicit synthetic back stack. This preserves the widget/notification entry points from `001`
without a `navDeepLink` graph.

**Alternatives rejected**:
- *Push only `Detail` with an empty back stack*: back would exit the app instead of going to
  Contadores, violating FR-031 and the "exit through Home" assumption.

---

## Decision 2c — Per-destination ViewModels in Navigation 3 (Hilt)

**Decision**: Add `rememberSaveableStateHolderNavEntryDecorator()` **and**
`rememberViewModelStoreNavEntryDecorator()` to `NavDisplay.entryDecorators` so each `NavEntry`
gets a correctly-scoped, saveable `ViewModelStore` (a fresh ViewModel per unique key instance,
cleared when the entry leaves the back stack). Counter-id arguments reach ViewModels via Hilt
**assisted injection**: `@HiltViewModel(assistedFactory = …)` + `@AssistedInject` constructor
taking the `@Assisted` NavKey, obtained in the entry with
`hiltViewModel(creationCallback = { it.create(key) })`. This requires the Hilt-Compose
integration version that exposes `hiltViewModel` with `creationCallback`.

**Rationale**: Without the `ViewModelStoreNavEntryDecorator`, ViewModels are not scoped to
entries and would leak or be shared incorrectly across Detail instances. Assisted injection is
the Nav3-recommended way to pass the `counterId` (replacing the Nav2 `SavedStateHandle` route
arg) while keeping Hilt-provided dependencies.

**Alternatives rejected**:
- *`SavedStateHandle` route args (Nav2 style)*: not how Nav3 passes keys; the key object itself
  is the argument carrier.

---

## Decision 3 — Milestone celebration auto-launch & dedup

**Decision**: Celebration eligibility is computed in `CounterDetailViewModel` when Detail
enters `RESUMED`. The "most recently reached" milestone is the highest value in
`{1, 7, 30, 100, 365, 1000}` that is `≤ current_streak`. If a `MilestoneRecord` exists for the
current attempt whose `celebration_shown = false` **and** it corresponds to that most-recent
milestone, the overlay auto-launches; on launch (or close), `MarkCelebrationsShownUseCase`
sets `celebration_shown = true` for **all** of the counter's milestone rows (FR-021) — so older
skipped milestones never produce a delayed celebration.

**Rationale**: Satisfies SC-004 (100 % auto-launch once, never twice) and the
"multiple milestones crossed at once" edge case (create a counter dated 32 days ago → only the
`30` celebration fires, not `1`/`7`/`30` in sequence), while the achieved-milestones list still
shows all three (FR-022). Dedup leverages the existing `UNIQUE(counter_id, milestone_days)`
index plus the new boolean flag.

**Alternatives rejected**:
- *Track "last celebrated milestone" as a single int on Counter*: cannot represent "shown vs
  not shown" per milestone and complicates the Revivir (on-demand) path.
- *Queue sequential celebrations*: explicitly rejected by the edge-case spec.

---

## Decision 4 — Reset as an atomic Room `@Transaction`

**Decision**: `ArchiveAndResetCounterUseCase` calls a single data-layer `@Transaction` method
that, in order: (a) inserts a `PastStreakRecord {counterId, streakDays=current, reason="Reiniciado",
endDate=today}` **only if** `current > 0`; (b) deletes all `MilestoneRecord` rows for the
counter; (c) sets `counter.startDate = today`. The streak-at-reset value is computed by
`CalculateStreakUseCase` before the transaction and passed in.

**Rationale**: FR-017 demands atomicity; Room `@Transaction` guarantees all-or-nothing. The
`current == 0` guard implements the "Reiniciar with a 0-day streak → no record" edge case.
Clearing milestone rows makes the next attempt able to re-achieve and re-celebrate every
milestone (FR-020/FR-024).

**Alternatives rejected**:
- *Three separate repository calls from the use case*: not atomic — a crash mid-sequence could
  archive without resetting, or reset without archiving.

---

## Decision 5 — Current-month calendar & sparkline from Compose primitives

**Decision**: `MonthCalendarGrid` is a `LazyVerticalGrid(columns = Fixed(7))` over the days of
the **current month only** (no navigation arrows — out of scope). Each cell state is derived in
`HistoryViewModel`: in-streak (highlighted), today (distinct), pre-streak-start (dimmed),
future-this-month (neutral, non-interactive). `Sparkline` is a `Canvas` polyline over the
per-day streak values (one point per day since `startDate`, capped to a reasonable window).

**Rationale**: FR-028/FR-029 fully specify the cell states and the single-month constraint. No
charting/calendar third-party dependency is justified (Principle IV/V) — `LazyVerticalGrid` and
`Canvas` are already available via `foundation`. Cell-state and sparkline-point computation are
pure functions, unit-testable without a device.

**Alternatives rejected**:
- *MPAndroidChart / Vico / a calendar library*: new dependency for two simple visuals; rejected.

---

## Decision 6 — Schema evolution: destructive fallback (fresh-install assumption)

**Decision**: Bump `AppDatabase` version and use `fallbackToDestructiveMigration(true)`. Add
`category` (TEXT, nullable) and `goal_milestone_target` (INTEGER, NOT NULL, default 30) to
`counters`; add `celebration_shown` (INTEGER/boolean, NOT NULL, default 0) to
`milestone_records`; create the `past_streak_records` table with FK→counters ON DELETE CASCADE
and an index on `(counter_id, end_date)`.

**Rationale**: The spec's Clarifications (Q1) and Assumptions explicitly state fresh-install
only, no production data, and no migration. A destructive fallback is the least-code,
no-speculative-backfill choice and is recorded in plan.md Complexity Tracking as a conscious,
spec-sanctioned exception. The old milestone set `{7,30,60,90,180,365}` is simply replaced.

**Alternatives rejected**:
- *Hand-written `Migration` objects*: more code and test surface for data the spec says does
  not exist; speculative and untestable.

---

## Decision 7 — Goal target vs. celebration set are independent

**Decision**: `goalMilestoneTarget` is chosen from `{7, 30, 100, 365}` (drives the ring
denominator and the "Hito alcanzado" badge); the celebration milestone set remains the full
`{1, 7, 30, 100, 365, 1000}`. The ring fill ratio is `min(1.0, currentDays / goalTarget)`; the
badge shows when `currentDays ≥ goalTarget`. Editing the target below the current streak makes
the ring full and the badge appear immediately (edge case).

**Rationale**: Matches FR-007/FR-008/FR-014 and the Assumptions note that "1 day" is too small
and "1000 days" too far to offer as first-time goal targets, while all six milestones still
fire celebrations regardless of the chosen target.

---

## Decision 8 — Streak recomputation on lifecycle resume (no live clock)

**Decision**: Each screen's ViewModel recomputes streaks when the screen enters
`RESUMED`/`STARTED` (collecting the repository `Flow` plus a `repeatOnLifecycle`-triggered
recompute against `LocalDate.now(clock)`). No live midnight observer is added.

**Rationale**: Clarifications Q5 — "Refresh on resume." Values may be momentarily stale until
re-entry; this is the accepted behaviour and avoids a background clock observer. The existing
injected `Clock` (`ClockModule`) is reused so tests can pin "today".

---

## Decision 9 — English default locale + Spanish (`values-es`) variant

**Decision**: Keep the **default/base locale English** in `res/values/strings.xml` and add a
**`res/values-es/strings.xml`** variant containing the verbatim Spanish copy required by
FR-032 (the milestone-celebration messages, informal "tú" tone, no emoji). The UI references
string resources only — no hardcoded literals in Composables or ViewModels. On a Spanish device
the es variant is shown; otherwise the English base. The exact Spanish strings from the spec
(US4 copy table) live in `values-es`; their English equivalents live in the base file.

**Rationale**: The constitution's Localization standard mandates "default locale MUST be
English; additional locales are additive." The earlier draft of this plan put Spanish as the
default, which contradicted that rule. This decision corrects it per the user's request:
English base, Spanish as an additive locale. Test-only technical labels may remain literal
(allowed exception).

**Alternatives rejected**:
- *Spanish as the base `values/` locale*: violates the constitution Localization standard.
- *Single hardcoded-Spanish strings in code*: violates "no hardcoded literals" and prevents the
  additive-locale model.

---

## Resolved spec clarifications (for traceability)

| # | Question | Resolution baked into this plan |
|---|----------|----------------------------------|
| Q1 | Migration for new fields | Fresh install; destructive fallback (Decision 6). |
| Q2 | Revivir target + list interactivity | Revivir opens the most-recent milestone; achieved list is informational only (Decision 3, FR-022/FR-024). |
| Q3 | Past-streak retention | No cap; UI paginates in batches of 50 (Decision 5 / FR-029). |
| Q4 | MilestoneRecord on reset | Reset deletes all rows for the counter, in the reset transaction (Decision 4 / FR-017). |
| Q5 | Midnight rollover while open | Recompute on resume; no live observer (Decision 8). |

**Output**: All decisions resolved. Ready for Phase 1 (data-model, contracts, quickstart).
