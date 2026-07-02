package com.shvarsman.news.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shvarsman.news.domain.entity.Article
import com.shvarsman.news.domain.repository.NewsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetArticlesByTopicsUseCaseTest {

    private val newsRepository: NewsRepository = mockk()
    private val useCase = GetArticlesByTopicsUseCase(newsRepository)

    @Test
    fun `emits articles returned by the repository for given topics`() = runTest {
        val articles = listOf(
            Article(
                title = "Title",
                description = "Description",
                imageUrl = null,
                sourceName = "Source",
                publishedAt = 0L,
                url = "https://example.com"
            )
        )
        every { newsRepository.getArticlesByTopics(listOf("kotlin")) } returns flowOf(articles)

        useCase(listOf("kotlin")).test {
            assertThat(awaitItem()).isEqualTo(articles)
            awaitComplete()
        }
    }

    @Test
    fun `emits empty list when there are no topics selected`() = runTest {
        every { newsRepository.getArticlesByTopics(emptyList()) } returns flowOf(emptyList())

        useCase(emptyList()).test {
            assertThat(awaitItem()).isEmpty()
            awaitComplete()
        }
    }
}
