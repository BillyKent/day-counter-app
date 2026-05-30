package com.daycounter.domain.model

/**
 * Aggregate pause metrics for the Estadísticas "Pausas" card (FR-027).
 *
 * @property pausedNow Number of counters currently paused.
 * @property totalPausedDays Sum of all paused days (completed intervals + ongoing pauses).
 * @property totalPauses Total number of pauses (completed intervals + currently-paused counters).
 */
data class PauseStats(
    val pausedNow: Int,
    val totalPausedDays: Int,
    val totalPauses: Int,
) {
    companion object {
        val EMPTY = PauseStats(pausedNow = 0, totalPausedDays = 0, totalPauses = 0)
    }
}
