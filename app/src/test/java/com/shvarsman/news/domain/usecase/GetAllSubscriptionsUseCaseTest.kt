package com.shvarsman.news.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shvarsman.news.domain.repository.NewsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetAllSubscriptionsUseCaseTest {

    private val newsRepository: NewsRepository = mockk()
    private val useCase = GetAllSubscriptionsUseCase(newsRepository)

    @Test
    fun `returns the flow of subscriptions exposed by the repository`() = runTest {
        every { newsRepository.getAllSubscriptions() } returns flowOf(listOf("kotlin", "android"))

        useCase().test {
            assertThat(awaitItem()).containsExactly("kotlin", "android")
            awaitComplete()
        }
    }
}
