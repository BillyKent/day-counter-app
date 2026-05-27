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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class UpdateCounterUseCaseTest {

    private val today = LocalDate.of(2026, 5, 27)
    private val clock = Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)
    private lateinit var repo: CounterRepository
    private lateinit var sut: UpdateCounterUseCase

    private val existing = Counter(
        id = 5L,
        goalName = "Read",
        startDate = today.minusDays(10),
        createdAt = Instant.parse("2026-05-17T00:00:00Z"),
    )

    @Before
    fun setUp() {
        repo = mockk(relaxed = true)
        sut = UpdateCounterUseCase(repo, clock, ZoneOffset.UTC)
        coEvery { repo.getById(existing.id) } returns existing
    }

    @Test
    fun `blank name returns ValidationError NameBlank`() = runTest {
        val result = sut(counterId = existing.id, goalName = "   ", startDate = today)
        assertTrue(result is UpdateCounterUseCase.Result.ValidationError)
        assertEquals(
            UpdateCounterUseCase.ValidationFailure.NameBlank,
            (result as UpdateCounterUseCase.Result.ValidationError).failure,
        )
    }

    @Test
    fun `name too long returns ValidationError NameTooLong`() = runTest {
        val result = sut(counterId = existing.id, goalName = "x".repeat(101), startDate = today)
        assertEquals(
            UpdateCounterUseCase.ValidationFailure.NameTooLong,
            (result as UpdateCounterUseCase.Result.ValidationError).failure,
        )
    }

    @Test
    fun `future startDate returns ValidationError FutureStartDate`() = runTest {
        val result = sut(counterId = existing.id, goalName = "Read", startDate = today.plusDays(1))
        assertEquals(
            UpdateCounterUseCase.ValidationFailure.FutureStartDate,
            (result as UpdateCounterUseCase.Result.ValidationError).failure,
        )
    }

    @Test
    fun `missing counter returns NotFound`() = runTest {
        coEvery { repo.getById(99L) } returns null
        val result = sut(counterId = 99L, goalName = "Read", startDate = today)
        assertTrue(result is UpdateCounterUseCase.Result.NotFound)
    }

    @Test
    fun `valid update persists new fields`() = runTest {
        val captured = slot<Counter>()
        val result = sut(counterId = existing.id, goalName = " Walk ", startDate = today.minusDays(3))
        coVerify { repo.update(capture(captured)) }
        assertTrue(result is UpdateCounterUseCase.Result.Success)
        assertEquals("Walk", captured.captured.goalName)
        assertEquals(today.minusDays(3), captured.captured.startDate)
        assertEquals(existing.createdAt, captured.captured.createdAt)
    }
}
