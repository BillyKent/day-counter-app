package com.daycounter.domain.usecase

import com.daycounter.domain.model.Counter
import com.daycounter.domain.repository.CounterRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class CreateCounterUseCaseTest {

    private val today = LocalDate.of(2026, 5, 27)
    private val zone: ZoneId = ZoneOffset.UTC
    private val clock = Clock.fixed(today.atStartOfDay(zone).toInstant(), zone)
    private lateinit var repo: CounterRepository
    private lateinit var sut: CreateCounterUseCase

    @Before
    fun setUp() {
        repo = mockk(relaxed = true)
        sut = CreateCounterUseCase(repo, clock, zone)
    }

    @Test
    fun `blank name returns ValidationError NameBlank`() = runTest {
        val result = sut(goalName = "   ", startDate = today)
        assertTrue(result is CreateCounterUseCase.Result.ValidationError)
        assertEquals(
            CreateCounterUseCase.ValidationFailure.NameBlank,
            (result as CreateCounterUseCase.Result.ValidationError).failure,
        )
    }

    @Test
    fun `name longer than 100 chars returns ValidationError NameTooLong`() = runTest {
        val name = "a".repeat(101)
        val result = sut(goalName = name, startDate = today)
        assertTrue(result is CreateCounterUseCase.Result.ValidationError)
        assertEquals(
            CreateCounterUseCase.ValidationFailure.NameTooLong,
            (result as CreateCounterUseCase.Result.ValidationError).failure,
        )
    }

    @Test
    fun `future startDate returns ValidationError FutureStartDate`() = runTest {
        val result = sut(goalName = "Run", startDate = today.plusDays(1))
        assertTrue(result is CreateCounterUseCase.Result.ValidationError)
        assertEquals(
            CreateCounterUseCase.ValidationFailure.FutureStartDate,
            (result as CreateCounterUseCase.Result.ValidationError).failure,
        )
    }

    @Test
    fun `null startDate defaults to today`() = runTest {
        coEvery { repo.insert(any()) } returns 1L
        val captured = slot<Counter>()
        sut(goalName = "Read", startDate = null)
        coVerify { repo.insert(capture(captured)) }
        assertEquals(today, captured.captured.startDate)
    }

    @Test
    fun `valid input inserts counter and returns Success`() = runTest {
        coEvery { repo.insert(any()) } returns 42L
        val result = sut(goalName = "No alcohol", startDate = today.minusDays(7))
        assertTrue(result is CreateCounterUseCase.Result.Success)
        assertEquals(42L, (result as CreateCounterUseCase.Result.Success).id)
        coVerify(exactly = 1) { repo.insert(any()) }
    }
}
