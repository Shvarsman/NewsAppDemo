package com.shvarsman.news.domain.usecase

import com.shvarsman.news.domain.repository.NewsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RemoveSubscriptionUseCaseTest {

    private val newsRepository: NewsRepository = mockk()
    private val useCase = RemoveSubscriptionUseCase(newsRepository)

    @Test
    fun `delegates removal to repository with exact topic`() = runTest {
        coEvery { newsRepository.removeSubscription(any()) } returns Unit

        useCase("kotlin")

        coVerify(exactly = 1) { newsRepository.removeSubscription("kotlin") }
    }
}
