package vn.phuclh.myapplication.presentation.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.phuclh.myapplication.domain.usecase.GetBookmarksUseCase
import vn.phuclh.myapplication.domain.usecase.ToggleBookmarkUseCase

class BookmarkViewModel(
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
) : ViewModel() {
    val uiState =
        getBookmarksUseCase()
            .map { BookmarkUiState(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                BookmarkUiState(),
            )

    fun removeBookmark(url: String) {
        viewModelScope.launch { toggleBookmarkUseCase(url, false) }
    }
}
