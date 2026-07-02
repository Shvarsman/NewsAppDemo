package com.shvarsman.news.domain.usecase

import com.shvarsman.news.domain.repository.NewsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ClearAllArticlesUseCaseTest {

    private val newsRepository: NewsRepository = mockk()
    private val useCase = ClearAllArticlesUseCase(newsRepository)

    @Test
    fun `passes the given topics through to the repository`() = runTest {
        coEvery { newsRepository.clearAllArticles(any()) } returns Unit

        useCase(listOf("kotlin", "android"))

        coVerify { newsRepository.clearAllArticles(listOf("kotlin", "android")) }
    }

    @Test
    fun `works correctly with an empty topic list`() = runTest {
        coEvery { newsRepository.clearAllArticles(any()) } returns Unit

        useCase(emptyList())

        coVerify { newsRepository.clearAllArticles(emptyList()) }
    }
}
