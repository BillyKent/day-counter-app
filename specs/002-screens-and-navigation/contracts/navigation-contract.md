# Navigation Contract: Screens and Navigation Overhaul (Navigation 3)

**Feature**: `002-screens-and-navigation` | **Date**: 2026-05-28

Defines the **Jetpack Navigation 3** route-key schema, the `entryProvider` entries, the
multi-back-stack tab model, bottom-bar visibility, back-stack behaviour, and the deep-link
synthetic back stack. This replaces the Navigation 2 (`NavHost`/`NavController`) contract from
`001`. This is the navigation "interface" the app exposes to itself and (via the retained deep
link) to the system.

---

## Route keys (`navigation/NavKeys.kt`)

All routes are type-safe `@Serializable` objects/classes implementing `NavKey`. There are no
string routes.

```kotlin
// Top-level (tab) keys — each owns its own back stack
@Serializable data object Contadores   : NavKey   // start key; app exits through this tab
@Serializable data object Estadisticas : NavKey
@Serializable data object Ajustes      : NavKey

// Full-screen child keys (hide the bottom bar)
@Serializable data class  Detail(val counterId: Long)               : NavKey
@Serializable data class  History(val counterId: Long)             : NavKey
@Serializable data class  Celebration(val counterId: Long, val milestone: Int) : NavKey

// Onboarding (one-time; rendered before the tab shell when onboarding not yet shown)
@Serializable data object Onboarding : NavKey

// Bottom-sheet keys (entries carrying BottomSheetSceneStrategy.bottomSheet() metadata)
@Serializable data object CreateCounter             : NavKey
@Serializable data class  EditCounter(val counterId: Long) : NavKey
@Serializable data class  ResetConfirm(val counterId: Long) : NavKey
```

| Key | Presentation | Bottom bar | Notes |
|-----|--------------|------------|-------|
| `Onboarding` | full screen | hidden | one-time; not a tab |
| `Contadores` | tab | **visible** | start key; Empty state when 0 counters |
| `Estadisticas` | tab | **visible** | US5 |
| `Ajustes` | tab | **visible** | hosts existing notification toggle (FR-005) |
| `Detail` | full screen | **hidden** | deep-link target |
| `History` | full screen | **hidden** | reachable only from Detail (FR-027) |
| `Celebration` | full screen overlay | **hidden** | auto-launch + Revivir |
| `CreateCounter` | bottom sheet | (sheet over tab) | hosted from Contadores |
| `EditCounter` | bottom sheet | (sheet over Detail) | start date read-only |
| `ResetConfirm` | bottom sheet | (sheet over Detail) | archive warning |

**Removed (Nav2 leftovers)**: string-based `Screen` sealed class, `AppNavGraph`/`NavHost`,
`counter/create` & `counter/{id}/edit` string routes, the `settings` standalone route, and
`navigation-compose`. The standalone `CreateCounterScreen`/`EditCounterScreen` files are deleted.

---

## Display & entry provider (`navigation/AppNavDisplay.kt`)

A single `NavDisplay` renders the flattened back stack from `TopLevelBackStack`:

```kotlin
NavDisplay(
    backStack = topLevelBackStack.backStack,          // flattened across tabs
    onBack = { topLevelBackStack.removeLast() },
    sceneStrategies = listOf(BottomSheetSceneStrategy()),   // copied-in strategy (not in core)
    entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),     // saveable UI state per entry
        rememberViewModelStoreNavEntryDecorator(),          // NavEntry-scoped ViewModels
    ),
    entryProvider = entryProvider {
        entry<Contadores>   { HomeScreen(...) }
        entry<Estadisticas> { StatsScreen(...) }
        entry<Ajustes>      { SettingsScreen(...) }
        entry<Detail>       { key -> CounterDetailScreen(counterId = key.counterId, ...) }
        entry<History>      { key -> HistoryScreen(counterId = key.counterId, ...) }
        entry<Celebration>  { key -> MilestoneCelebrationScreen(key.counterId, key.milestone, ...) }
        entry<CreateCounter>(metadata = BottomSheetSceneStrategy.bottomSheet()) { CreateCounterSheet(...) }
        entry<EditCounter>(metadata = BottomSheetSceneStrategy.bottomSheet())   { key -> EditCounterSheet(key.counterId, ...) }
        entry<ResetConfirm>(metadata = BottomSheetSceneStrategy.bottomSheet())  { key -> ResetConfirmSheet(key.counterId, ...) }
    },
)
```

ViewModels receive their NavKey via Hilt assisted injection:
`hiltViewModel<DetailVM, DetailVM.Factory>(creationCallback = { it.create(key) })`.

---

## Tab model (`navigation/TopLevelBackStack.kt` / `MainScaffold.kt`)

- `TopLevelBackStack` keeps one back stack per top-level key (`Contadores`, `Estadisticas`,
  `Ajustes`), tracks `topLevelKey`, and exposes a single flattened `backStack` to `NavDisplay`.
- `addTopLevel(key)` switches tab (preserving that tab's history); `add(key)` pushes a child onto
  the current tab; `removeLast()` pops and, when a tab's base key is popped, falls back to the
  start tab.
- Back stacks use the saveable `rememberNavBackStack`/`rememberSerializable` approach so tab
  history survives configuration change and process death.
- `MainScaffold` shows the `NavigationBar` **iff** `backStack.last()` ∈
  {`Contadores`, `Estadisticas`, `Ajustes`}; otherwise the bar is absent (FR-001/FR-002).
  `NavigationBarItem.selected = (key == topLevelBackStack.topLevelKey)`.

---

## Start destination (FR-003 / FR-004)

- First-ever launch (onboarding-shown flag false) → render `Onboarding`; on finish/skip, replace
  the back stack with `[Contadores]` (Empty state if 0 counters).
- Subsequent launches → start key `Contadores` directly.

---

## Back-stack contract (FR-031)

| From | Back / `removeLast()` returns to |
|------|----------------------------------|
| `Detail` | `Contadores` (its tab base) |
| `History` | `Detail` — **not** Contadores (Scenario US1-6) |
| `Celebration` | the entry that pushed it (typically `Detail`) |
| Sheet (`CreateCounter`/`EditCounter`/`ResetConfirm`) | pops the sheet entry; underlying screen unchanged (FR-030) |

**Delete from Detail (FR-012)**: rewrite the Contadores stack to `[Contadores]` (clearing
`Detail` and any `History`/`Celebration` above it) and switch to it. If it was the only counter →
Empty state.

**Tab indicator (US1-3)**: switching tabs updates `topLevelKey`; the previously selected tab
loses its indicator.

---

## Navigation actions (callbacks → `TopLevelBackStack` operations)

| Screen | Action | Operation |
|--------|--------|-----------|
| Home | tap counter card | `add(Detail(id))` — **Detail, not Edit** (FR-009) |
| Home | tap "+" / Empty CTA | `add(CreateCounter)` (sheet) |
| Home (after first Create) | sheet dismissed | list renders automatically (FR-018) |
| Detail | Editar | `add(EditCounter(id))` (sheet) |
| Detail | Reiniciar | `add(ResetConfirm(id))` (sheet) |
| Detail | Eliminar | confirm → delete → rewrite Contadores stack to `[Contadores]` |
| Detail | Abrir historial | `add(History(id))` |
| Detail | Revivir celebración | `add(Celebration(id, mostRecentMilestone))` |
| Detail (auto, on resume) | most-recent unseen milestone | `add(Celebration(id, N))` then mark shown |
| Celebration | "Seguir así" / X | `removeLast()` → Detail |
| History | back | `removeLast()` → Detail |
| Sheet | Guardar / Confirmar / Cancelar / backdrop | `removeLast()` (after persisting, if applicable) |

---

## Deep link & security (`navigation/DeepLinkResolver.kt`, Principle VI)

- Retain `daycounter://counter/{counterId}` (widget + notification `PendingIntent`s, unchanged
  in `:app`). The activity reads the launch `Intent`; `DeepLinkResolver` parses and validates
  `counterId` to `Long`.
- **Synthetic back stack**: a valid id seeds the Contadores tab with `[Contadores, Detail(id)]`
  so back/up lands on Contadores (FR-031). A missing/malformed id, or a non-existent counter,
  seeds just `[Contadores]` — no implicit trust of the intent extra.
- No **new** exported components are introduced; `History` and `Celebration` are in-app only
  (no deep link). `PendingIntent`s keep `FLAG_IMMUTABLE`.
