package vn.phuclh.myapplication.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vn.phuclh.myapplication.domain.repository.ArticleRepository
import vn.phuclh.myapplication.domain.usecase.ToggleBookmarkUseCase
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ArticleRepository,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase
) : ViewModel() {

    private val encodedUrl: String = checkNotNull(savedStateHandle["articleUrl"])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadArticle()
    }

    private fun loadArticle() {
        viewModelScope.launch {
            val url = java.net.URLDecoder.decode(encodedUrl, "UTF-8")
            val article = repository.getArticleByUrl(url)
            if (article != null) {
                _uiState.value = DetailUiState.Success(article)
            } else {
                _uiState.value = DetailUiState.Error("Article not found")
            }
        }
    }

    fun toggleBookmark() {
        val state = _uiState.value as? DetailUiState.Success ?: return
        viewModelScope.launch {
            toggleBookmarkUseCase(state.article.url, !state.article.isBookmarked)
            loadArticle()
        }
    }
}
