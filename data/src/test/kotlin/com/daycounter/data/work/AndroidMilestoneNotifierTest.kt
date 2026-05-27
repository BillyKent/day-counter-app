package com.daycounter.data.work

// NOTE: Concrete milestone-deduplication behaviour is exercised in
// :app via AndroidMilestoneNotifier. A pure-Kotlin unit test there asserts:
// - insertOrIgnore returning a new row id triggers NotificationManagerCompat.notify
// - insertOrIgnore returning -1 (duplicate) is a no-op
// - notifications_enabled=false suppresses the post even if insert succeeded
// This file is a marker for the integration test layer; see :app androidTest.
