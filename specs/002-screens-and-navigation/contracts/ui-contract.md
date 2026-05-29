# UI Contract: Screens, Sheets & Overlay

**Feature**: `002-screens-and-navigation` | **Date**: 2026-05-28

Per constitution Principle III, every ViewModel exposes immutable `StateFlow<UiState>` and
(where one-shot effects exist) `SharedFlow<UiEvent>`. Composables are stateless and driven by
that state plus hoisted callbacks. This contract is the basis for the Compose UI tests that
must fail before each screen is built (Principle II). Requirement IDs reference `spec.md`.

---

## HomeScreen / HomeViewModel — Contadores tab (US1, US2)

**UiState**
- `loading: Boolean`
- `counters: List<CounterCardUi>` where `CounterCardUi { id, name, startDate, streakDays,
  goalMilestoneTarget, ringFillRatio (0..1), goalReached: Boolean, category? }`
- `summary: SummaryUi?` — `{ totalDays: Int, bestStreak: Int }` (null when 0 counters)
- `isEmpty: Boolean` (no counters → Empty state)
- `createSheetVisible: Boolean`

**Callbacks**: `onCardTap(id)` → Detail; `onAddTap()` → show Create sheet; `onCreateDismiss()`.

**Contract checks**:
- 0 counters → `isEmpty = true`, Empty state + primary CTA (FR-002 empty; US2-1).
- ≥1 counters → summary shows Total = Σ streakDays, Mejor racha = max streakDays (FR-006).
- Each card shows ring (`ringFillRatio = min(1, streak/target)`), streak number, name,
  start date (FR-007).
- `goalReached` (streak ≥ target) → "Hito alcanzado" badge (FR-008).
- Card tap opens **Detail**, never Edit (FR-009).
- Streaks recompute on resume (no live observer).

---

## StatsScreen / StatsViewModel — Estadísticas tab (US5)

**UiState**: `{ isEmpty: Boolean, totalAccumulated: Int, bestStreak: Int, activeCounters: Int }`

**Contract checks** (FR-025/FR-026):
- ≥1 counters → hero `Total acumulado` = Σ current streak days; secondary `Mejor racha`
  = max streak, `Contadores activos` = count.
- 0 counters → neutral empty state inviting creation.
- Example: counters (5, 30, 120) → Total 155, Mejor 120, Activos 3.

---

## SettingsScreen — Ajustes tab (US1)

**UiState**: `{ notificationsEnabled: Boolean }` (unchanged from `001`).
**Contract checks**: milestone-notification toggle present and functional; toggling takes
effect < 1 s and survives cold restart (FR-005, SC-007). No new controls (Assumptions).

---

## CounterDetailScreen / CounterDetailViewModel (US3, US4 auto-launch)

**UiState**
- `counter: CounterUi?` (null while loading / missing → navigate to Contadores)
- `streakDays: Int`, `goalMilestoneTarget: Int`, `ringFillRatio`
- `nextMilestoneHint: String?` — "X días para tu próximo hito" where X = nextMilestone −
  streak; null/replaced with "Has alcanzado todos los hitos" when streak ≥ 1000 (FR-010)
- `achievedMilestones: List<Int>` — milestones ≤ streak, informational only (FR-022, **non-interactive**)
- `canRevive: Boolean` (≥1 milestone reached)
- `editSheetVisible`, `resetSheetVisible`, `deleteConfirmVisible: Boolean`

**UiEvent (SharedFlow)**: `AutoLaunchCelebration(milestone: Int)`, `CounterDeleted`,
`NavigateHistory`.

**Contract checks**:
- Hero ring uses `goalMilestoneTarget` as denominator (US3-1).
- next-milestone = smallest of `{1,7,30,100,365,1000}` strictly > streak (FR-010); hidden/replaced at ≥1000 (US3-3).
- Editar → Edit sheet (US3-4); Reiniciar → Reset sheet (US3-5); Eliminar → confirm → delete →
  Contadores (FR-012, US3-6); history icon → History (US3-7).
- achieved chips are **not** tappable (US3-8).
- On resume, if most-recent milestone has an unseen `MilestoneRecord` →
  emit `AutoLaunchCelebration`; then `MarkCelebrationsShownUseCase` for all rows (FR-021, SC-004).
- "Revivir celebración" (only if `canRevive`) → Celebration for highest milestone ≤ streak (FR-024).

---

## Create / Edit bottom sheets (US6)

**Presentation**: bottom-sheet **NavKey entries** (`CreateCounter` / `EditCounter(id)`) carrying
`BottomSheetSceneStrategy.bottomSheet()` metadata — not hoisted `ModalBottomSheet` state (see
navigation-contract). Pushed via `add(...)`; dismissal = `removeLast()`.

**Shared form UiState**: `{ name, category, startDate, goalTarget,
nameError?, categoryError?, dateError?, saveEnabled: Boolean, isEdit: Boolean }`.

**Contract checks**:
- Sheet slides over current screen with backdrop (FR-013); fields: Nombre, Categoría,
  Fecha de inicio, Hito objetivo (FR-015).
- Hito objetivo chosen from `{7, 30, 100, 365}` (FR-014/FR-015, US6-2).
- Validation: name 1–100; category 0–50; startDate ≤ today (Create).
- Guardar (valid) → persist with new fields, sheet closes (US6-3).
- Create from Empty state → on close, list view renders automatically (FR-018, US6-4).
- Backdrop tap / Cancelar → close, no persistence (FR-030, US6-5).
- Edit: Nombre/Categoría/Hito objetivo editable; **Fecha de inicio read-only** with hint
  "Para empezar de cero, usa Reiniciar" (FR-016, US6-6); save → return to Detail updated (US6-7).

---

## Reset-confirm sheet (US7)

**Presentation**: bottom-sheet NavKey entry (`ResetConfirm(id)`) with
`BottomSheetSceneStrategy.bottomSheet()` metadata; dismissal = `removeLast()`.

**UiState**: `{ warningCopy }` (copy mentions the prior streak will be archived).

**Contract checks** (FR-017):
- Reiniciar → confirmation sheet with archive warning (US7-1).
- Cancelar / backdrop → no change (US7-2, FR-030).
- Confirmar → atomic: archive PastStreakRecord (if streak > 0) + delete all MilestoneRecords +
  startDate = today → return to Detail (US7-3).
- After a 15-day reset, History "Rachas anteriores" contains "15 días · Reiniciado · [date]" (US7-4).

---

## MilestoneCelebrationScreen / ViewModel (US4)

**UiState**: `{ milestone: Int, counterName: String, message: String }` where `message` is read
from a string resource keyed by milestone. The English base values live in
`res/values/strings.xml`; the **`res/values-es/strings.xml`** variant carries the verbatim
Spanish copy below (FR-032). The ViewModel resolves the resource id; it never hardcodes the text.

| Day | Spanish (`values-es`) message |
|-----|---------|
| 1 | Día 1. El más difícil ya empezó. |
| 7 | Una semana completa. Esto ya es un hábito que empieza. |
| 30 | 30 días. Pasaste el mes. |
| 100 | Cien días. Algo cambió en ti. |
| 365 | Un año entero. Esto ya no es una meta, es quién eres. |
| 1000 | Mil días. No hay palabras suficientes. |

**Contract checks** (FR-023):
- Full-screen overlay, bottom bar hidden; shows milestone number in hero ring, the copy, and
  the counter name (US4-2).
- "Seguir así" / close X → `popBackStack()` → Detail (US4-3).
- Auto-launch happens once per most-recent unseen milestone; never twice (US4-1, US4-4, SC-004).
- Revivir always reopens the most recent milestone (US4-5).

---

## HistoryScreen / HistoryViewModel (US8)

**UiState**
- `counterName: String`, `currentStreak: Int`
- `sparklinePoints: List<Int>` (one per day since startDate; 1 point for a same-day counter)
- `calendar: MonthCalendarUi` — current month; per-day cell state ∈
  {`InStreak`, `Today`, `PreStreak` (dimmed), `Future` (neutral, non-interactive)}
- `pastStreaks: List<PastStreakUi>` — `{ streakDays, reason, endDate }`
- `canLoadMore: Boolean`, `pageSize = 50`

**Callbacks**: `onBack()` → Detail; `onLoadMore()` → next 50 by `end_date DESC`.

**Contract checks** (FR-027/FR-028/FR-029):
- Reachable only from Detail; back returns to Detail (US8-1).
- Header: name + current streak + sparkline (US8-2).
- Calendar: current month only (no nav arrows); streak days highlighted; today distinct;
  pre-start dimmed; future neutral/non-interactive (US8-3).
- "Rachas anteriores": each row shows day count + reason + end date; paginated 50/batch,
  newest `end_date` first, "Ver más" loads next batch (US8-4).
- Counter created today → sparkline 1 point, calendar shows today only, past list empty (edge case).

---

## Cross-cutting (FR-032 + Localization)

All user-facing strings are externalized to string resources — **no hardcoded literals** in
Composables or ViewModels (constitution Localization standard). The **default locale is English**
(`res/values/strings.xml`); a **`res/values-es/strings.xml`** variant provides the Spanish copy
(informal "tú", no emoji) that FR-032 requires the user to see. UI tests assert against resource
ids / the es-locale values, not inline literals.
