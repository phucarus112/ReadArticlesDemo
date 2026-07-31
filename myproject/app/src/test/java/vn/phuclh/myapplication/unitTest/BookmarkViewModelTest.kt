package vn.phuclh.myapplication.unitTest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import vn.phuclh.myapplication.domain.usecase.GetBookmarksUseCase
import vn.phuclh.myapplication.domain.usecase.ToggleBookmarkUseCase
import vn.phuclh.myapplication.presentation.bookmarks.BookmarkViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class BookmarkViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeArticleRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = FakeArticleRepository()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() =
        BookmarkViewModel(
            getBookmarksUseCase = GetBookmarksUseCase(repository),
            toggleBookmarkUseCase = ToggleBookmarkUseCase(repository),
        )

    @Test
    fun `uiState exposes bookmarks from repository`() =
        runTest(dispatcher) {
            // Nguồn dữ liệu thật: observeBookmarks() -> articlesFlow
            repository.articlesFlow.value = listOf(article("a"), article("b"))
            val viewModel = buildViewModel()

            // stateIn dùng WhileSubscribed -> phải có subscriber thì flow mới chạy
            backgroundScope.launch(dispatcher) { viewModel.uiState.collect { } }
            advanceUntilIdle()

            assertEquals(2, viewModel.uiState.value.bookmarks.size)
        }

    @Test
    fun `uiState is empty when repository has no bookmarks`() =
        runTest(dispatcher) {
            val viewModel = buildViewModel()

            backgroundScope.launch(dispatcher) { viewModel.uiState.collect { } }
            advanceUntilIdle()

            assertTrue(
                viewModel.uiState.value.bookmarks
                    .isEmpty(),
            )
        }

    @Test
    fun `removeBookmark delegates to repository with isBookmarked false`() =
        runTest(dispatcher) {
            repository.articlesFlow.value = listOf(article("a"), article("b"))
            val viewModel = buildViewModel()
            advanceUntilIdle()

            viewModel.removeBookmark("b")
            advanceUntilIdle()

            // Kiểm tra ĐÚNG lời gọi do removeBookmark tạo ra: (url, false)
            assertEquals(listOf("b" to false), repository.toggledBookmarks)
        }
}
