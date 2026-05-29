# Feature Specification: Screens and Navigation Overhaul

**Feature Branch**: `002-screens-and-navigation`

**Created**: 2026-05-28

**Status**: Draft

**Input**: User description: "Descripción funcional — Day Counter (full screen-by-screen user stories, navigation graph, excluding counter status filter, data export, notifications center, share, settings expansion beyond the existing notification toggle, calendar month navigation, and deferred Stats metrics)."

## Overview

This feature describes the functional surface of Day Counter as it should exist after the design-system rework. It is scoped to **behavior and navigation only**, not visual design — the visual rework is a separate workstream that follows.

Compared to the current app (spec `001-day-counter-app`), this iteration:

- Reorganizes the app around a **three-tab bottom navigation** (Contadores · Estadísticas · Ajustes).
- Adds two brand-new screens (**Stats**, **History/Calendar**) and one full-screen overlay (**Milestone Celebration**).
- Enriches the **Home** list with a global summary and per-card progress rings/milestone badges.
- Enriches the **Detail** screen with next-milestone information, achieved-milestones list, and Reset/Delete actions that the domain already supports but the UI does not yet expose.
- Refactors **Create/Edit** as bottom sheets and adds two new fields on a counter: **category** and **goal milestone target**.
- Persists **past streaks** so a user can review previous attempts after a reset.
- Updates the **milestone set** from `{7, 30, 60, 90, 180, 365}` to `{1, 7, 30, 100, 365, 1000}` to align with the design's celebratory messages.

This spec contains only **well-defined functionality**. The following items appear in the design but are intentionally **not included** because they are either undefined or pending decisions; each can be added in a future spec once its behavior is decided:

- Notifications Center (bell icon and its destination screen)
- "Compartir" button on the Milestone Celebration
- Month navigation arrows on the History calendar
- Expanded Settings (reminder time picker, dark mode toggle, language picker, "Borrar todo")
- Stats metrics whose formula is undefined: *Hitos alcanzados* count, *Constancia %*, and the weekly activity bar chart
- Counter status filter (active / paused / archived) — out of scope at user's request
- Data export — out of scope at user's request

## Clarifications

### Session 2026-05-28

- Q: Migration default for new fields (`goal_milestone_target`, `category`) on existing counters? → A: No migration needed — assume the app installs from scratch. There is no production data to preserve.
- Q: "Revivir celebración" from Detail — which milestone opens, and is the achieved-milestones list interactive? → A: A single "Revivir celebración" button reopens **the most recently reached** milestone. The achieved-milestones list in Detail is informational only (not tappable).
- Q: Past-streak retention policy? → A: **No cap; UI pagination only.** All past-streak records are kept indefinitely; History's "Rachas anteriores" list paginates in batches of 50 (with a "Ver más" affordance).
- Q: What happens to `MilestoneRecord` entries when a counter is reset? → A: **Reset deletes all `MilestoneRecord` entries for the counter** (in the same transaction as the reset). The new streak is a true fresh start — the user can reach and celebrate `1, 7, 30, ...` again. "Hitos alcanzados" in Detail always reflects the **current** streak attempt.
- Q: Live behavior when device clock crosses midnight while the app is open? → A: **Refresh on resume.** Each screen recomputes the streak when it enters `RESUMED`/`STARTED` (standard Android lifecycle). No live clock observer; values may appear momentarily stale until the user re-enters the screen.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Three-tab navigation shell (Priority: P1)

The user navigates between the three top-level destinations using a persistent bottom navigation bar. Entering any deeper screen (Detail, History, Celebration) hides the bar to reinforce the sense of "having gone in". The Ajustes tab initially carries only the existing milestone-notification toggle (no other new controls); its value in this spec is the structural move from a Home top-bar icon to a first-class tab.

**Why this priority**: This is a structural prerequisite — Stats cannot live anywhere without it, and reorganizing Settings away from the Home top-bar icon depends on it.

**Independent Test**: Launch the app with at least one counter already created. Verify three tabs are visible at the bottom and switching between them shows the corresponding screen content (Contadores list, Estadísticas content, Ajustes with the existing milestone-notification toggle). Tap a counter card and verify the bottom bar disappears; tap "back" and verify it returns.

**Acceptance Scenarios**:

1. **Given** the user opens the app for the first time, **When** onboarding finishes, **Then** the app shows the **Empty state** for the *Contadores* tab with the bottom navigation visible.
2. **Given** the user has at least one counter, **When** the app starts, **Then** the app opens directly on the *Contadores* tab.
3. **Given** the user is on any of the three top-level tabs, **When** they tap another tab, **Then** the corresponding tab content is shown and the previously-selected tab loses its selected indicator.
4. **Given** the user is on the *Contadores* tab, **When** they tap a counter card, **Then** the Detail screen opens and the bottom navigation is hidden.
5. **Given** the user is on the Detail screen, **When** they press the back affordance, **Then** they return to the *Contadores* tab with the bottom navigation visible again.
6. **Given** the user is on Detail and opens History, **When** they press back, **Then** they return to Detail (not Home).
7. **Given** the user opens the Ajustes tab, **When** the tab content renders, **Then** the existing milestone-notification toggle is shown and continues to function as in the current app.

---

### User Story 2 - Enriched counter list (Priority: P1)

On the Contadores tab the user sees a global summary (total accumulated days, best streak) at the top, followed by one card per counter. Each card shows the streak number inside a circular progress ring, the counter name, the start date, and a "Hito alcanzado" badge when the counter has reached or surpassed its goal milestone target. A floating "+" button creates a new counter.

**Why this priority**: This is the primary surface users see every day; without it the rest of the experience feels like a checklist of features without a home.

**Independent Test**: Create three counters with different streak lengths (e.g., 3, 15, 102 days), where one of them passed its goal milestone. Verify the summary cards reflect the correct totals, each card shows its ring filled proportionally to its goal target, and the one past its target shows the "Hito alcanzado" badge.

**Acceptance Scenarios**:

1. **Given** the user has 0 counters, **When** the *Contadores* tab loads, **Then** the **Empty state** is shown with a warm welcome message and a primary CTA to create the first counter.
2. **Given** the user has ≥1 counters, **When** the *Contadores* tab loads, **Then** the top of the screen shows two summary cards: **Total** (sum of streak days across all counters) and **Mejor racha** (max streak across all counters).
3. **Given** any counter is shown in the list, **When** rendered, **Then** the card displays the streak day count, a circular progress ring whose fill ratio = `current_days / goal_milestone_target`, the counter name, and the start date.
4. **Given** a counter's current streak ≥ its goal milestone target, **When** the card is rendered, **Then** a "Hito alcanzado" badge appears on the card.
5. **Given** the user taps the floating "+" button on Home (or the equivalent CTA in Empty state), **When** the action triggers, **Then** the **Create** bottom sheet opens.
6. **Given** the user taps a counter card, **When** the action triggers, **Then** the **Detail** screen opens for that counter.

---

### User Story 3 - Counter detail with full action set (Priority: P1)

When the user opens a counter, they see the streak as a hero ring + large number, a hint of how many days remain to the next milestone, the list of milestones already achieved in this streak attempt (informational only), and four primary actions: **Editar**, **Reiniciar**, **Eliminar**, and **Abrir historial**. If a milestone has already been reached, a fifth action lets them re-open the celebration for the most recently reached milestone.

**Why this priority**: Reset and Delete already exist as domain operations in the current app but the UI does not invoke them today. Detail also currently navigates incorrectly (Home goes to Edit instead of Detail) — fixing this is foundational for History, Celebration, and the achieved-milestones experience.

**Independent Test**: Open a counter that is 35 days old (past the 30-day milestone). Verify the hero shows "35", "5 días para tu próximo hito" appears, the milestones list shows `1, 7, 30` as informational chips, and tapping each of Editar/Reiniciar/Eliminar/Abrir historial/Revivir celebración performs the expected action.

**Acceptance Scenarios**:

1. **Given** the user taps a counter card in Home, **When** Detail opens, **Then** the streak number appears in a hero ring with the goal milestone target as the ring's denominator.
2. **Given** the user is on Detail and the current streak < `1000`, **When** the screen renders, **Then** a hint appears showing "X días para tu próximo hito" where X is `next_milestone - current_days` and `next_milestone` is the smallest milestone in `{1, 7, 30, 100, 365, 1000}` strictly greater than the current streak.
3. **Given** the current streak ≥ `1000`, **When** Detail renders, **Then** the next-milestone hint is hidden or replaced with a "Has alcanzado todos los hitos" message.
4. **Given** the user is on Detail, **When** they tap **Editar**, **Then** the **Edit** bottom sheet opens prefilled with the counter's data.
5. **Given** the user is on Detail, **When** they tap **Reiniciar**, **Then** the **Reset confirmation** bottom sheet opens.
6. **Given** the user is on Detail, **When** they tap **Eliminar**, **Then** a destructive confirmation appears, and on confirmation the counter is deleted and the user returns directly to the *Contadores* tab.
7. **Given** the user is on Detail, **When** they tap the calendar/history icon, **Then** the **History** screen opens.
8. **Given** the current streak has reached at least one milestone (e.g., ≥ 7), **When** Detail renders, **Then** the list of achieved milestones is shown as informational chips/rows (e.g., `1, 7`); these elements are **not interactive** — they do not trigger the celebration.
9. **Given** the current streak has reached at least one milestone, **When** the user taps the single "Revivir celebración" button, **Then** the **Milestone celebration** full-screen overlay opens for the **most recently reached** milestone (i.e., the highest value in `{1, 7, 30, 100, 365, 1000}` that is `≤ current_streak`).

---

### User Story 4 - Milestone celebration overlay (Priority: P2)

When the user's streak first crosses a milestone day (1, 7, 30, 100, 365, 1000), a full-screen celebration appears with a large animated ring, the milestone number, and a milestone-specific motivational message. The user can dismiss with "Seguir así" or the close X. The screen tapes over the bottom navigation; closing returns to wherever the user was (typically Detail).

**Why this priority**: This is the emotional payoff that justifies the whole product premise; high value but depends on Detail being navigable (US3) and on the milestone set being updated.

**Independent Test**: Create a counter whose `start_date` is exactly **7 days before today** so its streak naturally evaluates to 7. Open Detail; verify the celebration auto-launches the first time with the 7-day copy ("Una semana completa. Esto ya es un hábito que empieza."). Tap "Seguir así" to close. Reopen Detail and verify the celebration does **not** auto-launch again. Tap "Revivir celebración" and verify it opens on demand for the 7-day milestone.

**Acceptance Scenarios**:

1. **Given** a counter's streak first equals a value `N ∈ {1, 7, 30, 100, 365, 1000}` and no celebration was previously shown for that `(counter_id, N)` pair, **When** the user opens Detail for that counter, **Then** the milestone celebration appears automatically.
2. **Given** the celebration is showing, **When** the screen renders, **Then** it displays: the milestone number in a hero ring, the milestone-specific message (table below), and the counter's name.
3. **Given** the celebration is showing, **When** the user taps "Seguir así" or the close X, **Then** the celebration closes and returns to Detail.
4. **Given** the celebration was previously dismissed for this `(counter, milestone)`, **When** the user reopens Detail, **Then** it does **not** auto-launch again.
5. **Given** any past milestone has been reached, **When** the user taps "Revivir celebración" in Detail, **Then** the celebration opens for **the most recently reached** milestone. (There is no per-hito selector.)

**Milestone copy table** (verbatim, Spanish):

| Day  | Message                                                 |
| ---- | ------------------------------------------------------- |
| 1    | Día 1. El más difícil ya empezó.                        |
| 7    | Una semana completa. Esto ya es un hábito que empieza.  |
| 30   | 30 días. Pasaste el mes.                                |
| 100  | Cien días. Algo cambió en ti.                           |
| 365  | Un año entero. Esto ya no es una meta, es quién eres.   |
| 1000 | Mil días. No hay palabras suficientes.                  |

---

### User Story 5 - Estadísticas tab (Priority: P2)

The user opens the Estadísticas tab and sees a global view of their discipline across all counters: total accumulated days as a hero number, and a 1×2 row of secondary metrics (best streak, active counters count).

**Why this priority**: A new tab that gives the app a "look at your progress" surface; doesn't depend on Detail/Celebration but lives in the new bottom-nav structure (US1).

**Independent Test**: With three counters (5, 30, 120 days), open Estadísticas. Verify Total acumulado = 155, Mejor racha = 120, Contadores activos = 3.

**Acceptance Scenarios**:

1. **Given** the user has ≥1 counters, **When** the Estadísticas tab loads, **Then** a hero card shows **Total acumulado** = sum of current streak days across all counters.
2. **Given** the Estadísticas tab is shown, **When** the screen renders, **Then** two secondary metric cards display: **Mejor racha** (max streak across all counters) and **Contadores activos** (count of counters).
3. **Given** the user has 0 counters, **When** the Estadísticas tab loads, **Then** the tab shows a neutral empty state inviting the user to create their first counter.

---

### User Story 6 - Create and Edit counters as bottom sheets, with category + goal target (Priority: P2)

Creating and editing a counter happens in a bottom sheet that slides over the current screen, not in a separate screen. The sheet collects: **Nombre** (free text), **Categoría** (free text), **Fecha de inicio** (date, ≤ today, **editable only on Create**), and **Hito objetivo** (single choice from `{7, 30, 100, 365}` days). Edit is identical except the start date is read-only with a hint to use **Reiniciar** for that purpose.

**Why this priority**: This is both a UX refactor (full screen → sheet) and a data model expansion (two new fields). Required for the enriched Home (US2) to show categories and ring goals correctly.

**Independent Test**: From Home, tap "+" and verify the bottom sheet slides up. Enter "Dejar de fumar", category "Salud", today's date, goal 30 days. Save and verify a new counter appears in the list with a ring sized to 30. Open it in Detail, tap Editar, verify the sheet is prefilled and the date field is non-editable with a hint about Reiniciar.

**Acceptance Scenarios**:

1. **Given** the user taps "+" on Home or the CTA on Empty state, **When** Create triggers, **Then** a bottom sheet slides up with a backdrop and form fields: Nombre, Categoría, Fecha de inicio, Hito objetivo.
2. **Given** the Create sheet is open, **When** the user picks a Hito objetivo, **Then** they choose one value from `{7, 30, 100, 365}` days.
3. **Given** the Create sheet is open and all required fields are valid, **When** the user taps Guardar, **Then** the counter is persisted with the new fields and the sheet closes.
4. **Given** the user was on the Empty state and just saved their first counter, **When** the sheet closes, **Then** the *Contadores* tab transitions automatically to the populated list view.
5. **Given** any sheet is open, **When** the user taps the backdrop or "Cancelar", **Then** the sheet closes with no changes persisted.
6. **Given** the user opens Edit from Detail, **When** the sheet appears, **Then** Nombre, Categoría, and Hito objetivo are editable while Fecha de inicio is read-only and shows a hint such as "Para empezar de cero, usa Reiniciar".
7. **Given** the user saves an Edit, **When** the sheet closes, **Then** the user returns to Detail with the updated values.

---

### User Story 7 - Reset confirmation + past-streak persistence (Priority: P2)

Reiniciar a counter is destructive of the current streak but **not** of history. The confirmation sheet warns the user, mentions that the previous streak will be saved to history, and offers Confirmar / Cancelar. On confirm, the current streak is archived as a past-streak record and the counter's start date becomes today.

**Why this priority**: Required for History (US8) to have content; without persistence past streaks cannot be shown.

**Independent Test**: Create a counter dated 15 days ago. Open Detail → Reiniciar → Confirmar. Verify the streak resets to 0, the counter still exists, and a 15-day past-streak record was created. Open History and verify the 15-day past streak appears under "Rachas anteriores".

**Acceptance Scenarios**:

1. **Given** the user taps Reiniciar on Detail, **When** the action triggers, **Then** a bottom-sheet confirmation appears with warning copy that mentions the streak will be archived.
2. **Given** the confirmation sheet is open, **When** the user taps Cancelar or the backdrop, **Then** the sheet closes with no change.
3. **Given** the confirmation sheet is open, **When** the user taps Confirmar, **Then** (a) a past-streak record is persisted with the counter id, the streak days at reset time, the reset reason "Reiniciado", and the end date (today); (b) the counter's start date becomes today (streak = 0); (c) the user returns to Detail.
4. **Given** a counter has been reset once with a 15-day streak, **When** the user opens History for that counter, **Then** the past-streak list contains a row "15 días · Reiniciado · [date]".

---

### User Story 8 - History / Calendar per counter (Priority: P3)

From Detail, the calendar icon opens a History screen showing: a header summary card with the counter name, current streak and a sparkline of growth; a monthly calendar grid **for the current month** with streak days highlighted, today distinguished, and past/future days dimmed; and a list of past streaks (from US7's persistence). The calendar shows only the current month — month navigation arrows are not part of this spec.

**Why this priority**: This is the most data-rich screen and depends on past-streak persistence (US7) being in place. Lower priority because users get value from US1-7 without it.

**Independent Test**: For a 12-day-old counter that was previously reset twice (3 days, 14 days), open History from Detail. Verify the calendar shows the current month with the streak days highlighted, today's cell is visually distinct, the sparkline shows 12 data points growing, and the past-streaks list shows both prior runs with their reasons and end dates.

**Acceptance Scenarios**:

1. **Given** the user is on Detail and taps the calendar icon, **When** the action triggers, **Then** the History screen opens with a back affordance that returns to Detail.
2. **Given** History is open, **When** the header renders, **Then** the counter name, current streak day count, and a sparkline of the streak growth are shown.
3. **Given** History is open, **When** the calendar grid renders, **Then** (a) the **current month** is displayed (no month navigation); (b) each cell in the current streak window is highlighted; (c) today's cell is distinguished from the streak fill; (d) days before the streak start are dimmed; (e) future days within the current month are visually neutral and non-interactive.
4. **Given** the counter has past-streak records, **When** History renders, **Then** a "Rachas anteriores" section lists each record showing day count, reset reason, and end date, paginated in batches of 50 (most recent first by `end_date`) with a "Ver más" affordance.

---

### Edge Cases

- **First app launch never shows onboarding twice**: Once finished or skipped, the user always enters the post-onboarding flow on every subsequent launch.
- **Counter aged in the future**: Start date cannot be in the future; Create/Edit must reject this case.
- **Time zone / clock change**: Streak day count is calculated against the device's current local date. If the clock changes backwards the streak does not retroactively decrement; if it changes forwards the streak advances naturally. (No special accommodation.)
- **Natural midnight rollover while the app is open**: Streak values are recomputed when each screen enters `RESUMED`/`STARTED`. While a screen is already foregrounded and idle, displayed values may remain at the previous day's count until the user navigates away and back (or until the screen is rebound for any other reason). No live clock observer is required.
- **Multiple milestones crossed at once**: If a counter goes from 6 to 7 days (1 milestone) the celebration auto-launches. If a user creates a counter dated 32 days ago, the celebration triggers once for the **most recently reached** milestone (`30`), not three sequential celebrations (`1`, `7`, `30`). The achieved-milestones list still records all three.
- **Goal target reduced below current streak via Edit**: If a user edits a counter from goal 100 to goal 30 while at 45 days, the card immediately shows "Hito alcanzado" and the ring is full.
- **Counter deleted from Detail**: The user is returned to the *Contadores* tab, not to History or Celebration even if those were stacked in between. If the deleted counter was the user's only counter, they land on the Empty state.
- **Reiniciar with a 0-day streak**: If the user resets when current streak is 0, no past-streak record is created (nothing to archive); the start date is set to today (no-op visible).
- **History viewed for a counter created today**: Sparkline has one point; calendar shows today only; "Rachas anteriores" is empty.
- **All milestones reached (≥ 1000 days)**: Detail's "próximo hito" hint is replaced; Celebration's "Revivir" still works for the most recent milestone.

## Requirements *(mandatory)*

### Functional Requirements

**Navigation & shell**

- **FR-001**: System MUST present a persistent bottom navigation with three destinations — *Contadores*, *Estadísticas*, *Ajustes* — visible on those three screens and **only** those three.
- **FR-002**: System MUST hide the bottom navigation while on Detail, History, and Milestone Celebration.
- **FR-003**: System MUST start on *Contadores* whenever the user already has at least one counter on app launch.
- **FR-004**: System MUST start on Onboarding the first time the app is launched; on completion or skip, the user lands on Empty state for *Contadores*.
- **FR-005**: The *Ajustes* tab MUST host the existing milestone-notification toggle from the current app (no new controls are introduced by this spec).

**Counter list & detail**

- **FR-006**: On *Contadores* with ≥1 counters, System MUST show a global summary with **Total** (sum of streak days) and **Mejor racha** (max streak).
- **FR-007**: Each counter card MUST show a circular ring proportional to `streak_days / goal_milestone_target`, the streak number, the counter name, and the start date.
- **FR-008**: System MUST display a "Hito alcanzado" badge on any card whose `streak_days ≥ goal_milestone_target`.
- **FR-009**: Tapping a counter card MUST open Detail for that counter; the current Home → Edit navigation MUST be removed.
- **FR-010**: Detail MUST show the streak in a hero ring and a "X días para tu próximo hito" hint where X is the gap to the smallest milestone in `{1, 7, 30, 100, 365, 1000}` strictly greater than current streak.
- **FR-011**: Detail MUST expose actions: Editar, Reiniciar, Eliminar, Abrir historial, Revivir celebración (the last only when at least one milestone has been reached).
- **FR-012**: Eliminar MUST delete the counter (and cascade to its past-streak records and milestone records) and return the user to the *Contadores* tab.

**Counter creation, editing, reset**

- **FR-013**: Create and Edit MUST be bottom sheets, not separate full-screen destinations.
- **FR-014**: The Counter entity MUST gain two new persisted fields: `category` (free text, 0–50 characters, optional) and `goal_milestone_target` (one of `7`, `30`, `100`, `365`; required, default `30`).
- **FR-015**: Create MUST collect Nombre (1–100 chars), Categoría (optional), Fecha de inicio (≤ today), and Hito objetivo (one of `{7, 30, 100, 365}`).
- **FR-016**: Edit MUST allow modifying Nombre, Categoría, and Hito objetivo. Fecha de inicio MUST be read-only in Edit and accompanied by a hint pointing the user to Reiniciar.
- **FR-017**: Reiniciar MUST require a confirmation sheet stating that the prior streak will be archived. On confirm, System MUST atomically (a) persist a past-streak record with `{counter_id, days_at_reset, reason="Reiniciado", end_date=today}` if `days_at_reset > 0`, (b) **delete all `MilestoneRecord` entries for this counter** so the next attempt can re-achieve and re-celebrate every milestone, (c) set the counter's `start_date = today`.
- **FR-018**: After saving a Create from Empty state, the *Contadores* tab MUST transition to the populated list view without an extra user step.

**Milestones & celebration**

- **FR-019**: The milestone set MUST be `{1, 7, 30, 100, 365, 1000}`.
- **FR-020**: When a counter's streak first equals a milestone value during the **current attempt**, System MUST record the achievement (deduplicated per `(counter_id, milestone)` **within the current attempt only** — see FR-017, which clears prior `MilestoneRecord` rows on reset) and dispatch a milestone notification if the existing milestone-notification toggle is enabled.
- **FR-021**: When the user opens Detail for a counter whose most-recent achievement has never been displayed as a celebration, System MUST auto-launch the Milestone Celebration overlay for that milestone, then mark `celebration_shown=true` for **all** the counter's `MilestoneRecord` rows (so older skipped milestones never trigger a delayed celebration on later opens — only the most recent unseen milestone gets the auto-launch).
- **FR-022**: Detail MUST list all milestones already achieved in the **current** streak attempt, as informational (non-interactive) elements.
- **FR-023**: Milestone Celebration MUST show the milestone-specific copy from the table in US4 and the counter name; from it, "Seguir así" or the close X MUST close it and return to Detail.
- **FR-024**: A single "Revivir celebración" button on Detail MUST re-open the celebration for the most recently reached milestone of the current streak (the highest value in `{1, 7, 30, 100, 365, 1000}` that is `≤ current_streak`).

**Stats**

- **FR-025**: The Estadísticas tab MUST display **Total acumulado** (sum of current streak days across all counters), **Mejor racha** (max streak), and **Contadores activos** (count of counters).
- **FR-026**: With 0 counters, Estadísticas MUST show a neutral empty state inviting counter creation.

**History**

- **FR-027**: History MUST be reachable only from Detail; it MUST hide the bottom navigation.
- **FR-028**: History MUST render a monthly calendar **for the current month only** where current-streak days are highlighted, today is distinguished from those days, days before the streak start are dimmed, and future days within the current month are neutral and non-interactive. No month-navigation controls are part of this spec.
- **FR-029**: History MUST show a sparkline of streak growth and a "Rachas anteriores" list populated from the past-streak records. The list MUST be paginated client-side in batches of 50 records (most recent first by `end_date`); a "Ver más" affordance MUST load the next batch. No retention cap is applied at the data layer — all past streaks are persisted indefinitely.

**Cross-cutting**

- **FR-030**: All sheets (Create, Edit, Reset confirmation) MUST close on backdrop tap or explicit Cancelar with no persisted change.
- **FR-031**: A back navigation from any non-tab screen MUST return to the screen that opened it (Detail → Contadores, History → Detail, Celebration → Detail).
- **FR-032**: All copy in this feature MUST be in Spanish, addressing the user with informal "tú" tone, no emoji in UI, per the existing brand guidelines.

### Key Entities

- **Counter**: A user-defined streak. Existing fields: `id`, `goal_name`, `start_date`, `created_at`. **New fields in this spec**: `category` (optional free text), `goal_milestone_target` (one of `{7, 30, 100, 365}`).
- **MilestoneRecord**: Persistent record that a milestone was reached for a `(counter, milestone)` pair **within the counter's current streak attempt**. The allowed `milestone_days` set is `{1, 7, 30, 100, 365, 1000}`. **New field**: `celebration_shown` (boolean, defaults to false; set true after the first auto-launch of the celebration overlay for that record). **Lifecycle**: rows are deleted as part of the Reset transaction (FR-017) and cascade-deleted when the parent Counter is deleted (FR-012).
- **PastStreakRecord** *(new entity)*: A snapshot of a streak archived at reset time. Fields: `id`, `counter_id` (FK, cascade delete), `streak_days` (int), `reason` (string; initially only `"Reiniciado"`), `end_date` (LocalDate), `created_at` (Instant).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A first-time user can go from launching the app to having a created counter visible on the *Contadores* tab in under **60 seconds** of elapsed time.
- **SC-002**: A returning user with at least one counter sees the *Contadores* tab on every cold start in under **2 seconds** wall-clock from tap to first paint of the list.
- **SC-003**: From the *Contadores* tab, a user can navigate to any of the functional surfaces in this spec (Onboarding excluded) in **at most 3 taps**.
- **SC-004**: Once a milestone is reached, the celebration overlay auto-launches on the next Detail open **100%** of the time, and never auto-launches a second time for the same `(counter, milestone)`.
- **SC-005**: After a Reiniciar, the past streak is visible in History within **the same session** without requiring a relaunch.
- **SC-006**: All 32 functional requirements above are verifiable by end-to-end UI tests against the running app — i.e., the spec contains no requirement that cannot be exercised without the deferred visual rework.
- **SC-007**: Toggling the milestone-notification setting in *Ajustes* takes effect within **1 second** of the user toggling the switch and survives an app cold restart.

## Assumptions

- **Fresh install only**: This spec assumes the app is installed from scratch (no production data to migrate). There is no Room migration logic, no backfill, and no preservation of old `MilestoneRecord` values from the previous set `{7, 30, 60, 90, 180, 365}`. If a future installed base needs to be migrated, that is a separate workstream.
- **Categories are free text**, not a closed taxonomy. No pre-defined category list is provided by the design; supporting free text is the lowest-friction default and matches the design's chip aesthetic.
- **Goal milestone target is independent from the celebration set**. The user picks a target from `{7, 30, 100, 365}` (a reasonable subset of the celebration set `{1, 7, 30, 100, 365, 1000}` — excluding "1 day" as too small to set as a goal, and "1000 days" as too far for a first-time target). All values in the celebration set still fire celebrations regardless of the chosen target.
- **Ajustes contains only the existing milestone-notification toggle** at the end of this spec. Reminder time, dark mode, language, and "Borrar todo" are intentionally deferred until their behavior is defined.
- **No Notifications Center, no share, no calendar month navigation**: these design ideas are explicitly out of this spec; only the well-defined behaviors above are implemented.
- **Existing widget feature is unaffected** by this spec. The widget continues to render the same data; the new fields (`category`, `goal_milestone_target`) are not surfaced in the widget for now.
- **Time math** continues to use the device's local date as established in the current app; no additional timezone handling is introduced.
