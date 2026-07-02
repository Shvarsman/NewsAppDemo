package com.shvarsman.news.data.repository

import androidx.work.WorkManager
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shvarsman.news.data.local.ArticleDbModel
import com.shvarsman.news.data.local.NewsDao
import com.shvarsman.news.data.local.SubscriptionDbModel
import com.shvarsman.news.data.remote.ArticleDto
import com.shvarsman.news.data.remote.NewsApiService
import com.shvarsman.news.data.remote.NewsResponseDto
import com.shvarsman.news.domain.entity.Interval
import com.shvarsman.news.domain.entity.Language
import com.shvarsman.news.domain.entity.RefreshConfig
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NewsRepositoryImplTest {

    private val newsDao: NewsDao = mockk()
    private val newsApiService: NewsApiService = mockk()
    private val workManager: WorkManager = mockk(relaxed = true)

    private lateinit var repository: NewsRepositoryImpl

    @Before
    fun setUp() {
        repository = NewsRepositoryImpl(newsDao, newsApiService, workManager)
    }

    @Test
    fun `getAllSubscriptions maps db models to plain topic strings`() = runTest {
        every { newsDao.getAllSubscriptions() } returns flowOf(
            listOf(SubscriptionDbModel("kotlin"), SubscriptionDbModel("android"))
        )

        repository.getAllSubscriptions().test {
            assertThat(awaitItem()).containsExactly("kotlin", "android")
            awaitComplete()
        }
    }

    @Test
    fun `addSubscription stores a new subscription row`() = runTest {
        coEvery { newsDao.addSubscription(any()) } returns Unit

        repository.addSubscription("kotlin")

        coVerify { newsDao.addSubscription(SubscriptionDbModel("kotlin")) }
    }

    @Test
    fun `updateArticlesForTopic returns true when at least one new article was inserted`() = runTest {
        coEvery { newsApiService.loadArticles("kotlin", "en") } returns NewsResponseDto(
            articles = listOf(ArticleDto(title = "t", url = "u"))
        )
        coEvery { newsDao.addArticles(any()) } returns listOf(1L)

        val updated = repository.updateArticlesForTopic("kotlin", Language.ENGLISH)

        assertThat(updated).isTrue()
    }

    @Test
    fun `updateArticlesForTopic returns false when all articles were already known`() = runTest {
        // Room возвращает -1 для строк, проигнорированных из-за OnConflictStrategy.IGNORE
        coEvery { newsApiService.loadArticles(any(), any()) } returns NewsResponseDto(
            articles = listOf(ArticleDto(title = "t", url = "u"))
        )
        coEvery { newsDao.addArticles(any()) } returns listOf(-1L)

        val updated = repository.updateArticlesForTopic("kotlin", Language.ENGLISH)

        assertThat(updated).isFalse()
    }

    @Test
    fun `BUG-005 consequence - a single malformed article discards the whole topic batch, not just itself`() = runTest {
        // toDbModels() бросает ParseException на статье с "битым" publishedAt (см. BUG-005 в
        // NewsMapperTest). Здесь, на уровне репозитория, это исключение перехватывается общим
        // try-catch(Exception) в loadArticles() - но ценой этого является то, что ВСЕ статьи
        // из ответа (включая корректные) отбрасываются целиком, а не только повреждённая.
        coEvery { newsApiService.loadArticles(any(), any()) } returns NewsResponseDto(
            articles = listOf(
                ArticleDto(title = "valid article", url = "https://example.com/ok", publishedAt = "2024-05-01T10:15:00Z"),
                ArticleDto(title = "broken article", url = "https://example.com/broken", publishedAt = "not-a-date")
            )
        )
        coEvery { newsDao.addArticles(emptyList()) } returns emptyList()

        val updated = repository.updateArticlesForTopic("kotlin", Language.ENGLISH)

        assertThat(updated).isFalse()
        // ни одна статья не сохранена - даже валидная "valid article" потеряна из-за соседней "broken article"
        coVerify { newsDao.addArticles(emptyList()) }
    }

    @Test
    fun `updateArticlesForTopic does not crash when the API call fails`() = runTest {
        // Сеть недоступна / ошибка сервера - не должно приводить к падению юзкейса,
        // а должно тихо деградировать до "новых статей нет".
        coEvery { newsApiService.loadArticles(any(), any()) } throws java.io.IOException("network error")
        coEvery { newsDao.addArticles(emptyList()) } returns emptyList()

        val updated = repository.updateArticlesForTopic("kotlin", Language.ENGLISH)

        assertThat(updated).isFalse()
        coVerify { newsDao.addArticles(emptyList()) }
    }

    @Test
    fun `getArticlesByTopics maps db articles to domain entities`() = runTest {
        every { newsDao.getAllArticlesByTopics(listOf("kotlin")) } returns flowOf(
            listOf(
                ArticleDbModel(
                    title = "t",
                    description = "d",
                    imageUrl = null,
                    sourceName = "s",
                    publishedAt = 1L,
                    url = "u",
                    topic = "kotlin"
                )
            )
        )

        repository.getArticlesByTopics(listOf("kotlin")).test {
            val articles = awaitItem()
            assertThat(articles).hasSize(1)
            assertThat(articles.first().title).isEqualTo("t")
            awaitComplete()
        }
    }

    @Test
    fun `clearAllArticles delegates to dao with the given topics`() = runTest {
        coEvery { newsDao.deleteArticlesByTopics(any()) } returns Unit

        repository.clearAllArticles(listOf("kotlin"))

        coVerify { newsDao.deleteArticlesByTopics(listOf("kotlin")) }
    }

    @Test
    fun `startBackgroundRefresh enqueues a unique periodic work request`() {
        repository.startBackgroundRefresh(
            RefreshConfig(language = Language.ENGLISH, interval = Interval.HOUR_1, wifiOnly = true)
        )

        verify {
            workManager.enqueueUniquePeriodicWork(
                "Refresh data",
                androidx.work.ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                any()
            )
        }
    }

    @Test
    fun `updateArticlesForAllSubscriptions returns only topics that received new articles`() = runTest {
        every { newsDao.getAllSubscriptions() } returns flowOf(
            listOf(SubscriptionDbModel("kotlin"), SubscriptionDbModel("android"))
        )
        coEvery { newsApiService.loadArticles("kotlin", "en") } returns NewsResponseDto(
            articles = listOf(
                ArticleDto(title = "new", url = "u1", publishedAt = "2024-05-01T10:15:00Z")
            )
        )
        coEvery { newsApiService.loadArticles("android", "en") } returns NewsResponseDto(
            articles = emptyList()
        )
        coEvery { newsDao.addArticles(match { it.isNotEmpty() }) } returns listOf(1L)
        coEvery { newsDao.addArticles(emptyList()) } returns emptyList()

        val updatedTopics = repository.updateArticlesForAllSubscriptions(Language.ENGLISH)

        assertThat(updatedTopics).containsExactly("kotlin")
    }
}