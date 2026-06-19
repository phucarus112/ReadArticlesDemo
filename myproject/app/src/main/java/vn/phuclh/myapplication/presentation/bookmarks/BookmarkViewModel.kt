package vn.phuclh.myapplication.presentation.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.phuclh.myapplication.domain.model.Article
import vn.phuclh.myapplication.domain.usecase.GetBookmarksUseCase
import vn.phuclh.myapplication.domain.usecase.ToggleBookmarkUseCase
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    getBookmarksUseCase: GetBookmarksUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase
) : ViewModel() {

    val uiState = getBookmarksUseCase()
        .map { BookmarkUiState(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            BookmarkUiState()
        )

    fun removeBookmark(url: String) {
        viewModelScope.launch { toggleBookmarkUseCase(url, false) }
    }
}
