package com.daycounter.domain.model

/**
 * Time-of-day for the daily reminder. Minute granularity is 5 minutes to match the time picker.
 *
 * @property hour 0–23.
 * @property minute 0,5,…,55.
 */
data class ReminderTime(
    val hour: Int,
    val minute: Int,
) {
    init {
        require(hour in 0..23) { "hour must be 0..23, was $hour" }
        require(minute in 0..59 && minute % MINUTE_STEP == 0) {
            "minute must be a multiple of $MINUTE_STEP in 0..55, was $minute"
        }
    }

    /** `HH:mm` serialization used for persistence. */
    fun serialize(): String = "%02d:%02d".format(hour, minute)

    companion object {
        const val MINUTE_STEP: Int = 5

        /** Default reminder time (09:00). */
        val DEFAULT: ReminderTime = ReminderTime(9, 0)

        /** Quick presets surfaced in the picker. */
        val MORNING: ReminderTime = ReminderTime(8, 0)
        val MIDDAY: ReminderTime = ReminderTime(13, 0)
        val EVENING: ReminderTime = ReminderTime(21, 0)

        /** Parses an `HH:mm` string back to a [ReminderTime], falling back to [DEFAULT]. */
        fun parse(value: String?): ReminderTime {
            val parts = value?.split(":") ?: return DEFAULT
            val h = parts.getOrNull(0)?.toIntOrNull() ?: return DEFAULT
            val m = parts.getOrNull(1)?.toIntOrNull() ?: return DEFAULT
            return runCatching { ReminderTime(h, m) }.getOrDefault(DEFAULT)
        }
    }
}
