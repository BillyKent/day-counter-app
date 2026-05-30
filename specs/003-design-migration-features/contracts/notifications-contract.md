# Contract: Notifications & Scheduling

**Feature**: `003-design-migration-features` | **Date**: 2026-05-29

WorkManager only (Principle IV). Honor POST_NOTIFICATIONS (API 33+) and the relevant in-app toggle.
No emoji in UI/notification bodies; the milestone push title MAY carry one optional emoji at ≥30 days.

---

## Channels (`DayCounterApplication`)
- `milestone_notifications` (existing, IMPORTANCE_HIGH) — milestone + approaching-milestone.
- `daily_reminder` (NEW, IMPORTANCE_DEFAULT) — daily nudge. Channel strings localized.

## Daily reminder (FR-022..FR-025)
- `DailyReminderScheduler.schedule(enabled, time)`:
  - enabled → enqueue unique (`"daily_reminder"`, `KEEP`/`REPLACE` on time change)
    `OneTimeWorkRequest<DailyReminderWorker>` with `initialDelay = nextOccurrence(time) - now`.
  - disabled → `cancelUniqueWork("daily_reminder")`.
- `DailyReminderWorker.doWork()`:
  1. if toggle off → return success (no post, no reschedule).
  2. post daily reminder notification (localized; "Buenos días" style, references active counters).
  3. re-enqueue self for next day (+24h) → unique `REPLACE`.
- Re-arm via a `BOOT_COMPLETED` BroadcastReceiver (declared `android:exported="true"`, requires
  `RECEIVE_BOOT_COMPLETED`) **and** on app start, both calling `schedule(enabled, time)` (idempotent
  unique work).
- Test (work-testing): next-occurrence delay across midnight; disabled cancels; worker re-enqueues.

## Approaching-milestone (FR-025b, SHOULD)
- Evaluated inside the existing daily refresh worker: for each **ACTIVE** counter, if
  `daysToNextMilestone ∈ thresholds` (e.g., {1,2,3}) and milestone toggle on and not already notified
  for that (counter, milestone), post on `milestone_notifications`; record dedup. Paused excluded.

## Milestone (existing) — unchanged
- Fires on milestone reach; deduped by existing milestone records; **suppressed while paused** (the
  worker reads `status`/effective days, FR-011).

## Permission & gating
- All posting paths first check the OS permission and the matching in-app toggle; if permission
  denied, scheduling persists but no notification is shown (settings reflect permission needed).
