package com.shvarsman.news.domain.usecase

import com.shvarsman.news.domain.entity.Interval
import com.shvarsman.news.domain.entity.Language
import com.shvarsman.news.domain.entity.Settings
import com.shvarsman.news.domain.repository.NewsRepository
import com.shvarsman.news.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AddSubscriptionUseCaseTest {

    private val newsRepository: NewsRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk()

    private val useCase = AddSubscriptionUseCase(newsRepository, settingsRepository)

    @Test
    fun `adds subscription to repository immediately`() = runTest {
        every { settingsRepository.getSettings() } returns flowOf(defaultSettings())
        coEvery { newsRepository.updateArticlesForTopic(any(), any()) } returns true

        useCase("kotlin")

        coVerify { newsRepository.addSubscription("kotlin") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `triggers background articles refresh for the new topic`() = runTest {
        every { settingsRepository.getSettings() } returns flowOf(defaultSettings(language = Language.GERMAN))
        coEvery { newsRepository.updateArticlesForTopic(any(), any()) } returns true

        useCase("android")
        advanceUntilIdle()

        coVerify { newsRepository.updateArticlesForTopic("android", Language.GERMAN) }
    }

    private fun defaultSettings(language: Language = Language.ENGLISH) = Settings(
        language = language,
        interval = Interval.MIN_15,
        notificationsEnabled = false,
        wifiOnly = true
    )
}
