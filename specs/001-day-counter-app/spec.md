# Feature Specification: Day Counter — Streak Habit Tracker

**Feature Branch**: `001-day-counter-app`

**Created**: 2026-05-27

**Status**: Draft

**Input**: Full application specification for the Day Counter Android app

---

## Clarifications

### Session 2026-05-27

- Q: Should the app's local data be included in Android's automatic device backup? → A: Yes — streak history MUST be preserved via Android auto-backup so data survives reinstalls and device transfers.
- Q: How many widget sizes should the app support? → A: Two sizes — compact 2×1 (streak count only) and medium 4×2 (goal name + streak count).
- Q: What does the user see on first launch when no counters exist? → A: A 2–3 screen onboarding walkthrough (shown once only) explaining the concept, followed by the home screen with a clear call-to-action to create the first counter.
- Q: Should milestone notifications be delivered if the device was off at midnight? → A: Yes — deliver on next device wake with no expiry; the user always receives the congratulation regardless of when the device was on.
- Q: How should counters be ordered on the home screen? → A: Longest streak first — the counter with the most elapsed days appears at the top; order updates automatically each day.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Create a Personal Streak Counter (Priority: P1)

A user who wants to track a new personal goal (e.g., "quit smoking") opens the app for the
first time, taps a button to add a new counter, provides a name for the goal and a start
date, and immediately sees the counter displaying the number of days elapsed since that date.

**Why this priority**: This is the core value proposition of the app. Without the ability to
create a counter, nothing else is usable. Every other story builds on top of this.

**Independent Test**: Launch the app, create one counter named "No alcohol" with a start date
7 days in the past. The home screen MUST display "No alcohol — 7 days" (or equivalent). The
counter persists after closing and reopening the app.

**Acceptance Scenarios**:

1. **Given** the app is freshly installed and opened, **When** it launches for the first
   time, **Then** a 2–3 screen onboarding walkthrough is displayed before the home screen.
2. **Given** the onboarding walkthrough is complete (or skipped), **When** the home screen
   appears with no counters, **Then** a prominent "Add your first counter" call-to-action is
   displayed so the user can immediately create their first streak.
3. **Given** the app has been previously opened (onboarding already shown), **When** the
   user opens the app again, **Then** the onboarding is NOT shown again.
4. **Given** the home screen is shown, **When** the user taps "Add Counter",
   **Then** a creation form appears requesting a goal name and start date.
5. **Given** the creation form is open, **When** the user enters a name and selects a start
   date in the past, **Then** the counter is saved and the home screen shows the elapsed day
   count immediately.
6. **Given** a counter exists, **When** the app is closed and reopened the next day, **Then**
   the counter's day count has incremented by one without any user action.
7. **Given** the creation form is open, **When** the user submits it with an empty name,
   **Then** a validation error is shown and no counter is saved.
8. **Given** the creation form is open, **When** the user selects a future start date,
   **Then** a validation error is shown, because a streak cannot begin in the future.

---

### User Story 2 — Manage Existing Counters (Priority: P2)

A user can edit the name or start date of an existing counter, reset a counter back to day
zero (when a streak is broken), or permanently delete a counter they no longer need.

**Why this priority**: Users make mistakes and life circumstances change. Without management
actions the app becomes cluttered with stale data, reducing trust and usefulness.

**Independent Test**: With at least one counter on screen, edit its name and verify the
updated name appears. Reset it and verify the day count shows 0 (or 1 if reset counts the
current day as day 1). Delete it and verify it disappears from the list.

**Acceptance Scenarios**:

1. **Given** a counter exists, **When** the user selects "Edit" and changes the goal name,
   **Then** the updated name is reflected on the home screen immediately and persisted across
   app restarts.
2. **Given** a counter exists, **When** the user selects "Edit" and changes the start date,
   **Then** the day count recalculates correctly based on the new date.
3. **Given** a counter exists, **When** the user selects "Reset", **Then** a confirmation
   dialog appears before the reset is applied, and after confirming, the counter's start date
   is set to today and the streak count returns to 0.
4. **Given** a counter exists, **When** the user selects "Delete", **Then** a confirmation
   dialog appears, and after confirming, the counter is permanently removed from the list.
5. **Given** multiple counters exist, **When** one counter is deleted, **Then** the remaining
   counters are unaffected.

---

### User Story 3 — Home Screen Widget (Priority: P3)

A user can add one or more Day Counter widgets to their Android home screen. The app offers
two widget sizes: a compact 2×1 showing the streak count only, and a medium 4×2 showing
the goal name and streak count. Each widget instance is linked to one counter and updates
daily without requiring the user to open the app.

**Why this priority**: Widgets provide passive motivation — seeing progress without friction
reinforces the habit. However, the core app must be stable first; widget support is additive.

**Independent Test**: Add the widget to the home screen, map it to an existing counter. The
widget shows the correct streak count. Do not open the app the next day; verify the widget
count has incremented.

**Acceptance Scenarios**:

1. **Given** the app has at least one counter, **When** the user long-presses the home screen
   and adds a Day Counter widget, **Then** the system shows a picker to select which counter
   the widget represents.
2. **Given** a widget is placed, **When** the day changes at midnight, **Then** the widget
   updates its displayed count without the user opening the app.
3. **Given** a widget is placed, **When** the underlying counter is deleted via the app,
   **Then** the widget shows a graceful "Counter removed" state rather than crashing or
   displaying stale data.
4. **Given** a widget is placed, **When** the user taps the widget, **Then** the app opens
   directly to the detail view of the associated counter.

---

### User Story 4 — Milestone Notifications (Priority: P4)

The app sends a push notification when a user's streak reaches a notable milestone (e.g.,
7 days, 30 days, 100 days), congratulating the user and encouraging continued commitment.

**Why this priority**: Notifications are motivational reinforcement. They require notification
permission and are a value-add after the core tracking and widget features are stable.

**Independent Test**: Create a counter with a start date that makes its current count exactly
6 days. Advance the device date by 1 day (to day 7). A notification appears within a
reasonable time (under 1 hour of the day change) with a congratulatory message referencing
the counter name and the 7-day milestone.

**Acceptance Scenarios**:

1. **Given** a counter's streak reaches a milestone day (7, 30, 60, 90, 180, 365), **When**
   the day changes, **Then** a notification is delivered within 1 hour congratulating the
   user by counter name and milestone. If the device was off at midnight, the notification
   MUST be delivered as soon as the device next powers on (no expiry).
2. **Given** the user has denied notification permission **or** has disabled notifications
   in the app settings, **When** a milestone is reached, **Then** no notification is sent
   and the app does not crash or show an error.
3. **Given** a notification is delivered, **When** the user taps it, **Then** the app opens
   to the counter that reached the milestone.
4. **Given** a counter is reset before its milestone day, **When** the milestone day would
   have been reached, **Then** no notification is sent for the old streak.
5. **Given** the user disables motivational notifications in the app settings, **When** any
   counter reaches a milestone, **Then** no notification is sent until the user re-enables
   the setting.

---

### Edge Cases

- What happens if the user dismisses the onboarding mid-way (taps skip)? The app MUST
  proceed to the home screen immediately; the onboarding MUST NOT be shown again.
- What happens when the device's date/time is changed manually by the user? The streak count
  MUST be based on the stored start date and the current system date; no local caching of
  "last seen day" that could be manipulated to inflate the streak.
- What happens if the user has 50 or more counters? The home screen list MUST remain
  performant (smooth scrolling, no jank) regardless of counter count.
- What happens when a counter's start date is today? The streak MUST display 0 days
  (counter just started) until the following calendar day.
- What happens when two counters reach a milestone on the same day? Both send individual
  notifications; they MUST NOT be merged silently or dropped.
- What happens when two counters have the same streak count? Their relative order within
  that tie MUST be deterministic (e.g., sorted by creation date as a secondary criterion)
  to prevent the list from flickering or reordering randomly on each render.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-000**: On first launch, the app MUST display a 2–3 screen onboarding walkthrough
  that explains the core concept (tracking streak days for personal goals). The walkthrough
  MUST include a skip option on every screen. It MUST be shown exactly once; subsequent
  launches MUST go directly to the home screen.
- **FR-001**: Users MUST be able to create a streak counter by providing a goal name
  (required, 1–100 characters) and a start date (must not be in the future). If the user
  does not explicitly select a start date, the current date MUST be used as the default.
- **FR-002**: The app MUST automatically calculate and display the number of full calendar
  days elapsed from the start date to the current date for each counter.
- **FR-003**: Users MUST be able to edit the name and start date of any existing counter.
- **FR-004**: Users MUST be able to reset any counter; reset sets the start date to the
  current day and brings the streak to 0. A confirmation prompt MUST precede the reset.
- **FR-005**: Users MUST be able to permanently delete any counter. A confirmation prompt
  MUST precede the deletion.
- **FR-006**: All counters MUST be persisted locally on the device so they survive app
  restarts and device reboots without requiring an internet connection.
- **FR-007**: Streak counts MUST increment automatically at the start of each new calendar
  day without any user interaction.
- **FR-008**: The app MUST offer two home screen widget sizes per counter:
  (a) Compact (2×1 cells): displays the current streak count only.
  (b) Medium (4×2 cells): displays the goal name and the current streak count.
  Both sizes MUST allow the user to select which counter they represent at placement time.
- **FR-009**: Widgets MUST refresh their displayed count at least once per calendar day,
  independently of whether the app is open.
- **FR-010**: The app MUST send a local notification when a counter reaches the following
  streak milestones: 7, 30, 60, 90, 180, and 365 days. If the device was powered off when
  the notification was scheduled, it MUST be delivered upon the next device power-on with no
  expiry window.
- **FR-011**: Notifications MUST include the counter's goal name and the milestone reached.
  Tapping the notification MUST open the app to the relevant counter.
- **FR-012**: The app MUST request notification permission from the user on first relevant
  interaction; if denied, the app MUST continue to function without notifications.
- **FR-017**: The app MUST provide an in-app settings option to enable or disable
  motivational notifications. When disabled by the user, no milestone notification MUST be
  sent regardless of the OS-level notification permission status.
- **FR-018**: If a counter cannot be saved or updated due to a storage error, the app MUST
  display an error message informing the user that the operation failed. No partial or
  corrupted counter state MUST be persisted.
- **FR-013**: The home screen counter list MUST support at least 50 simultaneous counters
  without degraded scrolling performance.
- **FR-016**: Counters on the home screen MUST be sorted by streak length in descending order
  (longest streak at the top). The sort order MUST update automatically each day without
  user interaction. No manual reordering is supported in this version.
- **FR-014**: The app MUST function fully offline; no network access is required for any
  core feature.
- **FR-015**: The app's local data (counters, milestone notification history, widget bindings)
  MUST be included in Android's automatic device backup. After reinstalling the app or
  transferring to a new Android device, all previously created counters and their start dates
  MUST be restored automatically, provided the device's backup service was active.

### Key Entities

- **Counter**: Represents one personal goal or habit. Key attributes: unique identifier,
  goal name, start date, creation timestamp. The current streak (day count) is always
  derived from the start date and the current date — it is not stored.
- **Milestone**: A predefined day count (7, 30, 60, 100, 365) at which a notification is
  triggered. Tracks whether a notification has already been sent for a given counter +
  milestone combination to avoid duplicates.
- **Widget Binding**: Associates a home screen widget instance (identified by its system
  widget ID) with a specific Counter, so the widget knows which data to display.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A new user can add their first streak counter in under 60 seconds from first
  app launch, with no prior instructions.
- **SC-002**: The home screen list loads and displays all counters in under 1 second, even
  when 50 or more counters are present.
- **SC-003**: 100% of existing counters retain their correct start dates and streak counts
  after the app is closed and the device is rebooted.
- **SC-004**: Widgets display the correct streak count for their associated counter at all
  times, and the count reflects the current calendar day within 1 hour of midnight.
- **SC-005**: Milestone notifications are delivered within 1 hour of the day the milestone
  is reached for every active counter that hits a milestone, as long as notification permission
  has been granted. If the device was off at the scheduled delivery time, the notification
  MUST be delivered as soon as the device next powers on, with no expiry window.
- **SC-006**: No duplicate milestone notification is ever sent for the same counter and
  milestone combination.
- **SC-007**: The app achieves a cold-start time of under 2 seconds on a mid-range device.
- **SC-008**: A user who reinstalls the app or transfers to a new device finds all previously
  created counters with their correct start dates intact, with no manual data-entry required,
  provided the device's automatic backup was enabled.

---

## Assumptions

- The app is single-user; there is no account system or manual cloud sync. All data is stored
  locally on the device. Android's built-in automatic backup IS supported: streak data is
  preserved when the user reinstalls the app or transfers to a new device (see FR-015).
- Streak counts are based on full calendar days in the device's local time zone. No cross-
  timezone correction is applied when the user travels.
- A "day" boundary is defined as midnight in the device's local time zone (i.e., when the
  date changes from day N to day N+1, the streak increments).
- The initial milestone set (7, 30, 60, 90, 180, 365) is fixed for this version. Users
  cannot define custom milestones.
- Each widget instance displays exactly one counter. Multiple widget instances can be placed,
  each configured independently.
- The app targets Android 8.0 (API 26) and above, covering approximately 97% of active
  Android devices.
- Dark mode and light mode are both supported from the first release.
- No social sharing, manual backup/export, or cloud sync features are in scope for this
  version. Automatic device backup (OS-level) is supported but not a user-visible feature.
