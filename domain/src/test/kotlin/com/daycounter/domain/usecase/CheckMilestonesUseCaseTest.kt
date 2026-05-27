package com.daycounter.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CheckMilestonesUseCaseTest {

    private val sut = CheckMilestonesUseCase()

    @Test
    fun `returns matching milestone for each canonical day`() {
        listOf(7, 30, 60, 90, 180, 365).forEach { m ->
            assertEquals(m, sut(m))
        }
    }

    @Test
    fun `returns null for non-milestone day counts`() {
        listOf(0, 1, 6, 8, 29, 31, 100, 364, 366).forEach { d ->
            assertNull("streak=$d should not match", sut(d))
        }
    }
}
