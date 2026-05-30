package com.daycounter.domain.usecase

import com.daycounter.domain.repository.CounterRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

class PauseResumeUseCaseTest {

    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 5, 29)
    private val clock = Clock.fixed(today.atStartOfDay(zone).toInstant(), zone)
    private val repository = mockk<CounterRepository>(relaxed = true)

    @Test
    fun `pause delegates to repository with today`() = runTest {
        PauseCounterUseCase(repository, clock, zone)(counterId = 7L)
        coVerify(exactly = 1) { repository.pause(7L, today) }
    }

    @Test
    fun `resume delegates to repository with today`() = runTest {
        ResumeCounterUseCase(repository, clock, zone)(counterId = 7L)
        coVerify(exactly = 1) { repository.resume(7L, today) }
    }
}
