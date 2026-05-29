# Contract: UI (ViewModel UiState / UiEvent)

**Feature**: `003-design-migration-features` | **Date**: 2026-05-29

ViewModels expose `StateFlow<UiState>` + `SharedFlow<UiEvent>` (Principle III). Each acceptance
scenario in spec US1–US7 maps to a Compose test; new tests fail before the screen exists (Principle II).

---

## Home / Contadores (`HomeViewModel`)
```
UiState(
  counters: List<CounterCardUi>,   // each: id, name, category, effectiveDays, goalTarget, isPaused
  filter: CounterFilter,           // ALL | ACTIVE | PAUSED
  counts: FilterCounts,            // all, active, paused
  totalEffectiveDays: Int, bestStreak: Int,
  isEmpty: Boolean, emptyKind: EmptyKind  // NO_COUNTERS | NO_ACTIVE | NO_PAUSED
)
Events: SetFilter(filter), OpenCounter(id), CreateNew
```
- Filter changes list + which empty message shows (FR-014). Totals use effective days (FR-008/FR-015).

## Counter Detail (`CounterDetailViewModel`)
```
UiState(counter, effectiveDays, isPaused, pausedTotalDays, nextMilestone, daysToNext,
        achievedMilestones, canShowCelebration)
Events: TogglePause, Edit, Reset, Delete, OpenHistory, ShareMilestone?
```
- `TogglePause` → Pause/Resume use case; paused renders dashed/muted ring + "En pausa" + banner
  ("Sin notificaciones · N días en pausa") + primary "Reanudar" (FR-007..FR-011).
- Paused suppresses milestone celebration (FR-011).

## Create / Edit sheets (`CreateCounterViewModel`, `EditCounterViewModel`)
```
UiState(name, category: CategoryKey?, startDate, goalTarget, nameError, categories: List<CategoryKey>)
Events: SetName, SelectCategory, SetStartDate(create-only), SelectGoalTarget, Save, Cancel
```
- Category is a chip set from `Counter.CATEGORIES` (R10). Edit keeps startDate read-only.

## Stats / Estadísticas (`StatsViewModel`)
```
UiState(summary: StatsSummary, pause: PauseStats, week: WeeklyActivity, isEmpty)
```
- Hero `totalEffectiveDays`; grid Mejor racha / Hitos / Contadores activos / Racha media; Pausas card
  (pausedNow, totalPausedDays, totalPauses); weekly bars with today emphasized (FR-026..FR-029).

## Settings / Ajustes (`SettingsViewModel`)
```
UiState(milestoneEnabled, dailyReminderEnabled, reminderTime, language, appearance, counterCount)
Events: ToggleMilestone, ToggleDailyReminder, OpenReminderTime, SaveReminderTime(t),
        OpenLanguage, SelectLanguage(l), SetAppearance(m), OpenEraseAll, ConfirmEraseAll, UndoErase
UiEvent: LanguageChanged(requiresRecreate), ShowUndoToast, DataErased, DataRestored
```
- `SelectLanguage` persists then emits `LanguageChanged` → Activity `recreate()` (R4, FR-016..FR-020).
- `ToggleDailyReminder`/`SaveReminderTime` persist + (re)schedule worker (FR-022..FR-025).
- `ConfirmEraseAll` → erase + `ShowUndoToast`; `UndoErase` within window → restore (FR-030/FR-031).

## Sheets as Nav3 entries (`NavKeys`)
`LanguageSheet`, `ReminderTimeSheet`, `EraseAllSheet` — bottom-sheet entries via the existing
`BottomSheetSceneStrategy`; backdrop/back dismiss pops the entry.

## Theme contract (US1)
- All screens consume `MaterialTheme` + `LocalDayCounterColors`; **no hardcoded colors**.
- `ProgressRing` accepts `paused: Boolean` (dashed/muted) and `milestone: Boolean` (terracotta+glow).
- Paused & milestone states conveyed by **text + shape**, never color alone (Principle I; SC-002/006).
- Appearance: `Theme(darkTheme = when(appearance){SYSTEM->isSystemInDarkTheme(); LIGHT->false; DARK->true})`,
  `dynamicColor = false` (R3).
