# Specification Quality Checklist: Screens and Navigation Overhaul

**Purpose**: Validate specification completeness and quality before proceeding to planning
**Created**: 2026-05-28
**Feature**: [spec.md](../spec.md)

## Content Quality

- [x] No implementation details (languages, frameworks, APIs)
- [x] Focused on user value and business needs
- [x] Written for non-technical stakeholders
- [x] All mandatory sections completed

## Requirement Completeness

- [x] No [NEEDS CLARIFICATION] markers remain
- [x] Requirements are testable and unambiguous
- [x] Success criteria are measurable
- [x] Success criteria are technology-agnostic (no implementation details)
- [x] All acceptance scenarios are defined
- [x] Edge cases are identified
- [x] Scope is clearly bounded
- [x] Dependencies and assumptions identified

## Feature Readiness

- [x] All functional requirements have clear acceptance criteria
- [x] User scenarios cover primary flows
- [x] Feature meets measurable outcomes defined in Success Criteria
- [x] No implementation details leak into specification

## Notes

- Items marked incomplete require spec updates before `/speckit-plan`.
- After Iteration 3, the spec no longer contains ⚙️ or 📝 markers. Every requirement is well-defined and end-to-end testable.
- Several user-story rationale lines reference the existing app's domain (e.g., the mention of `ResetCounterUseCase` in US3's "Why this priority"). These references are **rationale only** and do not constrain how the spec is implemented; planning is free to refactor.
- Features explicitly **out of scope** in this spec: counter status filter (active/paused/archived), data export, Notifications Center, share button, calendar month navigation, expanded Settings controls (reminder time, dark mode, language, Borrar todo), and Stats metrics with undefined formulas (Hitos alcanzados, Constancia %, weekly bars).

## Validation Iteration Log

### Iteration 1 — 2026-05-28
Initial draft. All checklist items pass on first review.

### Iteration 2 — 2026-05-28 (post `/speckit-clarify`)
Five clarifications resolved and integrated into the spec. Affected sections: Overview/Assumptions (fresh-install scope), US3 + US4 + FR-024 (Revivir celebración semantics), US7 + FR-030 (past-streak retention/pagination), US10 + FR-040 + NotificationLogEntry (90-day retention + pagination), US4 + FR-023 (share payload format). No new ambiguity introduced. Checklist re-evaluated: all items still pass.

### Iteration 3 — 2026-05-28 (scope reduction by user)
At user request, all undefined / pending functionality was removed from the spec so it contains only well-defined behavior:

- **Removed entirely**: Notifications Center (US10), bell icon on Home / Empty state, "Compartir" button on Milestone Celebration, calendar month-navigation arrows, expanded Settings (Hora del recordatorio, Modo oscuro, Idioma, Borrar todo), and the Stats metrics whose formulas were deferred (Hitos alcanzados, Constancia %, weekly bars).
- **Settings tab content reduced** to host only the existing milestone-notification toggle from the current app. No new Settings controls in this spec.
- **Stats reduced** from a 2×2 grid + bar chart to three well-defined metrics: Total acumulado, Mejor racha, Contadores activos.
- **History calendar limited** to the current month (no month navigation).
- **User Story 4 Independent Test** rewritten: replaced the device-clock-advance hack with creating a counter whose `start_date` is exactly 7 days ago, which yields a 7-day streak naturally without test infrastructure tricks.
- **Clarifications carried forward**: Q1 (no migration), Q2 (Revivir celebración semantics), Q3 (past-streak retention) remain. Q4 (notifications log retention) and Q5 (share payload format) were removed since the features they clarified are no longer in scope.
- **Counts**: 10 user stories → 8 stories; 44 functional requirements → 32 FRs; 8 success criteria → 7 SCs; 5 key entities → 3 entities (Counter, MilestoneRecord, PastStreakRecord). NotificationLogEntry removed; UserPreferences expansion removed.

Checklist re-evaluated against the trimmed spec: all items still pass. The spec is now strictly implementable end-to-end without any ⚙️/📝 placeholders.

### Iteration 4 — 2026-05-28 (second `/speckit-clarify` pass on trimmed spec)
Two additional clarifications resolved:

- `MilestoneRecord` lifecycle on Reset: rows are deleted in the same transaction as the reset, so the new attempt can re-achieve and re-celebrate every milestone. Resolves the implicit contradiction between FR-020 (global dedup) and FR-022 ("current attempt" semantics). Updated: FR-017, FR-020, MilestoneRecord entity.
- Midnight rollover while the app is open: screens refresh streak values on `RESUMED`/`STARTED` (standard Android lifecycle); no live clock observer. Updated: Edge Cases section.

Also: a small follow-up edit to FR-021 makes the "catch-up" behavior explicit. When a user comes back after several un-shown milestones accumulated, the most recent one auto-launches and **all** older milestones for that counter are marked `celebration_shown=true` to avoid delayed celebrations on subsequent opens.

Stopped at 2 questions (3 remaining budget unused). Remaining ambiguities (form validation UX, confirmation dialog vs sheet shape, category whitespace handling) are planning-level details — they do not affect architecture, data model, or test design and can be resolved during `/speckit-plan`.

Checklist re-evaluated: all items still pass.
