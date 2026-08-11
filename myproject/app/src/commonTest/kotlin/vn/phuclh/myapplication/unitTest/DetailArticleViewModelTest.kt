package vn.phuclh.myapplication.unitTest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import vn.phuclh.myapplication.domain.usecase.ToggleBookmarkUseCase
import vn.phuclh.myapplication.presentation.detail.DetailUiState
import vn.phuclh.myapplication.presentation.detail.DetailViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DetailArticleViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeArticleRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = FakeArticleRepository()
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(url: String = "test-url") =
        DetailViewModel(
            repository = repository,
            toggleBookmarkUseCase = ToggleBookmarkUseCase(repository),
            articleUrl = url,
        )

    @Test
    fun `init emits Success when article exists in repository`() =
        runTest(dispatcher) {
            // Data phải có SẴN trước khi build VM, vì init đọc repo ngay
            repository.articlesFlow.value = listOf(article("test-url"))

            val viewModel = buildViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is DetailUiState.Success)
            assertEquals("test-url", (state as DetailUiState.Success).article.url)
        }

    @Test
    fun `init emits Error when article not found`() =
        runTest(dispatcher) {
            // repo rỗng -> getArticleByUrl trả null -> Error
            val viewModel = buildViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is DetailUiState.Error)
            assertEquals("Article not found", (state as DetailUiState.Error).message)
        }

    @Test
    fun `initial state is Loading before coroutine runs`() =
        runTest(dispatcher) {
            repository.articlesFlow.value = listOf(article("test-url"))

            val viewModel = buildViewModel()
            // KHÔNG advanceUntilIdle -> loadArticle chưa chạy xong

            assertTrue(viewModel.uiState.value is DetailUiState.Loading)
        }

    @Test
    fun `toggleBookmark delegates to repository with negated flag`() =
        runTest(dispatcher) {
            // Bài đang chưa bookmark -> toggle phải gửi true
            repository.articlesFlow.value = listOf(article("test-url", isBookmarked = false))
            val viewModel = buildViewModel()
            advanceUntilIdle()

            viewModel.toggleBookmark()
            advanceUntilIdle()

            assertEquals(listOf("test-url" to true), repository.toggledBookmarks)
        }

    @Test
    fun `toggleBookmark does nothing when state is not Success`() =
        runTest(dispatcher) {
            // repo rỗng -> state là Error, không phải Success -> guard clause chặn
            val viewModel = buildViewModel()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value is DetailUiState.Error)

            viewModel.toggleBookmark()
            advanceUntilIdle()

            assertTrue(repository.toggledBookmarks.isEmpty())
        }
}
