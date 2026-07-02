package com.shvarsman.news.presentation.screen.subscriptions

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.shvarsman.news.domain.usecase.AddSubscriptionUseCase
import com.shvarsman.news.domain.usecase.ClearAllArticlesUseCase
import com.shvarsman.news.domain.usecase.GetAllSubscriptionsUseCase
import com.shvarsman.news.domain.usecase.GetArticlesByTopicsUseCase
import com.shvarsman.news.domain.usecase.RemoveSubscriptionUseCase
import com.shvarsman.news.domain.usecase.UpdateSubscribedArticlesUseCase
import com.shvarsman.news.presentation.ui.theme.NewsTheme
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SubscriptionsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val addSubscriptionUseCase: AddSubscriptionUseCase = mockk()
    private val clearAllArticlesUseCase: ClearAllArticlesUseCase = mockk()
    private val getAllSubscriptionsUseCase: GetAllSubscriptionsUseCase = mockk()
    private val getArticlesByTopicsUseCase: GetArticlesByTopicsUseCase = mockk()
    private val removeSubscriptionUseCase: RemoveSubscriptionUseCase = mockk()
    private val updateSubscribedArticlesUseCase: UpdateSubscribedArticlesUseCase = mockk()

    private val subscriptionsFlow = MutableStateFlow<List<String>>(emptyList())

    @Before
    fun setUp() {
        every { getAllSubscriptionsUseCase() } returns subscriptionsFlow
        every { getArticlesByTopicsUseCase(any()) } returns flowOf(emptyList())
        coEvery { addSubscriptionUseCase(any()) } returns Unit
        coEvery { removeSubscriptionUseCase(any()) } returns Unit
        coEvery { clearAllArticlesUseCase(any()) } returns Unit
        coEvery { updateSubscribedArticlesUseCase() } returns emptyList()
    }

    private fun viewModel() = SubscriptionsViewModel(
        addSubscriptionUseCase,
        clearAllArticlesUseCase,
        getAllSubscriptionsUseCase,
        getArticlesByTopicsUseCase,
        removeSubscriptionUseCase,
        updateSubscribedArticlesUseCase
    )

    @Test
    fun emptyState_showsNoSubscriptionsMessage() {
        composeRule.setContent {
            NewsTheme {
                SubscriptionsScreen(onNavigateToSettings = {}, viewModel = viewModel())
            }
        }

        composeRule.onNodeWithText("No subscriptions").assertExists()
    }

    @Test
    fun addSubscriptionButton_isDisabledForEmptyQuery() {
        composeRule.setContent {
            NewsTheme {
                SubscriptionsScreen(onNavigateToSettings = {}, viewModel = viewModel())
            }
        }

        composeRule.onNodeWithTag("add_subscription_button").assertIsNotEnabled()
    }

    @Test
    fun typingTopicAndClickingAdd_callsAddSubscriptionUseCase() {
        composeRule.setContent {
            NewsTheme {
                SubscriptionsScreen(onNavigateToSettings = {}, viewModel = viewModel())
            }
        }

        composeRule.onNodeWithTag("subscription_query_field").performTextInput("kotlin")
        composeRule.onNodeWithTag("add_subscription_button").assertIsEnabled()
        composeRule.onNodeWithTag("add_subscription_button").performClick()

        coVerify(timeout = 2_000) { addSubscriptionUseCase("kotlin") }
    }

    @Test
    fun existingSubscription_isRenderedAsChip() {
        subscriptionsFlow.value = listOf("android")

        composeRule.setContent {
            NewsTheme {
                SubscriptionsScreen(onNavigateToSettings = {}, viewModel = viewModel())
            }
        }

        composeRule.onNodeWithText("android").assertExists()
    }

    @Test
    fun settingsIcon_navigatesToSettings() {
        var navigated = false
        composeRule.setContent {
            NewsTheme {
                SubscriptionsScreen(
                    onNavigateToSettings = { navigated = true },
                    viewModel = viewModel()
                )
            }
        }

        composeRule.onNodeWithTag("settings_icon").performClick()

        assert(navigated) { "onNavigateToSettings should have been invoked" }
    }
}
