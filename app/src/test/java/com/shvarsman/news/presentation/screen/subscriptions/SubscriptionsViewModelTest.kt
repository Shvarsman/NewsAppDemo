package com.shvarsman.news.presentation.screen.subscriptions

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shvarsman.news.domain.entity.Article
import com.shvarsman.news.domain.usecase.AddSubscriptionUseCase
import com.shvarsman.news.domain.usecase.ClearAllArticlesUseCase
import com.shvarsman.news.domain.usecase.GetAllSubscriptionsUseCase
import com.shvarsman.news.domain.usecase.GetArticlesByTopicsUseCase
import com.shvarsman.news.domain.usecase.RemoveSubscriptionUseCase
import com.shvarsman.news.domain.usecase.UpdateSubscribedArticlesUseCase
import com.shvarsman.news.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SubscriptionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val addSubscriptionUseCase: AddSubscriptionUseCase = mockk()
    private val clearAllArticlesUseCase: ClearAllArticlesUseCase = mockk()
    private val getAllSubscriptionsUseCase: GetAllSubscriptionsUseCase = mockk()
    private val getArticlesByTopicsUseCase: GetArticlesByTopicsUseCase = mockk()
    private val removeSubscriptionUseCase: RemoveSubscriptionUseCase = mockk()
    private val updateSubscribedArticlesUseCase: UpdateSubscribedArticlesUseCase = mockk()

    private val subscriptionsFlow = MutableStateFlow<List<String>>(emptyList())

    private fun createViewModel(): SubscriptionsViewModel {
        every { getAllSubscriptionsUseCase() } returns subscriptionsFlow
        every { getArticlesByTopicsUseCase(any()) } returns flowOf(emptyList())
        return SubscriptionsViewModel(
            addSubscriptionUseCase,
            clearAllArticlesUseCase,
            getAllSubscriptionsUseCase,
            getArticlesByTopicsUseCase,
            removeSubscriptionUseCase,
            updateSubscribedArticlesUseCase
        )
    }

    @Test
    fun `InputTopic updates the query in state`() = runTest {
        val viewModel = createViewModel()

        viewModel.processCommand(SubscriptionsCommand.InputTopic("kotlin"))

        assertThat(viewModel.state.value.query).isEqualTo("kotlin")
    }

    @Test
    fun `subscribe button is disabled for a blank query`() = runTest {
        val viewModel = createViewModel()

        viewModel.processCommand(SubscriptionsCommand.InputTopic("   "))

        assertThat(viewModel.state.value.subscribeButtonEnabled).isFalse()
    }

    @Test
    fun `ClickSubscribe adds a trimmed topic and clears the input`() = runTest {
        val viewModel = createViewModel()
        coEvery { addSubscriptionUseCase(any()) } returns Unit
        viewModel.processCommand(SubscriptionsCommand.InputTopic("  kotlin  "))

        viewModel.processCommand(SubscriptionsCommand.ClickSubscribe)

        coVerify { addSubscriptionUseCase("kotlin") }
        assertThat(viewModel.state.value.query).isEmpty()
    }

    @Test
    fun `newly observed subscriptions are selected by default`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            assertThat(awaitItem().subscriptions).isEmpty()

            subscriptionsFlow.value = listOf("kotlin")

            val state = awaitItem()
            assertThat(state.subscriptions).containsExactly("kotlin", true)
        }
    }

    @Test
    fun `ToggleTopicSelection flips the selection flag for that topic`() = runTest {
        val viewModel = createViewModel()
        subscriptionsFlow.value = listOf("kotlin")

        viewModel.state.test {
            var state = awaitItem()
            while (state.subscriptions.isEmpty()) {
                state = awaitItem()
            }
            assertThat(state.subscriptions["kotlin"]).isTrue()

            viewModel.processCommand(SubscriptionsCommand.ToggleTopicSelection("kotlin"))

            val updated = awaitItem()
            assertThat(updated.subscriptions["kotlin"]).isFalse()
        }
    }

    @Test
    fun `RemoveSubscription delegates to the use case with the exact topic`() = runTest {
        val viewModel = createViewModel()
        coEvery { removeSubscriptionUseCase(any()) } returns Unit

        viewModel.processCommand(SubscriptionsCommand.RemoveSubscription("kotlin"))

        coVerify { removeSubscriptionUseCase("kotlin") }
    }

    @Test
    fun `ClearArticles clears only the currently selected topics`() = runTest {
        val viewModel = createViewModel()
        coEvery { clearAllArticlesUseCase(any()) } returns Unit
        subscriptionsFlow.value = listOf("kotlin", "android")

        viewModel.state.test {
            var state = awaitItem()
            while (state.subscriptions.size < 2) {
                state = awaitItem()
            }
            cancelAndIgnoreRemainingEvents()
        }
        // android отписан от выборки, kotlin остаётся выбранным
        viewModel.processCommand(SubscriptionsCommand.ToggleTopicSelection("android"))

        viewModel.processCommand(SubscriptionsCommand.ClearArticles)

        coVerify { clearAllArticlesUseCase(listOf("kotlin")) }
    }

    @Test
    fun `RefreshData triggers UpdateSubscribedArticlesUseCase`() = runTest {
        val viewModel = createViewModel()
        coEvery { updateSubscribedArticlesUseCase() } returns listOf("kotlin")

        viewModel.processCommand(SubscriptionsCommand.RefreshData)

        coVerify { updateSubscribedArticlesUseCase() }
    }

    @Test
    fun `articles are refreshed when the set of selected topics changes`() = runTest {
        val article = Article(
            title = "t",
            description = "d",
            imageUrl = null,
            sourceName = "s",
            publishedAt = 0L,
            url = "u"
        )
        every { getAllSubscriptionsUseCase() } returns subscriptionsFlow
        every { getArticlesByTopicsUseCase(emptyList()) } returns flowOf(emptyList())
        every { getArticlesByTopicsUseCase(listOf("kotlin")) } returns flowOf(listOf(article))

        val viewModel = SubscriptionsViewModel(
            addSubscriptionUseCase,
            clearAllArticlesUseCase,
            getAllSubscriptionsUseCase,
            getArticlesByTopicsUseCase,
            removeSubscriptionUseCase,
            updateSubscribedArticlesUseCase
        )

        subscriptionsFlow.value = listOf("kotlin")

        assertThat(viewModel.state.value.articles).containsExactly(article)
    }
}
