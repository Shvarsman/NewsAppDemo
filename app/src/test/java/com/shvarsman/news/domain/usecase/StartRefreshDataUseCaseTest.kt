package com.shvarsman.news.domain.usecase

import com.shvarsman.news.domain.entity.Interval
import com.shvarsman.news.domain.entity.Language
import com.shvarsman.news.domain.entity.RefreshConfig
import com.shvarsman.news.domain.entity.Settings
import com.shvarsman.news.domain.repository.NewsRepository
import com.shvarsman.news.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StartRefreshDataUseCaseTest {

    private val newsRepository: NewsRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk()

    private val useCase = StartRefreshDataUseCase(newsRepository, settingsRepository)

    @Test
    fun `schedules a background refresh based on the current settings`() = runTest {
        val settings = Settings(Language.ENGLISH, Interval.MIN_30,
            notificationsEnabled = false,
            wifiOnly = true
        )
        every { settingsRepository.getSettings() } returns flowOf(settings)

        useCase()

        verify {
            newsRepository.startBackgroundRefresh(
                RefreshConfig(language = Language.ENGLISH, interval = Interval.MIN_30, wifiOnly = true)
            )
        }
    }

    @Test
    fun `maps settings language and wifiOnly into the refresh config unchanged`() = runTest {
        val settings = Settings(Language.RUSSIAN, Interval.HOUR_24, true, false)
        every { settingsRepository.getSettings() } returns flowOf(settings)

        useCase()

        verify {
            newsRepository.startBackgroundRefresh(
                RefreshConfig(language = Language.RUSSIAN, interval = Interval.HOUR_24, wifiOnly = false)
            )
        }
    }
}
