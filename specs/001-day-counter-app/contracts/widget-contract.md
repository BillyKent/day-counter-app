# Widget Contract: DayCounterWidget

**Feature branch**: `001-day-counter-app` | **Date**: 2026-05-27

This document specifies the Glance AppWidget state model, supported sizes, action
PendingIntents, and manifest declaration requirements for the Day Counter home-screen widget.

---

## Widget Sizes

| Variant | Cells | `minResizeWidth` | `minResizeHeight` | Content |
|---------|-------|-----------------|------------------|---------|
| Compact | 2×1 | 110 dp | 40 dp | Streak count (large) + truncated goal name (1 line, ellipsis) |
| Medium | 4×2 | 250 dp | 110 dp | Full goal name (2 lines max) + streak count + "days" label |

Both sizes are provided by a single `GlanceAppWidget` implementation
(`DayCounterWidget`) that branches its layout on `LocalSize.current`. The
`appwidget-provider` XML declares `resizeMode="horizontal|vertical"` so the
user can resize between the two size classes.

---

## State Model

Defined as a `kotlinx.serialization.Serializable` data class stored via
`GlanceStateDefinition` (Glance's DataStore-backed state).

```kotlin
@Serializable
data class DayCounterWidgetState(
    val counterId: Long?,          // null → counter was deleted
    val goalName: String,
    val streakDays: Int,
    val isCounterDeleted: Boolean  // true → show "counter removed" placeholder
)
```

**Initial state** (before `CounterPickerActivity` completes):
```kotlin
DayCounterWidgetState(
    counterId = null,
    goalName = "",
    streakDays = 0,
    isCounterDeleted = false
)
```

**Deleted counter state**:
```kotlin
DayCounterWidgetState(
    counterId = null,
    goalName = "",
    streakDays = 0,
    isCounterDeleted = true
)
```

The widget renders a "Counter removed" placeholder with a "Select counter" button when
`isCounterDeleted == true`.

---

## State Updates

State is pushed (never pulled) from two sites:

| Site | Trigger | Method |
|------|---------|--------|
| `DailyUpdateWorker` | Every daily worker execution | `GlanceAppWidgetManager.updateIf<DayCounterWidget>()` for each bound widget |
| CRUD operations (create/edit/reset/delete) | Immediately after a counter is modified | Same `updateIf` call inside the use case caller in `:presentation` ViewModels |

The Glance state DataStore key is the system-assigned `GlanceId`. `WidgetBindingRepository`
maps `appWidgetId (Int) → GlanceId` via `GlanceAppWidgetManager.getGlanceIdBy(appWidgetId)`.

---

## PendingIntent Actions

### 1. Tap to open counter detail

Triggered when the user taps anywhere on the widget surface (excluding the
"Select counter" button in deleted state).

```
Intent:
  action  = Intent.ACTION_VIEW
  data    = Uri.parse("daycounter://counter/{counterId}")
  target  = MainActivity

PendingIntent flags: FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
```

If `counterId` is null (counter deleted), this action is disabled and the "Select
counter" button action fires instead.

### 2. Select / re-select counter

Triggered by:
- Tapping the "Select counter" button in the deleted-counter placeholder.
- Widget configuration on first placement (handled by `CounterPickerActivity`).

```
Intent:
  target  = CounterPickerActivity
  extra   = EXTRA_APPWIDGET_ID: Int  (system App Widget ID)
  flags   = FLAG_ACTIVITY_NEW_TASK

PendingIntent flags: FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
```

`CounterPickerActivity` sets `RESULT_OK` with the updated `appWidgetId` on completion,
which triggers Glance state refresh.

---

## Manifest Declarations

```xml
<!-- AppWidgetProvider receiver -->
<receiver
    android:name=".widget.DayCounterWidgetReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE"/>
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/day_counter_widget_info"/>
</receiver>

<!-- CounterPickerActivity (widget configuration) -->
<activity
    android:name=".widget.CounterPickerActivity"
    android:exported="false"
    android:theme="@style/Theme.DayCounter.WidgetPicker"/>
```

`appwidget-provider` XML (`res/xml/day_counter_widget_info.xml`):
```xml
<appwidget-provider
    android:minWidth="110dp"
    android:minHeight="40dp"
    android:minResizeWidth="110dp"
    android:minResizeHeight="40dp"
    android:targetCellWidth="2"
    android:targetCellHeight="1"
    android:maxResizeWidth="250dp"
    android:maxResizeHeight="110dp"
    android:resizeMode="horizontal|vertical"
    android:updatePeriodMillis="0"
    android:configure=".widget.CounterPickerActivity"
    android:previewLayout="@layout/widget_preview"/>
```

`updatePeriodMillis="0"` — all updates are driven by `DailyUpdateWorker`, not the system
polling interval, to avoid unnecessary wake-ups (max system poll is every 30 min anyway).

---

## Security Notes

- `DayCounterWidgetReceiver` is `exported="true"` as required by the App Widget framework.
  It only handles `APPWIDGET_UPDATE`, `APPWIDGET_DELETED`, and `APPWIDGET_DISABLED` intents.
- `CounterPickerActivity` is `exported="false"` — only launched via `PendingIntent` from
  within the app process.
- Deep-link URI `daycounter://counter/{counterId}` validates the counter ID against Room
  before navigating; invalid or missing IDs fall back to the home screen.
- All `PendingIntent` instances use `FLAG_IMMUTABLE` (required on API 31+).
