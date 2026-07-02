package com.shvarsman.news.domain.usecase

import com.shvarsman.news.domain.entity.Interval
import com.shvarsman.news.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UpdateIntervalUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk()
    private val useCase = UpdateIntervalUseCase(settingsRepository)

    @Test
    fun `passes the interval's raw minute value to the repository`() = runTest {
        coEvery { settingsRepository.updateInterval(any()) } returns Unit

        useCase(Interval.HOUR_8)

        coVerify { settingsRepository.updateInterval(480) }
    }
}
