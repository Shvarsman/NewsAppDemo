package com.shvarsman.news.data.mapper

import com.google.common.truth.Truth.assertThat
import com.shvarsman.news.domain.entity.Interval
import com.shvarsman.news.domain.entity.Language
import com.shvarsman.news.domain.entity.Settings
import org.junit.Test

class SettingsMapperTest {

    @Test
    fun `settings are mapped to refresh config correctly`() {
        val settings = Settings(
            language = Language.FRENCH,
            interval = Interval.HOUR_4,
            notificationsEnabled = true,
            wifiOnly = false
        )

        val refreshConfig = settings.toRefreshConfig()

        assertThat(refreshConfig.language).isEqualTo(settings.language)
        assertThat(refreshConfig.interval).isEqualTo(settings.interval)
        assertThat(refreshConfig.wifiOnly).isEqualTo(settings.wifiOnly)
    }

    @Test
    fun `notificationsEnabled is intentionally dropped from refresh config`() {
        // RefreshConfig умышленно не содержит notificationsEnabled - это поле
        // используется только в RefreshDataWorker отдельно. Тест фиксирует это
        // как осознанное поведение, чтобы случайное добавление поля не осталось незамеченным.
        val settings = Settings(
            language = Language.ENGLISH,
            interval = Interval.MIN_15,
            notificationsEnabled = true,
            wifiOnly = true
        )

        val refreshConfig = settings.toRefreshConfig()

        assertThat(refreshConfig::class.java.declaredFields.map { it.name })
            .doesNotContain("notificationsEnabled")
    }
}
