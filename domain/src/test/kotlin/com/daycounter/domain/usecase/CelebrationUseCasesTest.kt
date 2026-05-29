package com.daycounter.domain.usecase

import com.daycounter.domain.model.MilestoneRecord
import com.daycounter.domain.repository.MilestoneRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.Instant

class CelebrationUseCasesTest {

    @Test
    fun `mark celebrations shown delegates to repository`() = runTest {
        val repo = mockk<MilestoneRepository>(relaxed = true)
        MarkCelebrationsShownUseCase(repo)(counterId = 42L)
        coVerify(exactly = 1) { repo.markAllShownForCounter(42L) }
    }

    @Test
    fun `check milestones recognises the new milestone set`() {
        val sut = CheckMilestonesUseCase()
        listOf(1, 7, 30, 100, 365, 1000).forEach { assertEquals(it, sut(it)) }
    }

    @Test
    fun `a freshly recorded milestone defaults to celebration not shown`() {
        val record = MilestoneRecord(counterId = 1L, milestoneDays = 7, notifiedAt = Instant.EPOCH)
        assertFalse(record.celebrationShown)
    }
}
