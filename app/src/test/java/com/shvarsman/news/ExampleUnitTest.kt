package com.shvarsman.news

import com.shvarsman.news.data.mapper.toRefreshConfig
import com.shvarsman.news.domain.entity.Interval
import com.shvarsman.news.domain.entity.Language
import com.shvarsman.news.domain.entity.Settings
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun settings_are_mapped_to_refresh_config_correctly() {
        val settings = Settings(
            language = Language.FRENCH,
            interval = Interval.HOUR_4,
            notificationsEnabled = true,
            wifiOnly = false
        )

        val refreshConfig = settings.toRefreshConfig()

        assertEquals(settings.language, refreshConfig.language)
        assertEquals(settings.interval, refreshConfig.interval)
        assertEquals(settings.wifiOnly, refreshConfig.wifiOnly)
    }
}