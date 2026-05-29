# Day Counter — Android UI Kit

Clickable prototype of the Day Counter Android app. Open `index.html` to navigate the screens — use the state picker pills above the phone to jump to onboarding, empty, stats or settings; the rest are reachable by tapping inside the phone.

Open `widgets.html` for the Android home-screen widgets (2×2 / 4×2) and a lockscreen with push notifications.

## Screens (8 total)
1. **Onboarding** — 3-slide intro (ring · targets · trophy + bell).
2. **Empty state** — first launch after onboarding ("Empieza por una meta pequeña").
3. **Home (Contadores)** — list with stats header, **filter chips (Todos / Activos / Pausados)** that filter the list live, FAB.
4. **Detail** — hero ring, next-milestone hint, **Pausar / Reanudar** primary action, edit/reset/delete + history shortcut. Paused counters show a frozen gray dashed ring, an "En pausa" banner and a resume button.
5. **History / Calendar** — sparkline of growth, monthly grid, past streaks list.
6. **Create sheet** — name + category + start date + goal.
7. **Edit sheet** — prefilled, locked start date.
8. **Milestone celebration** — full-screen ring at hito + motivational copy.
9. **Stats** — total / longest / milestones / avg streak + a dedicated **Pausas** card (en pausa ahora · días pausados · pausas totales) + weekly bars.
10. **Settings** — notifications · appearance · data. Interactive sheets: **Idioma** (6-language radio list with check), **Hora del recordatorio** (scrollable hour/minute wheel + Mañana/Mediodía/Noche presets), **Borrar todo** (destructive confirmation + undo toast).

## Pause feature — how the math works
Streak days are **derived, never stored**, so Contadores y Estadísticas can never disagree. The counter model carries `startDate`, `status` (`active`/`paused`), completed `pausePeriods` and `pausedSince`. `counterDays()` = elapsed time − every paused interval, with the clock frozen at `pausedSince` while paused. Example from the brief: start 1 ene, pausa 10 ene, reanuda 20 ene → the counter holds **9 días** on resume. Paused counters never generate milestone notifications and never lose accumulated progress. A production Android build persists this same shape locally (Room / DataStore) so it survives restarts. See `components.jsx` for the helpers (`counterDays`, `totalPausedDays`, `pauseCount`).

Plus: **Widgets** (3 sizes on a homescreen) and **Push notifications** (lockscreen) in `widgets.html`.

## Files
- `components.jsx` — `<Phone>`, `<TopBar>`, `<Button>`, `<IconButton>`, `<Card>`, `<Ring>`, `<Chip>`, `<Sheet>`, `<BottomNav>`, `<FAB>`, `<Input>`, `<StreakCard>`, `<Icon>`.
- `screen-*.jsx` — one file per screen.
- `app.jsx` — root with route state machine (`onboarding | empty | main | detail | history`) plus overlays for milestone, sheets.
- `index.html` — loads everything + state picker.
- `widgets.html` — homescreen widgets + push notifications, pure HTML/CSS (no React).

## Note on the starter frame
`android-frame.jsx` was copied from the starter as a reference but is **not used** — its default surface color is Material 3 mint-green, which fights the cream/teal Day Counter palette. We render a custom `<Phone>` component that uses our tokens.
