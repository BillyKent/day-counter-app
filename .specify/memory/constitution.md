<!--
Sync Impact Report
==================
Version change: 2.0.0 → 2.1.0 (MINOR)
Reason: Added Principle VI (Security & Privacy) as a new standalone principle.
        Expanded Technical Standards with Baseline Profiles, WorkManager, localization,
        and crash-reporting expectations. No existing principle was removed or weakened.

Modified principles (old title → new title):
  None — all five existing principles retain their names and content.

Added sections:
  - Principle VI: Security & Privacy (new)
  - Technical Standards: Baseline Profiles requirement
  - Technical Standards: Localization requirement
  - Technical Standards: WorkManager added to mandatory-stack table (Principle IV)
  - Development Workflow: Pre-merge checklist — Security gate added

Removed sections:
  None

Templates requiring updates:
  - .specify/templates/plan-template.md ⚠ pending
      Constitution Check should include Principle VI (Security) gate.
      Technical Context fields should reference WorkManager and Baseline Profiles
      when applicable to the feature.
  - .specify/templates/spec-template.md ✅ No structural change required.
      Security acceptance scenarios should be included in User Stories where applicable;
      Principle VI guidance is sufficient for authors.
  - .specify/templates/tasks-template.md ✅ No structural change required.
      Security hardening is already listed in the Polish phase. Authors should
      reference Principle VI explicitly when security tasks are generated.

Follow-up TODOs:
  - TODO(MIN_SDK): Confirm minSdk = 26 (Android 8.0) is acceptable for target user base.
    Current choice covers ~97% of active Android devices (June 2025 distribution data).
  - TODO(COVERAGE_THRESHOLD): 80% line coverage on :domain module is the initial gate.
    Raise to 90% once the test suite matures past the first two features.
  - TODO(CRASH_REPORTING): Choose a crash-reporting backend (Firebase Crashlytics is the
    recommended default) and add it to the mandatory-stack table in Principle IV once decided.
-->

# Day Counter Constitution

## Core Principles

### I. User Experience First

Every product decision MUST be evaluated through the lens of the end user's experience.
Screens MUST comply with Material Design 3 guidelines. All interactive elements MUST meet
the 48 dp minimum touch-target size. The application MUST support:

- **Accessibility**: TalkBack compatibility with meaningful content descriptions on every
  interactive element; no information conveyed by color alone; focus order MUST be logical.
- **Theming**: Dark mode and dynamic color (Material You) MUST be supported from the first
  release. No hardcoded colors; all colors sourced from the Material 3 theme.
- **Responsiveness**: Layouts MUST adapt to compact, medium, and expanded window size classes
  (phones, foldables, tablets) using adaptive layout APIs.
- **Edge-to-edge**: The app MUST render edge-to-edge and handle window insets correctly on
  all API levels declared in the manifest.
- **Performance**: Cold start MUST complete in under 2 seconds on a mid-range device
  (equivalent to Pixel 4a). UI thread MUST NOT be blocked; all I/O and computation MUST
  run on appropriate Coroutine dispatchers.

**Rationale**: Users abandon apps with poor UX regardless of correctness. Accessibility and
responsiveness are not optional polish — they are baseline quality gates for Android.

### II. Test-Driven Development (NON-NEGOTIABLE)

The Red-Green-Refactor cycle is mandatory for all production code:

1. **Red**: Write a failing test that precisely describes the expected behavior.
2. **Green**: Write the minimum implementation code to make that test pass.
3. **Refactor**: Clean up the implementation without breaking the test.

No task may be marked done until all associated tests pass in CI.

**Test layers required**:

| Layer | Framework | Scope |
|-------|-----------|-------|
| Unit | JUnit 4 + Mockk | Domain use cases, data mappers, ViewModels |
| Integration | Hilt testing + Room in-memory | Repository implementations, database DAOs |
| UI | Compose Testing API | User-visible flows end-to-end |

**Coverage gate**: The `:domain` module MUST maintain ≥ 80 % line coverage as reported by
Kover. Coverage MAY NOT decrease in a PR without a documented justification.

UI tests MUST cover every acceptance scenario defined in the feature spec. New UI tests MUST
fail on the unimplemented screen before the screen is built.

**Rationale**: Date and time logic is dense with edge cases (leap years, DST transitions,
month boundaries, locale-specific formats). TDD forces explicit specification of behavior
before code exists and prevents regression as the feature set grows.

### III. Clean Architecture & Unidirectional Data Flow

The codebase MUST be organized into three layers with strict dependency rules:

```
:domain          ← pure Kotlin; zero Android framework dependencies
    ↑
:data            ← implements domain repository interfaces; owns data sources
    ↑
:presentation    ← Compose UI + ViewModels; depends on :domain only (not :data directly)
```

**Rules**:
- The `:domain` module MUST contain zero Android framework imports (`android.*`).
- Dependencies between layers point inward only: presentation → domain ← data.
- ViewModels MUST expose state as `StateFlow<UiState>` and events as `SharedFlow<UiEvent>`.
- Composables MUST be stateless or limited to hoisted local UI state. Business logic MUST NOT
  live in a Composable.
- Repository interfaces MUST be defined in `:domain`; implementations live in `:data`.
- Use cases in `:domain` MUST be single-responsibility (one public `operator fun invoke`).

**Rationale**: Clean Architecture makes each layer independently testable. ViewModels survive
configuration changes; stateless Composables are trivial to preview and test. Violations of
layer boundaries are the most common source of untestable, fragile Android code.

### IV. Modern Android Development Stack

The following stack is **mandatory** for all new code. Deviations require a constitution
amendment.

| Concern | Chosen Technology | Prohibited Alternative |
|---------|------------------|----------------------|
| Language | Kotlin (2.x) | Java in new files |
| UI toolkit | Jetpack Compose (latest stable BOM) | XML Views / View Binding |
| State management | `ViewModel` + `StateFlow` / `SharedFlow` | `LiveData`, `RxJava` |
| Dependency injection | Hilt (latest stable) | Manual DI, Koin, Dagger without Hilt |
| Async / reactive | Kotlin Coroutines + `Flow` | RxJava, callbacks |
| Navigation | Navigation Compose | Fragment backstack, manual intent routing |
| Local persistence | Room (if needed) | SQLiteOpenHelper, raw cursors |
| Preferences / secrets | DataStore (Preferences) for non-sensitive; `EncryptedSharedPreferences` for secrets | Plain `SharedPreferences` |
| Background work | WorkManager | Bare `Service`, `AlarmManager`, deprecated `JobScheduler` direct use |
| Build config | Gradle Version Catalogs (`libs.versions.toml`) | Hardcoded version strings |

All Jetpack library versions MUST be sourced from the official Jetpack Compose BOM where
applicable. Non-BOM dependencies MUST be pinned to an exact version in `libs.versions.toml`.

**Rationale**: The MAD stack is the current Android-endorsed approach. It provides lifecycle
safety, testability, and Kotlin-first APIs. Mixing paradigms (e.g., LiveData + Flow) creates
dual-source-of-truth bugs and increases cognitive overhead for reviewers.

### V. Code Quality & Maintainability

**Static analysis**: Android Lint MUST pass with zero errors before any PR is merged. Detekt
MUST be configured and its findings addressed; new `@Suppress` annotations require a comment
explaining why the suppression is justified.

**ProGuard / R8**: Release builds MUST enable minification and resource shrinking. No
class or method MAY be excluded from shrinking without a documented reason in the ProGuard
rules file.

**Dependencies**: Every new dependency MUST be justified in the PR description. Prefer
AndroidX / Jetpack libraries over third-party equivalents. No dependency with a known,
unpatched critical CVE MAY be merged.

**KDoc**: All public functions and classes in `:domain` and `:data` MUST have KDoc. UI
composables with non-obvious parameters MUST document those parameters.

**No dead code**: Unused imports, functions, and resource files MUST be removed before merge.
Commented-out code is not permitted; use git history instead.

**Rationale**: Static analysis catches real bugs before runtime. R8 reduces APK size and
attack surface. KDoc on domain code serves as the canonical API contract for future features
and test authors.

### VI. Security & Privacy

Security is a first-class quality attribute, not a post-launch concern. The following rules
are NON-NEGOTIABLE:

**Network security**:
- Cleartext HTTP traffic MUST be disabled in the Network Security Configuration for release
  builds (`cleartextTrafficPermitted="false"`).
- Certificate pinning MUST be applied for all first-party API endpoints in release builds.
  A documented pin-rotation procedure MUST exist before the first production release.

**Data protection**:
- Secrets (tokens, API keys, credentials) MUST NOT be hardcoded in source code or committed
  to version control. Build-time injection via `local.properties` + `BuildConfig` or
  environment variables is the required approach.
- User-sensitive data at rest MUST be encrypted using `EncryptedSharedPreferences` or
  the Jetpack Security `EncryptedFile` API. Plain `DataStore` is acceptable only for
  non-sensitive preferences.
- The app MUST NOT log sensitive user data (PII, tokens, passwords) at any log level in
  release builds. Timber or equivalent MUST be configured to suppress all logging in release.

**Component security**:
- Every `Activity`, `Service`, `BroadcastReceiver`, and `ContentProvider` declared in
  `AndroidManifest.xml` MUST specify `android:exported` explicitly.
- Exported components MUST declare the minimum required permissions. Components that do not
  need to be exported MUST set `android:exported="false"`.
- Deep-link URIs MUST validate and sanitize all input parameters before use; no implicit
  trust of intent extras from external callers.

**Authentication**:
- Biometric authentication MUST use `BiometricPrompt` (AndroidX Biometric library).
  Deprecated fingerprint APIs (`FingerprintManager`) are prohibited.

**OWASP Mobile Top 10 compliance**: Each feature involving user data or remote communication
MUST be reviewed against the current OWASP Mobile Top 10. Findings MUST be addressed before
the feature is merged; documented, accepted exceptions require a recorded justification in
the feature spec.

**Rationale**: Android apps are high-value targets for credential theft, data exfiltration,
and reverse engineering. Embedding security rules in the constitution ensures they are
evaluated at design time — the cheapest point to address them — rather than discovered in
production or a security audit.

## Technical Standards

**SDK configuration** (minimum requirements; may only increase via MINOR amendment):

| Setting | Value |
|---------|-------|
| `minSdk` | 26 (Android 8.0 — covers ~97% of active devices) |
| `targetSdk` | Latest stable Android release |
| `compileSdk` | Same as `targetSdk` |
| Kotlin | 2.x (latest stable) |
| AGP | Latest stable |

**Build variants**: Debug builds MUST enable strict-mode logging. Release builds MUST enable
R8 full mode. No feature flags or debug-only code paths MUST ship in release APKs.

**APK / AAB size**: The release AAB MUST NOT exceed 10 MB for the initial feature set.
Exceeding this threshold requires documented justification.

**Baseline Profiles**: A Baseline Profile MUST be generated and shipped with the release AAB
to improve cold-start and frame-render performance for new installs. The profile MUST be
regenerated whenever a significant new user flow is introduced. Generation is performed with
the Macrobenchmark library; results MUST be reviewed before each release.

**Localization**: All user-facing strings MUST be externalized in `res/values/strings.xml`.
Hardcoded string literals in Composable functions or ViewModels are prohibited (except for
non-visible technical labels used exclusively in tests). The default locale MUST be English;
additional locales are additive and do not require a constitution amendment.

**Date/time representation**: All date values MUST use `java.time` (`LocalDate`, `LocalDateTime`,
`ZonedDateTime`). ISO 8601 format (YYYY-MM-DD) is the canonical serialization format.
No implicit timezone assumption is permitted in business logic; timezone MUST be explicit at
every call site that needs it.

## Development Workflow

All work MUST follow the SpecKit workflow in order: **Specify → Plan → Tasks → Implement**.
Skipping a phase requires a recorded justification in the relevant feature spec.

**Branch naming**: Sequential numbering as configured (`branch_numbering: sequential`).
Every commit MUST reference the task ID it addresses.

**Pre-merge checklist** (enforced in code review):

- [ ] Constitution Check section in `plan.md` completed and all gates passed.
- [ ] All new code has corresponding failing tests written before implementation (Principle II).
- [ ] Layer boundaries respected — no `:domain` imports of `android.*` (Principle III).
- [ ] Stack compliance verified — no prohibited technologies introduced (Principle IV).
- [ ] Android Lint: zero errors. Detekt: no new findings without suppression comment (Principle V).
- [ ] Security review completed against Principle VI (network, data, components, OWASP).
- [ ] No secrets or sensitive data in committed source files (Principle VI).
- [ ] UI tested on at least one compact and one expanded window size class (Principle I).
- [ ] Accessibility: TalkBack manual smoke test on new screens (Principle I).
- [ ] Dark mode: visual inspection on new screens (Principle I).

Complexity violations MUST be documented in the Complexity Tracking table in `plan.md`
with: the violated principle, the concrete need, and the simpler alternative that was rejected.

## Governance

This constitution supersedes all other project conventions and practices.

**Amendment procedure**:
1. Run `/speckit-constitution` with the proposed change as input.
2. The updated constitution is written to `.specify/memory/constitution.md`.
3. Commit with: `docs: amend constitution to vX.Y.Z (<brief summary>)`.

Amendments that remove, rename, or substantively weaken an existing principle MUST include
a migration plan for code that depended on the prior rule.

**Versioning policy**:
- **MAJOR**: Principle removed, renamed, or substantively weakened; prohibited technology list changed.
- **MINOR**: New principle or section added; coverage threshold raised; new mandatory tool added.
- **PATCH**: Wording clarified; typo fixed; table reformatted; non-semantic refinement.

**Version**: 2.1.0 | **Ratified**: 2026-05-27 | **Last Amended**: 2026-05-27