package com.shvarsman.news

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shvarsman.news.presentation.MainActivity
import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Smoke-тест: приложение должно запускаться и доходить до RESUMED без падений.
 * Это первый и самый дешёвый сигнал "приложение вообще живо" перед тем, как
 * запускать более детальные UI-тесты конкретных экранов.
 */
@RunWith(AndroidJUnit4::class)
class AppLaunchSmokeTest {
    @Test
    fun mainActivity_launches_and_reaches_resumed_state() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }
}
