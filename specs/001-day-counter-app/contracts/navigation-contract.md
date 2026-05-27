# Navigation Contract: AppNavGraph

**Feature branch**: `001-day-counter-app` | **Date**: 2026-05-27

This document defines the Navigation Compose route schema, argument types, deep link URIs,
and navigation rules for the Day Counter app.

---

## Route Definitions

All routes are sealed objects in `com.daycounter.presentation.navigation.Screen`.

| Destination | Route String | Arguments | Entry Point |
|-------------|-------------|-----------|-------------|
| Onboarding | `onboarding` | — | First launch only |
| Home | `home` | — | Default after onboarding |
| Create Counter | `counter/create` | — | FAB on Home |
| Edit Counter | `counter/{counterId}/edit` | `counterId: Long` | Edit action on counter card |
| Counter Detail | `counter/{counterId}` | `counterId: Long` | Widget tap / notification tap |
| Settings | `settings` | — | Settings icon on Home toolbar |

---

## Start Destination Logic

`AppNavGraph` checks `OnboardingPreferencesDataStore.onboardingShown` on first composition:

```
onboardingShown == false  →  startDestination = Screen.Onboarding
onboardingShown == true   →  startDestination = Screen.Home
```

The check is performed synchronously at NavHost initialization via a `runBlocking` read of
the first value from the `Flow<Boolean>`, or via a `SplashScreen` API that holds the splash
until the flag is read. `MainActivity` never shows a blank frame.

---

## Deep Link URIs

Used by widget tap (PendingIntent) and milestone notification tap (PendingIntent).

| URI Pattern | Destination | Behavior on invalid ID |
|------------|-------------|----------------------|
| `daycounter://counter/{counterId}` | `Screen.CounterDetail` | Navigate to `Screen.Home` |

The deep link is registered in `AppNavGraph`:

```kotlin
composable(
    route = Screen.CounterDetail.route,
    arguments = listOf(navArgument("counterId") { type = NavType.LongType }),
    deepLinks = listOf(navDeepLink {
        uriPattern = "daycounter://counter/{counterId}"
    })
) { backStackEntry ->
    val counterId = backStackEntry.arguments?.getLong("counterId") ?: -1L
    CounterDetailScreen(counterId = counterId)
}
```

`CounterDetailViewModel` calls `GetCounterByIdUseCase(counterId)`. If the use case returns
`null` (counter deleted), the ViewModel emits a `NavigateToHome` `UiEvent`; the screen
navigates to `Screen.Home` and removes itself from the back stack.

---

## Notification PendingIntent

Milestone notifications use the same deep link:

```kotlin
val intent = Intent(
    Intent.ACTION_VIEW,
    Uri.parse("daycounter://counter/$counterId"),
    context,
    MainActivity::class.java
)
val pendingIntent = PendingIntent.getActivity(
    context,
    counterId.toInt(),         // unique request code per counter
    intent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)
```

Each notification uses `counterId.toInt()` as the notification ID so tapping any of
multiple simultaneous milestone notifications opens the correct counter.

---

## Navigation Rules

| From | Action | To | Back Stack |
|------|--------|----|-----------|
| Onboarding (last screen or skip) | `onboardingShown = true`; navigate | Home | Onboarding popped from back stack (`popUpTo(onboarding) { inclusive = true }`) |
| Home | Tap "Add Counter" | Create Counter | Home remains on stack |
| Create Counter | Save success | Home | Create Counter popped |
| Create Counter | Cancel / back | Home | Create Counter popped |
| Home | Tap "Edit" on counter card | Edit Counter | Home remains on stack |
| Edit Counter | Save success | Home | Edit Counter popped |
| Edit Counter | Cancel / back | Home | Edit Counter popped |
| Home | Tap Settings icon | Settings | Home remains on stack |
| Settings | Back | Home | Settings popped |
| Widget tap / notification tap | Deep link | Counter Detail | Existing stack preserved |
| Counter Detail (counter deleted) | Auto-redirect | Home | Counter Detail popped |

---

## Manifest Deep Link Declaration

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:launchMode="singleTop">
    <intent-filter>
        <action android:name="android.intent.action.MAIN"/>
        <category android:name="android.intent.category.LAUNCHER"/>
    </intent-filter>
    <intent-filter android:autoVerify="false">
        <action android:name="android.intent.action.VIEW"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <data android:scheme="daycounter" android:host="counter"/>
    </intent-filter>
</activity>
```

`launchMode="singleTop"` ensures that tapping a widget or notification while the app is
already open does not create a new `MainActivity` instance; `onNewIntent` is called
instead, and Navigation Compose handles the deep link via `handleDeepLink`.

`autoVerify="false"` — this is an internal URI scheme, not a web URL; Android App Links
verification is not required.
