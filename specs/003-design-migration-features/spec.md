# Feature Specification: Design System Migration + New Features (Pause, Language, Reminders, Stats, Data)

**Feature Branch**: `003-design-migration-features`

**Created**: 2026-05-29

**Status**: Draft

**Input**: User description: "Hice unos diseños usando Claude Design … Es una migración del sistema de diseño y la acomodación de algunos feature, como pausar contadores, o cambiar el idioma. Quiero que analices el contenido de este sistema de diseño y sus pantallas implementadas para agregar la migración al proyecto y la adición de las nuevas feature. Si no puedes obtener el recurso remoto usa la carpeta que está en el directorio de la spec."

> **Design source — RESOLVED.** The remote Claude Design link was inaccessible, so the design
> handoff was delivered into this feature folder and analyzed directly:
> `specs/003-design-migration-features/Day Counter Design System-handoff/day-counter-design-system/project/`.
> Key files read: `README.md` (brand guide), `colors_and_type.css` (tokens),
> `ui_kits/android_app/` (the 10 implemented screens + `components.jsx` model helpers + `widgets.html`),
> and the `screenshots/` set. The visual requirements below are now backed by concrete tokens and
> screen behavior. See **Design Reference** at the end of this spec.

## Design Reference *(brand + tokens, from the handoff)*

**Brand direction** (two founding constraints): colors that inspire *confianza y tranquilidad*
(no neon, no vibrant gradients) and *curved shapes* for warmth (generous radii, pill buttons,
circular progress rings). The result should feel closer to a wellbeing app than a productivity app.

- **Palette**: brand teal `#0F5F6E` (hover `#0B4E5B`, press `#08404B`, soft `#D8EAEC`); warm cream
  app background `#FBF6EE`; white cards `#FFFFFF`; sunken sections `#F4EDDF`. Ink `#1B2A33` →
  `#8B9AA3`. Semantic (all warm): success/sage `#6FA88B` (streak growing), milestone/terracotta
  `#D9876A` (milestone reached), warning `#D9A05B`, danger `#C97062`. Gradients only teal→teal-light
  (`#0F5F6E → #2A8597`), used sparingly on rings and milestone headers.
- **Typography**: **Outfit** for display + the large streak numeral (the "hero", tabular, weight 600,
  up to 96px); **Plus Jakarta Sans** for body/labels. Both humanist-geometric, good Spanish glyph
  coverage. (Web kit loads them from Google Fonts; the production app bundles equivalents.)
- **Shape/radii**: cards 24px (squircle), buttons 999px (pill), inputs/chips 16px, sheets 32px top.
  Nothing at 0 radius except full-bleed elements.
- **Spacing**: scale of 4 (`4,8,12,16,20,24,32,40,48,64`); 20px lateral screen padding; 12px gap
  between cards.
- **Elevation**: teal-tinted shadows (not black), `sm/md/lg/xl`. Cards use white fill + `shadow-md`,
  no border. Milestone ring gets a soft terracotta glow.
- **Motion**: dominant easing `cubic-bezier(0.32,0.72,0,1)`; durations 180ms (micro), 320ms (sheets /
  screen transitions / ring fill), 600ms (milestone celebration with a small bounce). Press = `scale
  0.97`. Sheets use a teal backdrop `rgba(15,95,110,0.25)` with blur; pinned headers blur on scroll.
- **Iconography**: rounded line icons, 24px, stroke 2 (handoff uses Lucide rounded as a stand-in:
  `flame, plus, pencil, rotate-ccw, trash-2, bell, bar-chart-3, settings, chevron, check, x, calendar,
  target, trophy, clock, pause, play, globe, moon`). **No emoji in UI**; at most 1 optional emoji in a
  large-milestone push notification.
- **Voice/content**: Spanish, sentence case, informal "tú", no emoji. Milestone messages at
  1/7/30/100/365/1000 days are fixed (already in the app's Spanish strings). Metadata separator `·`.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Refreshed visual design across the whole app (Priority: P1)

When I open any screen, I see the new Day Counter design — warm cream surfaces, deep-teal brand,
rounded squircle cards, pill buttons, the big tabular streak numeral, and rounded line icons —
applied consistently everywhere, in both light and dark, while every existing feature still works.

**Why this priority**: The migration is the foundation every other story inherits; it is
cross-cutting and must land first so new surfaces are built in the new language.

**Independent Test**: Walk every surface (the three tabs — Contadores, Estadísticas, Ajustes — plus
Counter Detail, History/Calendar, Create/Edit/Reset sheets, Milestone Celebration, Onboarding, Empty
state, and the home-screen Widget) in light and dark and confirm each matches the handoff tokens and
component styling with no behavioral or accessibility regression.

**Acceptance Scenarios**:

1. **Given** the app is installed, **When** I open the Contadores tab, **Then** the summary cards,
   filter chips, list cards, and the streak numeral use the new palette, type, radii, and shadows.
2. **Given** I visit every in-scope surface, **When** I inspect each, **Then** all match the design
   tokens (cream `#FBF6EE` background, white 24px cards, teal `#0F5F6E` brand, pill buttons) — no
   screen remains in the prior Material-3 mint styling.
3. **Given** dark mode, **When** I browse, **Then** a dark variant of the palette is applied with
   legible contrast on every surface.
4. **Given** any previously-supported action (create, edit, reset, delete, view history, celebrate),
   **When** I perform it, **Then** it behaves exactly as before the migration.
5. **Given** TalkBack is on, **When** I traverse each migrated screen, **Then** every control has a
   meaningful label and meaning is never conveyed by color alone (e.g., paused, milestone reached).

---

### User Story 2 - Pause and resume a counter (Priority: P2)

I can pause a counter so its streak freezes, and resume it later continuing from the same day —
paused time is never counted and is never lost. The Contadores list lets me filter Todos / Activos /
Pausados, and paused counters are clearly marked.

**Why this priority**: Pausing protects accumulated progress during planned breaks without forcing a
reset; it is the headline new feature and is self-contained.

**Independent Test**: Pause an active counter (it shows a frozen dashed ring + "En pausa" banner and
stops counting); after days pass, resume and confirm it continues from the frozen day with paused
days excluded, no milestone fired while paused, and history unchanged. Filter the list by Pausados
and confirm only paused counters show.

**Acceptance Scenarios**:

1. **Given** an active counter on its detail screen, **When** I tap "Pausar contador", **Then** it
   becomes paused: the ring renders frozen/dashed in a muted style, the numeral shows "En pausa", and
   a banner reads "Contador en pausa · Sin notificaciones · N días en pausa".
2. **Given** a paused counter, **When** days pass, **Then** its displayed day count does not increase.
3. **Given** a paused counter, **When** I tap "Reanudar contador", **Then** it continues from the
   frozen day value (paused interval excluded) and is no longer marked paused.
4. **Given** a counter paused on 10 Jan that started 1 Jan and resumed 20 Jan, **When** I view it on
   resume, **Then** it reads 9 days (the worked-example from the handoff).
5. **Given** a paused counter, **When** a date that would be a milestone passes during the pause,
   **Then** no milestone notification or celebration fires; milestones evaluate against the frozen
   day count.
6. **Given** the Contadores tab, **When** I tap the Todos / Activos / Pausados chips, **Then** the
   list filters live and each chip shows a live count; an appropriate empty message appears when a
   filter has no counters.
7. **Given** any counter, **When** I pause or resume it, **Then** no past streak is archived to
   History and no milestone records are deleted (pause ≠ reset ≠ delete).
8. **Given** a paused counter, **When** I cold-restart the app, **Then** it is still paused with the
   same frozen day count and accumulated paused total.
9. **Given** paused counters exist, **When** I view totals on Contadores and Estadísticas, **Then**
   their effective (paused-excluded) days are used consistently so the two tabs never disagree, and
   paused counters are excluded from the "activos" count.

---

### User Story 3 - Change the app language in-app (Priority: P2)

From Ajustes I can open "Idioma" and pick the app language from a list; the whole app switches to my
choice, and my choice is remembered across restarts.

**Why this priority**: Direct language control is a core part of the new Settings design and a
frequent request; it ranks alongside pause.

**Independent Test**: Open Ajustes → Idioma, choose a language, confirm the UI switches and the
choice is checked; restart and confirm it persists; date/number formatting follows the selection.

**Acceptance Scenarios**:

1. **Given** Ajustes, **When** I open Idioma, **Then** I see the supported languages as a radio list
   showing each language's native name (and a secondary label), with the current one checked.
2. **Given** the Idioma sheet, **When** I select a language, **Then** the sheet closes and all
   user-facing text switches to that language.
3. **Given** a selected language, **When** I cold-restart, **Then** the app starts in that language.
4. **Given** a selected language, **When** dates and numbers are shown, **Then** they are formatted
   per that language.
5. **Given** a language not yet fully translated, **When** a string is missing, **Then** it falls
   back to the base language rather than showing a blank or key.

> Resolved by **Q1**: the picker offers **English + Spanish only**, default **English** (English
> fallback). The handoff's other four locales are deferred; no constitution change is needed.

---

### User Story 4 - Daily reminder at a chosen time (Priority: P3)

I can turn on a daily reminder and pick the time of day it arrives (with quick Mañana / Mediodía /
Noche presets), so I get a gentle nudge to keep my streak.

**Why this priority**: A new, motivational notification type from the design; valuable but additive
and independent of the redesign.

**Independent Test**: Enable "Recordatorios diarios", open "Hora del recordatorio", set a time (or a
preset), save; confirm the settings row reflects the time and a reminder notification is delivered at
that time daily; disabling the toggle removes the reminder.

**Acceptance Scenarios**:

1. **Given** Ajustes → Notificaciones, **When** I toggle "Recordatorios diarios" on, **Then** the
   "Hora del recordatorio" row becomes active and shows the scheduled time.
2. **Given** the daily reminder is on, **When** I open "Hora del recordatorio", **Then** I can choose
   an hour and minute (minutes in 5-minute steps) or tap a preset (Mañana 08:00, Mediodía 13:00,
   Noche 21:00) and save.
3. **Given** a saved reminder time, **When** that time arrives on a day, **Then** a daily reminder
   notification is delivered.
4. **Given** the daily reminder is off, **When** time passes, **Then** no daily reminder is delivered
   and the time row shows it as disabled.
5. **Given** a reminder time and toggle state, **When** I cold-restart (or the device reboots),
   **Then** the schedule persists.
6. **Given** milestone notifications are a separate toggle, **When** I change one toggle, **Then** the
   other is unaffected (daily reminder and milestone notifications are independent).

---

### User Story 5 - Expanded statistics (Priority: P3)

The Estadísticas tab shows my total accumulated effective days, best streak, milestones reached,
active counters, average streak, a dedicated Pausas card, and a weekly activity view.

**Why this priority**: Richer insight from the design; depends on pause (for the Pausas card and
effective-day math) but is otherwise additive.

**Independent Test**: With several counters (some paused), open Estadísticas and verify each metric
matches the underlying data and that paused time is excluded from day totals.

**Acceptance Scenarios**:

1. **Given** counters exist, **When** I open Estadísticas, **Then** I see "Total acumulado" in
   effective (paused-excluded) days as the hero figure, with a note that paused time isn't counted.
2. **Given** counters exist, **When** I view the metric grid, **Then** I see Mejor racha, Hitos
   (count reached across counters), Contadores activos, and Racha media.
3. **Given** at least one paused counter, **When** I view the Pausas card, **Then** it shows "en pausa
   ahora", "días pausados", and "pausas totales".
4. **Given** a week of activity, **When** I view "Esta semana", **Then** a 7-bar weekly chart shows
   days fulfilled with today emphasized and a weekly total.
5. **Given** no counters, **When** I open Estadísticas, **Then** an empty state invites me to create
   one.

---

### User Story 6 - Erase all data ("Borrar todo") with undo (Priority: P3)

From Ajustes → Datos I can erase all counters and history after a clear confirmation, with a brief
undo option in case I change my mind.

**Why this priority**: A safety/reset utility shown in the design; useful but rarely used.

**Independent Test**: Tap "Borrar todo", confirm in the warning sheet, see all data cleared and a
toast with "Deshacer"; tapping Deshacer within the window restores everything; letting it dismiss
makes the erase permanent.

**Acceptance Scenarios**:

1. **Given** Ajustes → Datos, **When** I tap "Borrar todo", **Then** a destructive confirmation sheet
   appears stating how many counters and their history will be removed and that it can't be undone
   after the window.
2. **Given** the confirmation, **When** I confirm, **Then** all counters, history, milestones, and
   past streaks are erased and a toast "Todos los datos fueron eliminados" with "Deshacer" appears.
3. **Given** the toast is visible, **When** I tap "Deshacer", **Then** all erased data is restored.
4. **Given** the toast, **When** it dismisses without Deshacer, **Then** the erase is permanent.
5. **Given** I cancel the confirmation sheet, **When** it closes, **Then** no data is changed.

---

### User Story 7 - Appearance control (dark mode) (Priority: P4)

From Ajustes → Apariencia I can control dark mode (follow the system, or force on/off), and the new
design's dark palette is applied accordingly.

**Why this priority**: Polish; the app already supports system dark mode, so an explicit control is
a small enhancement.

**Independent Test**: Toggle the appearance control and confirm the app switches between the design's
light and dark palettes and that the choice persists.

**Acceptance Scenarios**:

1. **Given** Ajustes → Apariencia, **When** I view the dark-mode row, **Then** I can set it to follow
   the system (default) or override it.
2. **Given** I set dark mode on, **When** I browse the app, **Then** the design's dark palette is
   applied everywhere with legible contrast.
3. **Given** an appearance choice, **When** I cold-restart, **Then** it persists.

---

### Edge Cases

- **Pause on day zero**: pausing the day a counter starts (0 days) then resuming continues a 0-day
  streak normally.
- **Milestone during a pause window**: on resume, the next milestone is computed from the frozen day
  count, so a milestone is reached on the day the effective count hits it, not skipped.
- **Repeated pause/resume**: multiple paused intervals accumulate; "días pausados" and "pausas
  totales" reflect the sum/count.
- **Edit/Delete a paused counter**: editing name/category/goal is allowed and keeps it paused;
  deleting removes it like any counter.
- **Widget for a paused counter**: shows the frozen value and indicates the paused state.
- **Language with an untranslated string**: falls back to the base language; never shows a raw key
  or blank.
- **Language change with a sheet/overlay open**: applies without losing the user's place or data.
- **Reminder time across reboot / timezone or DST change**: the daily reminder re-arms for the chosen
  local time.
- **Reminder when notifications are OS-denied**: the app reflects that permission is required and the
  toggle/time have no effect until granted.
- **"Borrar todo" while a counter is paused or mid-sheet**: still erases all data; undo restores the
  full prior state including paused status.
- **Weekly bars with no activity / brand-new install**: shows zeros without error.
- **Empty filter result**: Pausados/Activos with no matches shows the matching empty message, not a
  blank list.

## Requirements *(mandatory)*

### Functional Requirements

**Design System Migration**

- **FR-001**: The app MUST adopt the handoff design tokens — palette (teal `#0F5F6E` brand, cream
  `#FBF6EE` background, white cards, sage/terracotta/warning/danger semantics), the Outfit + Plus
  Jakarta Sans type pairing with the large tabular streak numeral, radii (cards 24, pill buttons,
  16 inputs/chips, 32 sheets), the 4-based spacing scale with 20px lateral padding, teal-tinted
  elevation, and the defined motion/easing — across all surfaces.
- **FR-002**: The design MUST be applied consistently to every in-scope surface: Contadores,
  Estadísticas, Ajustes, Counter Detail, History/Calendar, Create/Edit/Reset sheets, Milestone
  Celebration, Onboarding, Empty state, and the home-screen Widget. No surface may remain in the
  prior style.
- **FR-003**: The app MUST provide both light and dark appearances under the new palette with legible
  contrast on every surface.
- **FR-004**: Iconography MUST use a rounded line-icon style consistent with the handoff set; **no
  emoji** appears in the UI (at most one optional emoji in a large-milestone push notification).
- **FR-005**: The migration MUST NOT regress accessibility — minimum touch-target sizing, sufficient
  contrast, and screen-reader labels MUST be maintained or improved, and meaning MUST NOT be conveyed
  by color alone.
- **FR-006**: The migration MUST NOT change the behavior, inputs, outputs, or navigation of any
  existing feature; all previously-passing acceptance scenarios MUST still pass.
- **FR-006a**: The Milestone Celebration screen MUST follow the handoff layout (full-screen animated
  ring, fixed per-milestone copy, "Seguir así" primary) and MUST add a **"Compartir"** (share)
  action that lets the user share the milestone.
- **FR-006b**: The home-screen Widget MUST be re-skinned to the new design and support the handoff's
  widget layouts (a featured-streak banner, a single-counter view with a 7-day mini-bar week, and a
  multi-counter list), updating at least daily.

**Pause / Resume + Filtering**

- **FR-007**: Users MUST be able to pause an active counter and resume a paused counter from the
  Counter Detail screen.
- **FR-008**: A counter's effective day count MUST equal elapsed time minus every paused interval,
  with the count frozen while paused; the day count is derived, never stored, so all tabs agree.
- **FR-009**: On resume, the streak MUST continue from the frozen value (paused interval excluded); a
  pause MUST never reduce nor advance the streak.
- **FR-010**: A paused counter MUST be clearly identified as paused on the list and detail (frozen/
  dashed muted ring, "En pausa" label, and a banner indicating no notifications and the paused-day
  total).
- **FR-011**: While paused, a counter MUST NOT generate milestone notifications or milestone
  celebrations.
- **FR-012**: Pausing/resuming MUST NOT archive a past streak, delete milestone records, or otherwise
  alter History — pause is distinct from Reset and Delete.
- **FR-013**: A counter's status (active/paused), its completed paused intervals, and the current
  paused-since point MUST persist across app restarts.
- **FR-014**: The Contadores list MUST offer Todos / Activos / Pausados filters that filter the list
  live, each showing a live count, with a filter-appropriate empty state.
- **FR-015**: Paused counters MUST be excluded from the "active counters" metric; their effective
  days still count toward total-accumulated metrics.

**In-App Language**

- **FR-016**: Users MUST be able to change the app's display language from Ajustes → Idioma via a
  radio list presenting each language's native name.
- **FR-017**: Changing the language MUST update all user-facing text without reinstalling the app.
- **FR-018**: The selected language MUST persist across app restarts.
- **FR-019**: Locale-sensitive formatting (dates, numbers per the handoff number rules) MUST follow
  the selected language.
- **FR-020**: Any string missing in the selected language MUST fall back to the base language.
- **FR-021**: The supported language set is **{English, Spanish}** with **English as the default and
  fallback** (per Q1). The picker shows only these two for now; the handoff's other four locales
  (Português, Français, Deutsch, Italiano) are explicitly **deferred to a future feature**. The
  English-default keeps the current constitution (v2.2.0) unchanged — no amendment required.

**Daily Reminder**

- **FR-022**: Users MUST be able to enable/disable a daily reminder, independently of milestone
  notifications.
- **FR-023**: Users MUST be able to set the daily reminder time (hour + minute in 5-minute steps) and
  use Mañana (08:00) / Mediodía (13:00) / Noche (21:00) presets.
- **FR-024**: When enabled, a daily reminder notification MUST be delivered at the chosen local time
  each day; when disabled, none is delivered.
- **FR-025**: The reminder enabled-state and time MUST persist across app restarts and re-arm after
  device reboot, honoring the OS notification-permission state.
- **FR-025a**: Notification copy MUST be specific and warm (never generic), localized to the selected
  language, and follow the no-emoji-in-UI rule (the milestone push title MAY include one optional
  emoji at ≥30 days). The handoff defines three notification types: milestone reached, daily reminder
  ("Buenos días …"), and an **approaching-milestone** nudge ("Faltan N días para tu hito de M").
- **FR-025b**: The app SHOULD deliver an approaching-milestone notification shortly before a counter
  reaches a milestone (subject to the milestone-notifications toggle); paused counters MUST be
  excluded.

**Expanded Statistics**

- **FR-026**: Estadísticas MUST show, in effective (paused-excluded) days: Total acumulado (hero),
  Mejor racha, Hitos alcanzados (count across counters), Contadores activos, and Racha media.
- **FR-027**: Estadísticas MUST include a Pausas card showing "en pausa ahora", "días pausados", and
  "pausas totales".
- **FR-028**: Estadísticas MUST include a weekly activity view (7 bars) with today emphasized and a
  weekly total of days fulfilled.
- **FR-029**: Estadísticas MUST show an empty state when there are no counters.

**Erase All Data**

- **FR-030**: Users MUST be able to erase all data ("Borrar todo") from Ajustes → Datos behind a
  destructive confirmation that states the scope of deletion.
- **FR-031**: Confirming MUST erase all counters, history, milestones, and past streaks and surface a
  confirmation with a time-bounded "Deshacer" (undo) that fully restores the prior state if invoked;
  once the undo window passes, the erase is permanent.

**Appearance**

- **FR-032**: Users MUST be able to control dark mode (follow system by default, or override), with
  the design's light/dark palette applied accordingly and the choice persisted.

### Key Entities *(include if feature involves data)*

- **Counter (extended)**: existing goal name, start date, category, goal target, created-at — plus a
  **status** (active/paused), the set of **completed paused intervals**, and the **paused-since**
  point used to freeze the count. Effective day count is derived (elapsed − paused time).
- **Language Preference**: the user's chosen app language, persisted and applied at start and on
  change (set/default per Q1).
- **Reminder Setting**: whether the daily reminder is enabled and the time-of-day it fires.
- **Appearance Preference**: dark-mode mode (follow system / on / off), persisted.
- **Pause Metrics (derived)**: counters paused now, total paused days, total pauses — computed from
  Counters' paused intervals for the Stats Pausas card.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of in-scope surfaces reflect the new design tokens in both light and dark,
  verified against a per-screen visual audit checklist keyed to the handoff.
- **SC-002**: A user can pause or resume a counter in at most 2 taps from its detail screen, and the
  paused state is visually distinct (verified with TalkBack: announced as paused, not color-only).
- **SC-003**: A paused counter's day count does not change across ≥24h paused (including a date
  rollover); the worked example (1 Jan start, pause 10 Jan, resume 20 Jan ⇒ 9 days) holds exactly.
- **SC-004**: After selecting a language, all visible UI is shown in that language within 2 seconds
  (or immediately on the next screen).
- **SC-005**: Language, paused state, reminder schedule, and appearance choice each survive a cold
  restart 100% of the time; the reminder re-arms after reboot.
- **SC-006**: No accessibility regression — every migrated screen meets the project's contrast and
  touch-target guidelines, confirmed by automated checks plus a TalkBack pass.
- **SC-007**: 100% of the prior feature's acceptance scenarios continue to pass after migration.
- **SC-008**: Contadores and Estadísticas never disagree on day totals (effective, paused-excluded
  days are the single source), checked across active and paused mixes.
- **SC-009**: A daily reminder fires within 1 minute of the chosen local time on the day it is
  scheduled, and never when disabled.
- **SC-010**: "Borrar todo" followed by "Deshacer" restores the exact prior data set 100% of the
  time within the undo window.

## Assumptions

- **Scope**: This feature brings the app in line with the entire delivered design, including the
  features surfaced only by the screens (filter chips, daily-reminder time, expanded Stats, erase-all,
  appearance), not only the two the prompt named. User-story priorities let planning phase the work.
- **Pause semantics** (confirmed 2026-05-29 and corroborated by the handoff): paused days are excluded
  ("freeze & exclude"); the count freezes on pause and resumes from the same value.
- **Milestone set / goal targets**: unchanged from the current app — celebrations at 1/7/30/100/365/
  1000 days; selectable goal targets unchanged. The handoff's milestone messaging matches existing
  Spanish copy.
- **Weekly activity ("Esta semana")**: a derived 7-day view of days fulfilled across counters
  (active, paused excluded); exact per-day definition to be finalized in planning. Treated as a
  read-only insight, not new tracked data.
- **Daily reminder content**: a single motivational daily nudge (not per-counter), in the selected
  language, with no emoji in-app; large-milestone push may optionally include one emoji.
- **Data posture**: consistent with prior iterations, no production data must be preserved;
  schema/preference additions (paused intervals, language, reminder, appearance) may be made without a
  written data migration.
- **Erase-all undo**: implemented as a short-lived restore window (e.g., a few seconds while the toast
  shows); after it elapses the deletion is final.
- **Offline**: the app remains fully offline; nothing here requires network access. Fonts/icons are
  bundled in the production app even though the web kit references CDNs.
- **Voice**: in-app copy stays Spanish-first per the brand brief (informal "tú", no emoji); other
  languages are translations of the same copy.
- **Category input**: the handoff Create/Edit sheets present **category as a fixed chip set** (Salud,
  Ejercicio, Ahorro, Estudio, Mente) rather than the current free-text field. This spec assumes the
  app moves to a predefined category chip set (final list confirmable in planning); existing
  free-text categories degrade gracefully.
- **Notifications scope**: three notification types are in scope (milestone, daily reminder,
  approaching-milestone), all gated by their respective toggles and the OS permission, and all
  suppressed for paused counters.
- **Constitution**: Q1 chose English-default with an English+Spanish picker, which **keeps the
  constitution (v2.2.0) unchanged** — no amendment needed. The handoff's Spanish-first voice still
  applies to the Spanish copy; English remains the base/default locale.

## Clarifications

### Session 2026-05-29

- **Q1 — Language set & default** → **Resolved: English + Spanish only, English default/fallback.**
  The in-app picker offers just English and Spanish for now; the handoff's other four locales
  (Português, Français, Deutsch, Italiano) are **deferred to a future feature**. English stays the
  default and fallback, so the constitution's English-default-locale rule (v2.2.0, lines 247–248) is
  satisfied with **no amendment**. Reflected in FR-021 and US3.
- **Q2 — Pause semantics** → **Resolved: Freeze & exclude** (count freezes while paused, resumes from
  the same value, paused time excluded). Confirmed by the handoff worked example.
