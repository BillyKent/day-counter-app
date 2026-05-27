package com.daycounter.domain.usecase

import com.daycounter.domain.model.Counter
import com.daycounter.domain.repository.CounterRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class DeleteCounterUseCaseTest {

    @Test
    fun `delegates to repository delete`() = runTest {
        val counter = Counter(
            id = 5L,
            goalName = "Run",
            startDate = LocalDate.of(2026, 5, 20),
            createdAt = Instant.parse("2026-05-20T00:00:00Z"),
        )
        val repo = mockk<CounterRepository>(relaxed = true)
        coEvery { repo.getById(counter.id) } returns counter
        val sut = DeleteCounterUseCase(repo)

        sut(counter.id)

        coVerify(exactly = 1) { repo.delete(counter) }
    }

    @Test
    fun `is a no-op when counter does not exist`() = runTest {
        val repo = mockk<CounterRepository>(relaxed = true)
        coEvery { repo.getById(99L) } returns null
        val sut = DeleteCounterUseCase(repo)

        sut(99L)

        coVerify(exactly = 0) { repo.delete(any()) }
    }
}
