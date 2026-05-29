package com.daycounter.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CheckMilestonesUseCaseTest {

    private val sut = CheckMilestonesUseCase()

    @Test
    fun `returns matching milestone for each canonical day`() {
        listOf(1, 7, 30, 100, 365, 1000).forEach { m ->
            assertEquals(m, sut(m))
        }
    }

    @Test
    fun `returns null for non-milestone day counts`() {
        listOf(0, 2, 6, 8, 29, 31, 99, 101, 364, 366, 999, 1001).forEach { d ->
            assertNull("streak=$d should not match", sut(d))
        }
    }
}
