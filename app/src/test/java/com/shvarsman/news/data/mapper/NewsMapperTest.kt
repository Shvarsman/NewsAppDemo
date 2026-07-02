package com.shvarsman.news.data.mapper

import com.google.common.truth.Truth.assertThat
import com.shvarsman.news.data.local.ArticleDbModel
import com.shvarsman.news.data.remote.ArticleDto
import com.shvarsman.news.data.remote.NewsResponseDto
import com.shvarsman.news.data.remote.SourceDto
import com.shvarsman.news.domain.entity.Interval
import com.shvarsman.news.domain.entity.Language
import org.junit.Test

class NewsMapperTest {

    // --- Language.toQueryParam ---

    @Test
    fun `each language maps to its correct ISO query param`() {
        assertThat(Language.ENGLISH.toQueryParam()).isEqualTo("en")
        assertThat(Language.RUSSIAN.toQueryParam()).isEqualTo("ru")
        assertThat(Language.FRENCH.toQueryParam()).isEqualTo("fr")
        assertThat(Language.GERMAN.toQueryParam()).isEqualTo("de")
    }

    // --- Int.toInterval ---
    // См. BUG_REPORTS.md, BUG-001.
    // SettingsRepositoryImpl читает интервал из DataStore так:
    //   preferences[intervalKey]?.toInterval() ?: Settings.DEFAULT_INTERVAL
    // Elvis-оператор НЕ спасает от падения, потому что toInterval() не возвращает null,
    // а бросает исключение, если значение minutes не совпадает ни с одним Interval.
    // Это воспроизводимо, например, если в DataStore лежит "битое"/устаревшее значение
    // после изменения списка допустимых интервалов в будущей версии приложения.

    @Test
    fun `toInterval maps known minute values correctly`() {
        assertThat(15.toInterval()).isEqualTo(Interval.MIN_15)
        assertThat(60.toInterval()).isEqualTo(Interval.HOUR_1)
        assertThat(1440.toInterval()).isEqualTo(Interval.HOUR_24)
    }

    @Test(expected = NoSuchElementException::class)
    fun `BUG-001 - toInterval throws on unknown minute value instead of falling back`() {
        // Любое значение, не входящее в Interval.entries (например, оставшееся
        // от удалённого варианта или повреждённое), приводит к падению приложения
        // при каждом запуске, а не к мягкому возврату дефолтного интервала.
        42.toInterval()
    }

    @Test(expected = NoSuchElementException::class)
    fun `BUG-001 repro - a different unknown value also fails the same way`() {
        7.toInterval()
    }

    // --- NewsResponseDto.toDbModels ---

    @Test
    fun `toDbModels maps remote articles to db models for given topic`() {
        val dto = NewsResponseDto(
            articles = listOf(
                ArticleDto(
                    description = "desc",
                    publishedAt = "2024-05-01T10:15:00Z",
                    source = SourceDto(name = "BBC"),
                    title = "Title 1",
                    url = "https://example.com/1",
                    urlToImage = "https://example.com/1.png"
                )
            )
        )

        val result = dto.toDbModels(topic = "kotlin")

        assertThat(result).hasSize(1)
        with(result.first()) {
            assertThat(title).isEqualTo("Title 1")
            assertThat(description).isEqualTo("desc")
            assertThat(url).isEqualTo("https://example.com/1")
            assertThat(imageUrl).isEqualTo("https://example.com/1.png")
            assertThat(sourceName).isEqualTo("BBC")
            assertThat(topic).isEqualTo("kotlin")
        }
    }

    @Test
    fun `BUG-002 - publishedAt is parsed using the local timezone even though the API returns UTC`() {
        // NewsMapper.toTimestamp() использует паттерн "yyyy-MM-dd'T'HH:mm:ss'Z'".
        // 'Z' здесь в кавычках, то есть это буквальный символ, а НЕ обозначение
        // часового пояса UTC. SimpleDateFormat в результате парсит время как
        // локальное (TimeZone.getDefault()), хотя NewsAPI всегда отдаёт время в UTC.
        // Следствие: на устройстве с часовым поясом, отличным от UTC, дата публикации
        // статьи будет смещена на величину UTC-офсета пользователя, что ломает
        // сортировку "ORDER BY publishedAt DESC" и отображаемую дату на карточке статьи.
        val rawDate = "2024-05-01T10:15:00Z"
        val dto = NewsResponseDto(
            articles = listOf(ArticleDto(publishedAt = rawDate, title = "t", url = "u"))
        )

        val actual = dto.toDbModels(topic = "kotlin").first().publishedAt

        // Эталон: то время, которое получилось бы при ПРАВИЛЬНОМ, timezone-aware
        // парсинге как UTC (используя стандартный Instant.parse).
        val correctUtcMillis = java.time.Instant.parse(rawDate).toEpochMilli()

        // Текущая реализация в большинстве часовых поясов (кроме UTC) даст другое
        // значение. Если default timezone окружения - UTC, оба значения совпадут,
        // и баг "молчит" - поэтому мы проверяем именно через сравнение с офсетом,
        // а не полагаемся только на равенство.
        val expectedBuggyMillis = correctUtcMillis -
                java.util.TimeZone.getDefault().getOffset(correctUtcMillis)

        assertThat(actual).isEqualTo(expectedBuggyMillis)
    }

    @Test(expected = java.text.ParseException::class)
    fun `BUG-005 - toDbModels throws ParseException for a malformed publishedAt instead of falling back`() {
        // На первый взгляд код выглядит как safe fallback:
        //   dateFormatter.parse(this)?.time ?: System.currentTimeMillis()
        // Но однопараметрический SimpleDateFormat.parse(String) НИКОГДА не возвращает null -
        // при нераспознанной строке он бросает ParseException. Значит "?.time ?: ..." - это
        // мёртвый код, который создаёт ложное ощущение защищённости от плохих данных.
        // На уровне NewsRepositoryImpl.loadArticles() это исключение будет перехвачено общим
        // try/catch(Exception) - но ценой этого является то, что ОДНА статья с "битой" датой
        // приводит к отбрасыванию ВСЕХ статей по теме за этот цикл обновления, а не только её одной.
        val dto = NewsResponseDto(
            articles = listOf(ArticleDto(publishedAt = "not-a-date", title = "Broken date article", url = "https://example.com/broken"))
        )

        dto.toDbModels(topic = "news")
    }

    @Test
    fun `toDbModels returns empty list for empty response`() {
        val result = NewsResponseDto(articles = emptyList()).toDbModels(topic = "kotlin")

        assertThat(result).isEmpty()
    }

    // --- List<ArticleDbModel>.toEntities ---

    @Test
    fun `toEntities removes fully duplicate articles`() {
        val article = ArticleDbModel(
            title = "Same article",
            description = "desc",
            imageUrl = null,
            sourceName = "Source",
            publishedAt = 1_000L,
            url = "https://example.com/article",
            topic = "kotlin"
        )
        // Одна и та же статья сохранена под двумя разными топиками подписки -
        // в БД это два разных primary key (url, topic), но как Article для UI
        // (без поля topic) они идентичны и должны схлопнуться в одну карточку.
        val sameArticleDifferentTopic = article.copy(topic = "android")

        val result = listOf(article, sameArticleDifferentTopic).toEntities()

        assertThat(result).hasSize(1)
    }

    @Test
    fun `toEntities keeps articles with different content`() {
        val first = ArticleDbModel(
            title = "First",
            description = "desc",
            imageUrl = null,
            sourceName = "Source",
            publishedAt = 1_000L,
            url = "https://example.com/1",
            topic = "kotlin"
        )
        val second = first.copy(title = "Second", url = "https://example.com/2")

        val result = listOf(first, second).toEntities()

        assertThat(result).hasSize(2)
    }
}