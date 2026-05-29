package com.daycounter.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MilestoneQueriesTest {

    private val next = GetNextMilestoneUseCase()
    private val achieved = GetAchievedMilestonesUseCase()
    private val mostRecent = GetMostRecentMilestoneUseCase()

    @Test
    fun `next milestone is smallest strictly greater`() {
        assertEquals(1, next(0))
        assertEquals(7, next(1))
        assertEquals(100, next(35))
        assertEquals(1000, next(365))
    }

    @Test
    fun `next milestone is null at or beyond the largest`() {
        assertNull(next(1000))
        assertNull(next(1500))
    }

    @Test
    fun `achieved milestones are those at or below the streak`() {
        assertEquals(emptyList<Int>(), achieved(0))
        assertEquals(listOf(1, 7, 30), achieved(35))
        assertEquals(listOf(1, 7, 30, 100, 365, 1000), achieved(1000))
    }

    @Test
    fun `most recent milestone is highest at or below the streak`() {
        assertNull(mostRecent(0))
        assertEquals(1, mostRecent(1))
        assertEquals(30, mostRecent(35))
        assertEquals(1000, mostRecent(1200))
    }
}
