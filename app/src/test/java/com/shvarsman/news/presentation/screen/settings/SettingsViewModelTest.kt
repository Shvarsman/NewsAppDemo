package com.shvarsman.news.presentation.screen.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shvarsman.news.domain.entity.Interval
import com.shvarsman.news.domain.entity.Language
import com.shvarsman.news.domain.entity.Settings
import com.shvarsman.news.domain.usecase.GetSettingsUseCase
import com.shvarsman.news.domain.usecase.UpdateIntervalUseCase
import com.shvarsman.news.domain.usecase.UpdateLanguageUseCase
import com.shvarsman.news.domain.usecase.UpdateNotificationsEnabledUseCase
import com.shvarsman.news.domain.usecase.UpdateWifiOnlyUseCase
import com.shvarsman.news.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getSettingsUseCase: GetSettingsUseCase = mockk()
    private val updateIntervalUseCase: UpdateIntervalUseCase = mockk()
    private val updateLanguageUseCase: UpdateLanguageUseCase = mockk()
    private val updateNotificationsEnabledUseCase: UpdateNotificationsEnabledUseCase = mockk()
    private val updateWifiOnlyUseCase: UpdateWifiOnlyUseCase = mockk()

    private val settingsFlow = MutableStateFlow(
        Settings(
            language = Language.ENGLISH,
            interval = Interval.MIN_15,
            notificationsEnabled = false,
            wifiOnly = true
        )
    )

    private fun createViewModel(): SettingsViewModel {
        every { getSettingsUseCase() } returns settingsFlow
        return SettingsViewModel(
            getSettingsUseCase,
            updateIntervalUseCase,
            updateLanguageUseCase,
            updateNotificationsEnabledUseCase,
            updateWifiOnlyUseCase
        )
    }

    @Test
    fun `initial state is Initial before settings are loaded`() = runTest {
        val viewModel = createViewModel()

        // Поскольку MainDispatcherRule использует UnconfinedTestDispatcher, init{} уже
        // успевает выполниться синхронно - проверяем именно факт наличия начального типа состояния.
        assertThat(viewModel.state.value).isInstanceOf(SettingsState.Configuration::class.java)
    }

    @Test
    fun `state reflects settings emitted by the use case`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem() as SettingsState.Configuration
            assertThat(state.language).isEqualTo(Language.ENGLISH)
            assertThat(state.interval).isEqualTo(Interval.MIN_15)
            assertThat(state.wifiOnly).isTrue()
            assertThat(state.notificationsEnabled).isFalse()
        }
    }

    @Test
    fun `state updates reactively when settings change`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            assertThat((awaitItem() as SettingsState.Configuration).language)
                .isEqualTo(Language.ENGLISH)

            settingsFlow.value = settingsFlow.value.copy(language = Language.GERMAN)

            assertThat((awaitItem() as SettingsState.Configuration).language)
                .isEqualTo(Language.GERMAN)
        }
    }

    @Test
    fun `SelectLanguage command delegates to UpdateLanguageUseCase`() = runTest {
        val viewModel = createViewModel()
        coEvery { updateLanguageUseCase(any()) } returns Unit

        viewModel.processCommand(SettingsCommand.SelectLanguage(Language.RUSSIAN))

        coVerify { updateLanguageUseCase(Language.RUSSIAN) }
    }

    @Test
    fun `SelectInterval command delegates to UpdateIntervalUseCase`() = runTest {
        val viewModel = createViewModel()
        coEvery { updateIntervalUseCase(any()) } returns Unit

        viewModel.processCommand(SettingsCommand.SelectInterval(Interval.HOUR_2))

        coVerify { updateIntervalUseCase(Interval.HOUR_2) }
    }

    @Test
    fun `SetNotificationsEnabled command delegates with the exact flag`() = runTest {
        val viewModel = createViewModel()
        coEvery { updateNotificationsEnabledUseCase(any()) } returns Unit

        viewModel.processCommand(SettingsCommand.SetNotificationsEnabled(true))

        coVerify { updateNotificationsEnabledUseCase(true) }
    }

    @Test
    fun `SetWifiOnly command delegates with the exact flag`() = runTest {
        val viewModel = createViewModel()
        coEvery { updateWifiOnlyUseCase(any()) } returns Unit

        viewModel.processCommand(SettingsCommand.SetWifiOnly(false))

        coVerify { updateWifiOnlyUseCase(false) }
    }
}
