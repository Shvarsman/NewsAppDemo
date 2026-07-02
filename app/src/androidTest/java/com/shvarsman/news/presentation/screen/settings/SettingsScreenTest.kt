package com.shvarsman.news.presentation.screen.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.shvarsman.news.domain.entity.Interval
import com.shvarsman.news.domain.entity.Language
import com.shvarsman.news.domain.entity.Settings
import com.shvarsman.news.domain.usecase.GetSettingsUseCase
import com.shvarsman.news.domain.usecase.UpdateIntervalUseCase
import com.shvarsman.news.domain.usecase.UpdateLanguageUseCase
import com.shvarsman.news.domain.usecase.UpdateNotificationsEnabledUseCase
import com.shvarsman.news.domain.usecase.UpdateWifiOnlyUseCase
import com.shvarsman.news.presentation.ui.theme.NewsTheme
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI-тесты экрана настроек. ViewModel создаётся напрямую (с замоканными UseCase),
 * без Hilt-инфраструктуры - это позволяет тестировать реальное связывание
 * Compose UI <-> ViewModel <-> UseCase без накладных расходов на DI-тестовый рантайм.
 */
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val getSettingsUseCase: GetSettingsUseCase = mockk()
    private val updateIntervalUseCase: UpdateIntervalUseCase = mockk()
    private val updateLanguageUseCase: UpdateLanguageUseCase = mockk()
    private val updateNotificationsEnabledUseCase: UpdateNotificationsEnabledUseCase = mockk()
    private val updateWifiOnlyUseCase: UpdateWifiOnlyUseCase = mockk()

    @Before
    fun setUp() {
        every { getSettingsUseCase() } returns flowOf(
            Settings(
                language = Language.ENGLISH,
                interval = Interval.MIN_15,
                notificationsEnabled = false,
                wifiOnly = true
            )
        )
        coEvery { updateLanguageUseCase(any()) } returns Unit
        coEvery { updateIntervalUseCase(any()) } returns Unit
        coEvery { updateNotificationsEnabledUseCase(any()) } returns Unit
        coEvery { updateWifiOnlyUseCase(any()) } returns Unit
    }

    private fun viewModel() = SettingsViewModel(
        getSettingsUseCase,
        updateIntervalUseCase,
        updateLanguageUseCase,
        updateNotificationsEnabledUseCase,
        updateWifiOnlyUseCase
    )

    private fun setScreen() {
        composeRule.setContent {
            NewsTheme {
                SettingsScreen(onBackClick = {}, viewModel = viewModel())
            }
        }
    }

    @Test
    fun settingsScreen_showsCurrentLanguageAndInterval() {
        setScreen()

        composeRule.onNodeWithText("English").assertExists()
        composeRule.onNodeWithText("15 minutes").assertExists()
    }

    @Test
    fun selectingLanguageFromDropdown_updatesSettingsViaUseCase() {
        setScreen()

        // Открываем выпадающий список языка и выбираем русский
        composeRule.onNodeWithText("English").performClick()
        composeRule.onNodeWithText("Русский").performClick()

        coVerify(timeout = 2_000) { updateLanguageUseCase(Language.RUSSIAN) }
    }

    @Test
    fun selectingIntervalFromDropdown_updatesSettingsViaUseCase() {
        setScreen()

        composeRule.onNodeWithText("15 minutes").performClick()
        composeRule.onNodeWithText("1 hour").performClick()

        coVerify(timeout = 2_000) { updateIntervalUseCase(Interval.HOUR_1) }
    }

    @Test
    fun togglingWifiOnlySwitch_updatesSettingsViaUseCase() {
        setScreen()

        composeRule.onNodeWithTag("wifi_only_switch").assertExists()
        composeRule.onNodeWithTag("wifi_only_switch").performClick()

        coVerify(timeout = 2_000) { updateWifiOnlyUseCase(false) }
    }

    @Test
    fun backButton_invokesOnBackClickCallback() {
        var backClicked = false
        composeRule.setContent {
            NewsTheme {
                SettingsScreen(onBackClick = { backClicked = true }, viewModel = viewModel())
            }
        }

        composeRule.onNodeWithContentDescription("Back").performClick()

        assert(backClicked) { "onBackClick should have been invoked" }
    }
}
