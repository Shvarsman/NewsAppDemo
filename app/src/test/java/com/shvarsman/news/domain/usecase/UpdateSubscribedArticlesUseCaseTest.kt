package com.shvarsman.news.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.shvarsman.news.domain.entity.Interval
import com.shvarsman.news.domain.entity.Language
import com.shvarsman.news.domain.entity.Settings
import com.shvarsman.news.domain.repository.NewsRepository
import com.shvarsman.news.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UpdateSubscribedArticlesUseCaseTest {

    private val newsRepository: NewsRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()

    private val useCase = UpdateSubscribedArticlesUseCase(newsRepository, settingsRepository)

    @Test
    fun `requests refresh using the current language and returns updated topics`() = runTest {
        val settings = Settings(
            language = Language.RUSSIAN,
            interval = Interval.HOUR_1,
            notificationsEnabled = true,
            wifiOnly = false
        )
        every { settingsRepository.getSettings() } returns flowOf(settings)
        coEvery {
            newsRepository.updateArticlesForAllSubscriptions(Language.RUSSIAN)
        } returns listOf("kotlin", "android")

        val result = useCase()

        assertThat(result).containsExactly("kotlin", "android")
    }

    @Test
    fun `returns empty list when nothing was updated`() = runTest {
        every { settingsRepository.getSettings() } returns flowOf(
            Settings(Language.ENGLISH, Interval.MIN_15,
                notificationsEnabled = false,
                wifiOnly = true
            )
        )
        coEvery {
            newsRepository.updateArticlesForAllSubscriptions(any())
        } returns emptyList()

        val result = useCase()

        assertThat(result).isEmpty()
    }
}
