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

/** US6 validation: category length (0-50) and goalMilestoneTarget ∈ {7,30,100,365}. */
class CreateUpdateCounterValidationTest {

    private val today = LocalDate.of(2026, 5, 27)
    private val zone = ZoneOffset.UTC
    private val clock = Clock.fixed(today.atStartOfDay(zone).toInstant(), zone)
    private lateinit var repo: CounterRepository
    private lateinit var create: CreateCounterUseCase
    private lateinit var update: UpdateCounterUseCase

    private val existing = Counter(
        id = 5L,
        goalName = "Read",
        startDate = today.minusDays(10),
        createdAt = Instant.parse("2026-05-17T00:00:00Z"),
    )

    @Before
    fun setUp() {
        repo = mockk(relaxed = true)
        create = CreateCounterUseCase(repo, clock, zone)
        update = UpdateCounterUseCase(repo, clock, zone)
        coEvery { repo.getById(existing.id) } returns existing
    }

    @Test
    fun `category over 50 chars is rejected on create`() = runTest {
        val result = create(goalName = "Run", startDate = today, category = "x".repeat(51), goalMilestoneTarget = 30)
        assertEquals(
            CreateCounterUseCase.ValidationFailure.CategoryTooLong,
            (result as CreateCounterUseCase.Result.ValidationError).failure,
        )
    }

    @Test
    fun `goal target outside the allowed set is rejected on create`() = runTest {
        val result = create(goalName = "Run", startDate = today, category = null, goalMilestoneTarget = 45)
        assertEquals(
            CreateCounterUseCase.ValidationFailure.InvalidGoalTarget,
            (result as CreateCounterUseCase.Result.ValidationError).failure,
        )
    }

    @Test
    fun `valid create persists category and goal target`() = runTest {
        coEvery { repo.insert(any()) } returns 1L
        val captured = slot<Counter>()
        create(goalName = "Stop smoking", startDate = today, category = "Salud", goalMilestoneTarget = 100)
        coVerify { repo.insert(capture(captured)) }
        assertEquals("Salud", captured.captured.category)
        assertEquals(100, captured.captured.goalMilestoneTarget)
    }

    @Test
    fun `blank category is stored as null`() = runTest {
        coEvery { repo.insert(any()) } returns 1L
        val captured = slot<Counter>()
        create(goalName = "Run", startDate = today, category = "   ", goalMilestoneTarget = 7)
        coVerify { repo.insert(capture(captured)) }
        assertTrue(captured.captured.category == null)
    }

    @Test
    fun `invalid goal target is rejected on update`() = runTest {
        val result = update(counterId = existing.id, goalName = "Read", startDate = existing.startDate, category = null, goalMilestoneTarget = 365_000)
        assertEquals(
            UpdateCounterUseCase.ValidationFailure.InvalidGoalTarget,
            (result as UpdateCounterUseCase.Result.ValidationError).failure,
        )
    }

    @Test
    fun `valid update persists category and goal target`() = runTest {
        val captured = slot<Counter>()
        update(counterId = existing.id, goalName = "Read", startDate = existing.startDate, category = "Hábitos", goalMilestoneTarget = 365)
        coVerify { repo.update(capture(captured)) }
        assertEquals("Hábitos", captured.captured.category)
        assertEquals(365, captured.captured.goalMilestoneTarget)
    }
}
