package vn.phuclh.myapplication.unitTest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import vn.phuclh.myapplication.domain.usecase.GetArticlesUseCase
import vn.phuclh.myapplication.domain.usecase.MarkArticlesSeenUseCase
import vn.phuclh.myapplication.domain.usecase.RefreshArticlesUseCase
import vn.phuclh.myapplication.domain.usecase.ToggleBookmarkUseCase
import vn.phuclh.myapplication.presentation.articles.ArticlesIntent
import vn.phuclh.myapplication.presentation.articles.ArticlesViewModel

/**
 * Fake thay cho Mockito: implement thẳng interface repository.
 * Gọn hơn mock framework, không cần bytecode magic, và chạy được trên KMP.
 * Vừa đóng vai Stub (trả dữ liệu định sẵn) vừa đóng vai Mock (ghi lại lời gọi để verify).
 */

@OptIn(ExperimentalCoroutinesApi::class)
class ArticlesViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeArticleRepository

    @Before
    fun setup() {
        // viewModelScope chạy trên Dispatchers.Main -> phải thay bằng test dispatcher
        Dispatchers.setMain(dispatcher)
        repository = FakeArticleRepository()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() =
        ArticlesViewModel(
            getArticlesUseCase = GetArticlesUseCase(repository),
            refreshArticlesUseCase = RefreshArticlesUseCase(repository),
            toggleBookmarkUseCase = ToggleBookmarkUseCase(repository),
            markArticlesSeenUseCase = MarkArticlesSeenUseCase(repository),
        )

    @Test
    fun `init refreshes and emits articles from repository`() =
        runTest(dispatcher) {
            val viewModel = buildViewModel()
            repository.articlesFlow.value = listOf(article("a"), article("b"))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(2, state.articles.size)
            assertFalse(state.isLoading)
            assertEquals(1, repository.refreshCallCount)
        }

    @Test
    fun `uiState updates when repository flow emits new articles`() =
        runTest(dispatcher) {
            val viewModel = buildViewModel()
            advanceUntilIdle()
            assertTrue(
                viewModel.uiState.value.articles
                    .isEmpty(),
            )

            repository.articlesFlow.value = listOf(article("new"))
            advanceUntilIdle()

            assertEquals(
                "new",
                viewModel.uiState.value.articles
                    .single()
                    .url,
            )
        }

    @Test
    fun `RefreshData intent calls refresh and clears refreshing flag`() =
        runTest(dispatcher) {
            val viewModel = buildViewModel()
            advanceUntilIdle()
            val callsAfterInit = repository.refreshCallCount

            viewModel.handleIntent(ArticlesIntent.RefreshData)
            advanceUntilIdle()

            assertEquals(callsAfterInit + 1, repository.refreshCallCount)
            assertFalse(viewModel.uiState.value.isRefreshing)
            assertNull(viewModel.uiState.value.error)
        }

    @Test
    fun `refresh failure exposes error message in uiState`() =
        runTest(dispatcher) {
            repository.refreshResult = Result.failure(RuntimeException("network down"))

            val viewModel = buildViewModel()
            advanceUntilIdle()

            assertEquals("network down", viewModel.uiState.value.error)
            assertFalse(viewModel.uiState.value.isRefreshing)
        }

    /**
     * Đây chính là case bắt được bug `ToggleBookmark -> Unit` trước đó:
     * intent được gửi nhưng ViewModel không gọi xuống use case.
     */
    @Test
    fun `ToggleBookmark intent delegates to repository with correct args`() =
        runTest(dispatcher) {
            val viewModel = buildViewModel()
            advanceUntilIdle()

            viewModel.handleIntent(ArticlesIntent.ToggleBookmark("http://x", true))
            advanceUntilIdle()

            assertEquals(listOf("http://x" to true), repository.toggledBookmarks)
        }

    @Test
    fun `MarkAllArticlesSeen intent delegates to repository`() =
        runTest(dispatcher) {
            val viewModel = buildViewModel()
            advanceUntilIdle()

            viewModel.handleIntent(ArticlesIntent.MarkAllArticlesSeen)
            advanceUntilIdle()

            assertEquals(1, repository.markAllSeenCallCount)
        }
}
