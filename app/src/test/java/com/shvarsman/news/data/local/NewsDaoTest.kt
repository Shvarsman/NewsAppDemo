package com.shvarsman.news.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Интеграционный тест: реальная (in-memory) Room БД + реальные SQL-запросы из NewsDao.
 * Прогоняется как обычный unit test (через Robolectric), без необходимости в эмуляторе,
 * поэтому может выполняться в любом CI-окружении на каждый push.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NewsDaoTest {

    private lateinit var database: NewsDatabase
    private lateinit var dao: NewsDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, NewsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.newsDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `adding the same subscription twice is silently ignored`() = runBlocking {
        dao.addSubscription(SubscriptionDbModel("kotlin"))
        dao.addSubscription(SubscriptionDbModel("kotlin"))

        val subscriptions = dao.getAllSubscriptions().first()

        assertThat(subscriptions).hasSize(1)
    }

    @Test
    fun `deleting a subscription cascades to its articles`() = runBlocking {
        dao.addSubscription(SubscriptionDbModel("kotlin"))
        dao.addArticles(listOf(article(topic = "kotlin", url = "https://example.com/1")))

        dao.deleteSubscription(SubscriptionDbModel("kotlin"))

        val remainingArticles = dao.getAllArticlesByTopics(listOf("kotlin")).first()
        assertThat(remainingArticles).isEmpty()
    }

    @Test
    fun `inserting an article for an unknown topic violates the foreign key constraint`() {
        // ArticleDbModel.topic - foreign key на subscriptions.topic.
        // Если кто-то попытается сохранить статью без предварительной подписки
        // (например, из-за гонки между removeSubscription и updateArticlesForTopic),
        // Room должна выбросить исключение, а не молча создать "осиротевшую" запись.
        assertThrows {
            runBlocking {
                dao.addArticles(listOf(article(topic = "no-such-subscription", url = "https://example.com/x")))
            }
        }
    }

    @Test
    fun `articles for unrequested topics are not returned`() = runBlocking {
        dao.addSubscription(SubscriptionDbModel("kotlin"))
        dao.addSubscription(SubscriptionDbModel("android"))
        dao.addArticles(
            listOf(
                article(topic = "kotlin", url = "https://example.com/1"),
                article(topic = "android", url = "https://example.com/2")
            )
        )

        val kotlinArticles = dao.getAllArticlesByTopics(listOf("kotlin")).first()

        assertThat(kotlinArticles).hasSize(1)
        assertThat(kotlinArticles.first().topic).isEqualTo("kotlin")
    }

    @Test
    fun `articles are ordered by publishedAt descending (newest first)`() = runBlocking {
        dao.addSubscription(SubscriptionDbModel("kotlin"))
        dao.addArticles(
            listOf(
                article(topic = "kotlin", url = "https://example.com/old", publishedAt = 1_000L),
                article(topic = "kotlin", url = "https://example.com/new", publishedAt = 2_000L)
            )
        )

        val articles = dao.getAllArticlesByTopics(listOf("kotlin")).first()

        assertThat(articles.map { it.url }).containsExactly(
            "https://example.com/new",
            "https://example.com/old"
        ).inOrder()
    }

    @Test
    fun `deleteArticlesByTopics removes only matching topics`() = runBlocking {
        dao.addSubscription(SubscriptionDbModel("kotlin"))
        dao.addSubscription(SubscriptionDbModel("android"))
        dao.addArticles(
            listOf(
                article(topic = "kotlin", url = "https://example.com/1"),
                article(topic = "android", url = "https://example.com/2")
            )
        )

        dao.deleteArticlesByTopics(listOf("kotlin"))

        assertThat(dao.getAllArticlesByTopics(listOf("kotlin")).first()).isEmpty()
        assertThat(dao.getAllArticlesByTopics(listOf("android")).first()).hasSize(1)
    }

    private fun article(
        topic: String,
        url: String,
        publishedAt: Long = 0L
    ) = ArticleDbModel(
        title = "title",
        description = "description",
        imageUrl = null,
        sourceName = "source",
        publishedAt = publishedAt,
        url = url,
        topic = topic
    )

    private fun assertThrows(block: () -> Unit) {
        var thrown: Throwable? = null
        try {
            block()
        } catch (t: Throwable) {
            thrown = t
        }
        assertThat(thrown).isNotNull()
    }
}
